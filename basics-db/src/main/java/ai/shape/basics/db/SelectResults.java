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

import ai.shape.basics.util.Exceptions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static ai.shape.basics.util.Exceptions.assertNotNull;
import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public class SelectResults {

  Select select;
  ResultSet resultSet;
  SqlBuilder sql;
  SelectLogger selectLogger;

  public SelectResults(Select select, ResultSet resultSet, SqlBuilder sql) {
    this.select = select;
    this.resultSet = resultSet;
    this.sql = sql;
    this.selectLogger = new SelectLogger(this);
  }

  public <T> List<T> getAll(Function<SelectResults, T> mapper) {
    List<T> rowObjects = new ArrayList<>();
    while (resultSetNext()) {
      T rowObject = mapper.apply(this);
      rowObjects.add(rowObject);
    }
    this.selectLogger.logRows();
    return rowObjects;
  }

  public <T> Optional<T> getFirst(Function<SelectResults, T> mapper) {
    this.selectLogger.logRowByRow();
    T rowObject = null;
    if (resultSetNext()) {
      rowObject = mapper.apply(this);
    }
    this.selectLogger.logRows();
    return Optional.ofNullable(rowObject);
  }

  public void forEach(Consumer<SelectResults> action) {
    while (resultSetNext()) {
      action.accept(this);
    }
    this.selectLogger.logRows();
  }

  @SuppressWarnings("unchecked")
  public <T> T get(SelectField selectField) {
    if (selectField instanceof Column) {
      Column column = (Column) selectField;
      Integer index = select.getSelectorJdbcIndex(column);
      assertNotNull(index, "Could find index position of results "+column+" in select \n"+sql.getDebugInfo());
      DataType type = column.getType();
      T value = (T)type.getResultSetValue(index, resultSet);
      selectLogger.setValue(index-1, type.getLogText(value));
      return value;
    } else {
      Exceptions.assertNotNullParameter(selectField, "selectField");
      throw new RuntimeException("Select field of type "+selectField.getClass().getSimpleName()+" not supported yet.");
    }
  }

  /** loops over all the results and logs the results in a table structure.
   * @return the number of rows that were logged */
  public long logAllRows() {
    List<Object> nulls = getAll(selectResults -> {
      for (SelectField field: select.getFields()) {
        get(field);
      }
      return null;
    });
    return nulls.size();
  }

  private boolean resultSetNext() {
    try {
      boolean hasNext = resultSet.next();
      selectLogger.nextRow(hasNext);
      return hasNext;
    } catch (SQLException e) {
      throw exceptionWithCause("get next() on JDBC result set for select \n"+sql.getDebugInfo(), e);
    }
  }

}
