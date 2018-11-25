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

import ai.shape.magicless.app.util.Exceptions;
import ai.shape.magicless.app.util.Mutable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static ai.shape.magicless.app.util.Exceptions.assertNotNull;

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
    return stream()
      .map(mapper)
      .collect(Collectors.toList());
  }

  public <T> Optional<T> getFirst(Function<SelectResults, T> mapper) {
    return stream()
      .map(mapper)
      .findFirst();
  }

  public boolean next() {
    try {
      boolean hasNext = resultSet.next();
      selectLogger.nextRow(hasNext);
      return hasNext;
    } catch (SQLException e) {
      throw Exceptions.exceptionWithCause("get next() on JDBC result set for select \n"+sql.getDebugInfo(), e);
    }
  }

  public <T> T get(SelectField selectField) {
    if (selectField instanceof Column) {
      Column column = (Column) selectField;
      Integer index = select.getSelectorJdbcIndex(column);
      assertNotNull(index, "Could find index position of results "+column+" in select \n"+sql.getDebugInfo());
      DataType type = column.getType();
      T value = type.getResultSetValue(index, resultSet);
      selectLogger.setValue(index-1, type.getLogText(value));
      return value;
    } else {
      Exceptions.assertNotNullParameter(selectField, "selectField");
      throw new RuntimeException("Select field of type "+selectField.getClass().getSimpleName()+" not supported yet.");
    }
  }

//  /** normally this is triggered automatically by the last .next() called on
//   * which returns false.  But in case .next() is not called in a
//   * while loop and never returns false, you can call this manually. */
//  public void logResults() {
//    selectLogger.logSelectResults();
//  }

  /** loops over all the results and logs the results in a table structure.
   * @return the number of rows that were logged */
  public long log() {
    final Mutable<Long> count = new Mutable<Long>(0l);
    stream().forEach(selectResults -> {
      for (SelectField field: select.getFields()) {
        get(field);
        count.set(count.get()+1);
      }
    });
    return count.get();
  }

  private class SelectResultsSpliterator extends Spliterators.AbstractSpliterator<SelectResults> {
    public SelectResultsSpliterator() {
      super(Long.MAX_VALUE,Spliterator.ORDERED);
    }
    @Override
    public boolean tryAdvance(Consumer<? super SelectResults> action) {
      if (next()) {
        action.accept(SelectResults.this);
        return true;
      } else {
        return false;
      }
    }
    private boolean next() {
      try {
        return SelectResults.this.next();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  public Stream<SelectResults> stream() {
    return StreamSupport.stream(new SelectResultsSpliterator(),false);
  }
}
