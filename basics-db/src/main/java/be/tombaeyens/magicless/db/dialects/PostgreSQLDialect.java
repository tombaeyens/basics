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

import be.tombaeyens.magicless.db.Column;
import be.tombaeyens.magicless.db.DataType;
import be.tombaeyens.magicless.db.Dialect;
import be.tombaeyens.magicless.db.types.JsonType;

public class PostgreSQLDialect extends Dialect {

  public static PostgreSQLDialect INSTANCE  = new PostgreSQLDialect();

  public static class PostgreSQLJsonType extends JsonType {
    @Override
    public String getParameterText() {
      return "to_json(?::json)";
    }
  }

  @Override
  protected void initializeColumn(Column column) {
    DataType type = column.getType();
    if (JsonType.class.isAssignableFrom(type.getClass())) {
      column.setType(new PostgreSQLJsonType());
    }
    super.initializeColumn(column);
  }


}
