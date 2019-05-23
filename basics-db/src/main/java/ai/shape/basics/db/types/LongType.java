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
package ai.shape.basics.db.types;

import ai.shape.basics.db.DataType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public class LongType implements DataType {

  public static final LongType INSTANCE = new LongType();

  @Override
  public String getSql() {
    return "INTEGER";
  }

  @Override
  public void setParameter(PreparedStatement statement, int jdbcParameterIndex, Object value) {
    try {
      if (value!=null) {
        long longValue = value!=null ? ((Number) value).longValue() : null;
        statement.setLong(jdbcParameterIndex, longValue);
      } else {
        statement.setNull(jdbcParameterIndex, Types.INTEGER);
      }
    } catch (SQLException e) {
      throw exceptionWithCause("set JDBC long appendParameter value "+value, e);
    }
  }

  @Override
  public boolean isRightAligned() {
    return true;
  }

  @Override
  public Long getResultSetValue(int index, ResultSet resultSet) {
    try {
      long longValue = resultSet.getLong(index);
      return resultSet.wasNull() ? null: longValue;
    } catch (SQLException e) {
      throw exceptionWithCause("get JDBC long value "+index+" from result set", e);
    }
  }

  @Override
  public int getSqlType() {
    return Types.INTEGER;
  }
}
