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

import ai.shape.basics.db.types.*;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public interface DataType {

  static IntegerType integerType() {
    return IntegerType.INSTANCE;
  }
  static LongType longType() {
    return LongType.INSTANCE;
  }
  static FloatType floatType() {
    return FloatType.INSTANCE;
  }
  static DoubleType doubleType() {
    return DoubleType.INSTANCE;
  }
  static VarcharType varcharType(int n) {
    return new VarcharType(n);
  }
  static TimestampType timestampType() {
    return TimestampType.INSTANCE;
  }
  static BooleanType booleanType() {
    return BooleanType.INSTANCE;
  }
  static ClobType clobType() {
    return ClobType.INSTANCE;
  }
  static JsonType jsonType() {
    return JsonType.INSTANCE;
  }
  static VarcharType idType() {
    return VarcharType.ID_TYPE;
  }

  String getSql();

  void setParameter(PreparedStatement statement, int jdbcParameterIndex, Object value);

  Object getResultSetValue(int index, ResultSet resultSet);

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
