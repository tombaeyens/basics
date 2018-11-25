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
package be.tombaeyens.magicless.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectLogger {

  static int MAX_COLUMN_LENGTH = 20;

  Tx tx;
  SelectResults selectResults;
  Select select;
  List<SelectField> fields;
  List<String> fieldNames = new ArrayList<>();
  List<Integer> maxColumnLengths = new ArrayList<>();
  List<List<String>> rowValues = new ArrayList<>();
  String[] nextRow = null;

  public SelectLogger(SelectResults selectResults) {
    this.selectResults = selectResults;
    this.select = selectResults.select;
    this.tx = select.getTx();
    this.fields = select.getFields();
    for (int i = 0; i<fields.size(); i++) {
      SelectField field = fields.get(i);
      String fieldName = field.getName();
      maxColumnLengths.add(Math.min(fieldName.length(), MAX_COLUMN_LENGTH));
      fieldNames.add(fieldName);
    }
  }

  public void nextRow(boolean hasNext) {
    if (hasNext) {
      flushNextRow();
      nextRow = new String[fieldNames.size()];
    } else {
      logSelectResults();
    }
  }

  private void flushNextRow() {
    if (nextRow!=null) {
      // the next loop ensures proper max length calculation
      // in case the results for a results are not fetched
      // and a null value has to be displayed
      for (int i=0; i<nextRow.length; i++) {
        if (nextRow[i]==null) {
          selectResults.selectLogger.setValue(i, "?");
        }
      }
      rowValues.add(Arrays.asList(nextRow));
    }
  }

  void logSelectResults() {
    flushNextRow();

    // initialize the format and separatorLine
    int rowLength = 1; // the starting |
    StringBuilder formatBuilder = new StringBuilder();
    formatBuilder.append("|");
    for (int i=0; i<maxColumnLengths.size(); i++) {
      Integer columnLength = maxColumnLengths.get(i);
      rowLength += columnLength+1; // +1 for the | separator
      formatBuilder.append("%");
      if (!fields.get(i).getType().isRightAligned()) {
        formatBuilder.append("-");
      }
      formatBuilder.append(columnLength);
      formatBuilder.append("s|");
    }
    String format = formatBuilder.toString();

    // Build the appendText as plain appendText with newlines
    StringBuilder tableText = new StringBuilder();
    String headersFormat = format.replace('|', '+');
    String header = createRowLine(headersFormat, fieldNames).replace(' ','-');
    tableText.append(header);
    for (List<String> rowValues: rowValues) {
      tableText.append("\n");
      tableText.append(createRowLine(format, rowValues));
    }
    // log the SQL results table with the tx prefix
    tx.logSQL(tableText.toString());
  }

  private String createRowLine(String format, List<String> rowValues) {
    Object[] truncatedValues = new String[rowValues.size()];
    for (int i=0; i<rowValues.size(); i++) {
      String rowValue = rowValues.get(i);
      if (rowValue!=null && rowValue.length()>MAX_COLUMN_LENGTH) {
        rowValue = rowValue.substring(0, MAX_COLUMN_LENGTH-3)+"...";
      }
      truncatedValues[i] = rowValue;
    }
    return String.format(format, truncatedValues);
  }

  /** arrayIndex starts from 0 (not from 1 like in JDBC) */
  public void setValue(Integer arrayIndex, String valueText) {
    nextRow[arrayIndex] = valueText;
    Integer length = maxColumnLengths.get(arrayIndex);
    if (length < valueText.length()) {
      maxColumnLengths.set(arrayIndex, Math.min(valueText.length(), MAX_COLUMN_LENGTH));
    }
  }
}
