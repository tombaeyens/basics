/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ai.shape.basics.db;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static ai.shape.basics.db.SelectLogger.LogMode.ALL_ROWS_AT_THE_END;
import static ai.shape.basics.db.SelectLogger.LogMode.ROW_BY_ROW;

public class SelectLogger {

  enum LogMode {
    /** ALL_ROWS_AT_THE_END is the default and used
     * with {@link SelectResults#getAll(Function)}.
     * It first collects all the values to calculate the column widths,
     * Logging will be triggered when {@link ResultSet#next()}
     * returns false. */
    ALL_ROWS_AT_THE_END,

    /** When getting a single result with {@link SelectResults#getFirst(Function)}
     * We log each line individually.  Each time a line is logged, also the
     * header is printed.  Normally there will only be 1 line logged.*/
    ROW_BY_ROW
  }

  static int MAX_COLUMN_LENGTH = 20;

  Tx tx;
  SelectResults selectResults;
  Select select;
  List<FieldExpressionWithAlias> expressions;
  List<String> fieldNames = null;
  List<Integer> maxColumnLengths = null;
  List<List<String>> rowValues = new ArrayList<>();
  String[] nextRow = null;
  LogMode logMode = ALL_ROWS_AT_THE_END;

  public SelectLogger(SelectResults selectResults) {
    this.selectResults = selectResults;
    this.select = selectResults.select;
    this.tx = select.getTx();
    this.expressions = select.getFields();
  }

  public void logRowByRow() {
    logMode = ROW_BY_ROW;
  }

  public void nextRow(boolean hasNext) {
    if (hasNext) {
      flushNextRow();
      nextRow = new String[select.getFields().size()];
    }
  }

  private void flushNextRow() {
    if (nextRow!=null) {
      // the next loop ensures proper max length calculation
      // in case the results for a results are not fetched
      // and a null value has to be displayed
      for (int i=0; i<nextRow.length; i++) {
        if (nextRow[i]==null) {
          setValue(i, "?");
        }
      }
      rowValues.add(Arrays.asList(nextRow));
    }
  }

  void logRows() {
    // flushNextRow ensures proper max length calculation in case some values in a resultSet are not fetched
    flushNextRow();

    // Calculate colun widths and initialize the row format to something like this:
    // |%-2s|%-5s|%-4s|%-8s|%4s|%4s|%-11s|%-12s|%-10s|%15s|%15s|
    String rowFormat = getRowFormat();

    // Build the appendText as plain appendText with newlines
    StringBuilder tableText = new StringBuilder();

    // Append header
    String headersFormat = rowFormat.replace('|', '+');
    String header = createRowLine(headersFormat, fieldNames).replace(' ','-');
    tableText.append(header);

    // Append rows
    for (List<String> rowValues: rowValues) {
      tableText.append("\n");
      tableText.append(createRowLine(rowFormat, rowValues));
    }

    // Clean up the stuff we don't need any more
    rowValues.clear(); // Normally not, but in theory it could be that more rows will be fetched and logged later
    this.maxColumnLengths = null;
    this.fieldNames = null;

    // logAllRows the SQL results table with the tx prefix
    tx.logSQL(tableText.toString());
  }

  /** creates a string format like eg
   * |%-2s|%-5s|%-4s|%-8s|%4s|%4s|%-11s|%-12s|%-10s|%15s|%15s|
   * based on the max width over all values in a column */
  private String getRowFormat() {
    int rowLength = 1; // the starting |
    StringBuilder formatBuilder = new StringBuilder();
    formatBuilder.append("|");
    ensureFieldNamesAndMaxColumnLengthsInitialized();
    for (int i=0; i<maxColumnLengths.size(); i++) {
      Integer columnLength = maxColumnLengths.get(i);
      rowLength += columnLength+1; // +1 for the | separator
      formatBuilder.append("%");
      if (!expressions.get(i).getExpression().getType().isRightAligned()) {
        formatBuilder.append("-");
      }
      formatBuilder.append(columnLength);
      formatBuilder.append("s|");
    }
    return formatBuilder.toString();
  }

  private String createRowLine(String format, List<String> rowValues) {
    Object[] truncatedValues = new String[rowValues.size()];
    for (int i=0; i<rowValues.size(); i++) {
      String rowValue = rowValues.get(i);
      rowValue = rowValue.replaceAll("(\\s)+"," ");
      if (rowValue!=null && rowValue.length()>MAX_COLUMN_LENGTH) {
        rowValue = rowValue.substring(0, MAX_COLUMN_LENGTH-3)+"...";
      }
      truncatedValues[i] = rowValue;
    }
    return String.format(format, truncatedValues);
  }

  /** arrayIndex starts from 0 (not from 1 like in JDBC) */
  public void setValue(Integer arrayIndex, String valueText) {
    ensureFieldNamesAndMaxColumnLengthsInitialized();
    nextRow[arrayIndex] = valueText;
    Integer length = maxColumnLengths.get(arrayIndex);
    if (length < valueText.length()) {
      maxColumnLengths.set(arrayIndex, Math.min(valueText.length(), MAX_COLUMN_LENGTH));
    }
  }

  private void ensureFieldNamesAndMaxColumnLengthsInitialized() {
    if (fieldNames==null) {
      this.maxColumnLengths = new ArrayList<>();
      this.fieldNames = new ArrayList<>();
      for (int i = 0; i< expressions.size(); i++) {
        FieldExpressionWithAlias expressionWithAlias = expressions.get(i);
        String alias = expressionWithAlias.getAlias();
        String fieldName = alias !=null ? alias : expressionWithAlias.getExpression().getTitle();
        maxColumnLengths.add(Math.min(fieldName.length(), MAX_COLUMN_LENGTH));
        fieldNames.add(fieldName);
      }
    }
  }
}
