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
package be.tombaeyens.magicless.db.dialects;

import ai.shape.magicless.app.util.Io;
import be.tombaeyens.magicless.db.Column;
import be.tombaeyens.magicless.db.DataType;
import be.tombaeyens.magicless.db.Dialect;
import be.tombaeyens.magicless.db.types.JsonType;

import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ai.shape.magicless.app.util.Exceptions.exceptionWithCause;

public class H2Dialect extends Dialect {

  public static H2Dialect INSTANCE  = new H2Dialect();


  public static class H2JsonType extends JsonType {
    @Override
    public String getSql() {
      return "CLOB";
    }

    @Override
    public void setParameter(PreparedStatement statement, int i, Object value) {
      try {
        if (value!=null) {
          if (value instanceof String) {
            statement.setClob(i, new StringReader((String) value));
          } else {
            throw new RuntimeException("Unsupported data type: "+value);
          }
        }

      } catch (SQLException e) {
        throw exceptionWithCause("set JDBC clob parameter value "+value, e);
      }
    }

    @Override
    public String getResultSetValue(int index, ResultSet resultSet) {
      try {
        Reader reader = resultSet.getCharacterStream(index);
        return reader!=null ? Io.getString(reader) : null;
      } catch (SQLException e) {
        throw exceptionWithCause("get JDBC clob result set value "+index, e);
      }
    }
  }

  @Override
  protected void initializeColumn(Column column) {
    DataType type = column.getType();
    if (JsonType.class.isAssignableFrom(type.getClass())) {
      column.setType(new H2JsonType());
    }
    super.initializeColumn(column);
  }
}
