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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public interface DataType {

  String getSql();

  void setParameter(PreparedStatement statement, int i, Object value);

  <T> T getResultSetValue(int index, ResultSet resultSet);

  default String getLogText(Object value) {
    return value!=null ? value.toString() : "null";
  }

  default boolean isRightAligned() {
    return false;
  }

  String DEFAULT_PARAMETER_TEXT = "?";
  default String getParameterText() {
    return DEFAULT_PARAMETER_TEXT;
  }

  default int getSqlType() {
    return Types.VARCHAR;
  }
}
