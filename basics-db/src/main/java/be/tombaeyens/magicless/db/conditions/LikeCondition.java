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
package be.tombaeyens.magicless.db.conditions;

import be.tombaeyens.magicless.db.Column;
import be.tombaeyens.magicless.db.Condition;
import be.tombaeyens.magicless.db.SqlBuilder;
import be.tombaeyens.magicless.db.Statement;


public class LikeCondition implements Condition {

  Column column;
  Object pattern;

  public LikeCondition(Column column, Object pattern) {
    this.column = column;
    this.pattern = pattern;
  }

  @Override
  public void buildSql(SqlBuilder sql, Statement statement) {
    sql.appendText(statement.getQualifiedColumnName(column)+" like ");
    sql.appendParameter();
  }

  @Override
  public void collectParameters(Statement statement) {
    statement.addParameter(pattern!=null ? pattern : "%", column.getType());
  }
}
