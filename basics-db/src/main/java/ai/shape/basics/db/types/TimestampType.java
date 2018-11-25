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

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Date;

import static ai.shape.magicless.app.util.Exceptions.exceptionWithCause;
import static org.slf4j.LoggerFactory.getLogger;

public class TimestampType implements DataType {

  private static final Logger log = getLogger(TimestampType.class.getName());

  @Override
  public String getSql() {
    return "TIMESTAMP";
  }

  @Override
  public void setParameter(PreparedStatement statement, int i, Object value) {
    try {
      Timestamp timestamp = null;

      if (value!=null) {
        if (value instanceof Timestamp) {
          timestamp = (Timestamp) value;
        } else if (value instanceof Date) {
          timestamp = new Timestamp(((Date)value).getTime());
        } else if (value instanceof LocalDateTime) {
          timestamp = Timestamp.valueOf((LocalDateTime)value);
        } else {
          throw new RuntimeException("Unsupported data type: "+value);
        }
      }

      if (timestamp!=null) {
        statement.setTimestamp(i, timestamp);
      } else {
        statement.setNull(i, getSqlType());
      }

    } catch (SQLException e) {
      throw exceptionWithCause("set JDBC timestamp appendParameter value "+value, e);
    }
  }

  @Override
  public LocalDateTime getResultSetValue(int index, ResultSet resultSet) {
    try {
      Timestamp timestamp = resultSet.getTimestamp(index);
      return timestamp!=null ? timestamp.toLocalDateTime() : null;
    } catch (SQLException e) {
      throw exceptionWithCause("get JDBC timestamp result set value "+index, e);
    }
  }

  @Override
  public int getSqlType() {
    return Types.TIMESTAMP;
  }
}
