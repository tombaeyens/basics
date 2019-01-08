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

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public class JsonType implements DataType {

  @Override
  public String getSql() {
    return "JSON";
  }

  @Override
  public void setParameter(PreparedStatement statement, int i, Object value) {
    try {
      if (value!=null) {
        if (value instanceof String) {
          statement.setString(i, (String)value);
        } else {
          throw new RuntimeException("Unsupported data type: "+value);
        }
      }
    } catch (SQLException e) {
      throw exceptionWithCause("set JDBC json parameter value "+value, e);
    }
  }

  @Override
  public String getResultSetValue(int index, ResultSet resultSet) {
    try {
      return resultSet.getString(index);
    } catch (SQLException e) {
      throw exceptionWithCause("get JDBC json result set value "+index, e);
    }
  }

}
