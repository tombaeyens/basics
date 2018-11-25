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
package be.tombaeyens.magicless.db.types;

import be.tombaeyens.magicless.db.DataType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static ai.shape.magicless.app.util.Exceptions.exceptionWithCause;

public class FloatType implements DataType {

  @Override
  public String getSql() {
    return "REAL";
  }

  @Override
  public void setParameter(PreparedStatement statement, int i, Object value) {
    try {
      if (value!=null) {
        float floatValue = ((Number) value).floatValue();
        statement.setFloat(i, floatValue);
      } else {
        statement.setNull(i, Types.FLOAT);
      }
    } catch (SQLException e) {
      throw exceptionWithCause("set JDBC float appendParameter value "+value, e);
    }
  }

  @Override
  public boolean isRightAligned() {
    return true;
  }

  @Override
  public Float getResultSetValue(int index, ResultSet resultSet) {
    try {
      return resultSet.getFloat(index);
    } catch (SQLException e) {
      throw exceptionWithCause("get JDBC float value "+index+" from result set", e);
    }
  }

  @Override
  public int getSqlType() {
    return Types.FLOAT;
  }

}
