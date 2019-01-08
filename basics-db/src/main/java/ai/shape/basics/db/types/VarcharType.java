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
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;
import static org.slf4j.LoggerFactory.getLogger;

public class VarcharType implements DataType {

  private static final Logger log = getLogger(VarcharType.class.getName());

  int n;

  public VarcharType(int n) {
    this.n = n;
  }

  public int getN() {
    return n;
  }

  @Override
  public String getSql() {
    return "VARCHAR("+n+")";
  }

  @Override
  public String getLogText(Object value) {
    return value!=null ? "'"+value+"'" : "null";
  }

  @Override
  public void setParameter(PreparedStatement statement, int i, Object value) {
    try {
      String string = null;

      if (value instanceof String) {
        string = (String) value;
      } else if (value!=null) {
        string = value.toString();
      }

      if (string!=null) {
        // logAllRows.debug("Setting param "+i+" to string value "+string);
        statement.setString(i, string);
      } else {
        // logAllRows.debug("Setting param "+i+" to null");
        statement.setNull(i, getSqlType());
      }

    } catch (SQLException e) {
      throw exceptionWithCause("set JDBC varchar appendParameter value "+value, e);
    }
  }

  @Override
  public String getResultSetValue(int index, ResultSet resultSet) {
    try {
      return resultSet.getString(index);
    } catch (SQLException e) {
      throw exceptionWithCause("get JDBC string result set value "+index, e);
    }
  }
}
