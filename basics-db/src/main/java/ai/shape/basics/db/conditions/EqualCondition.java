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
package ai.shape.basics.db.conditions;

import ai.shape.basics.db.*;


public class EqualCondition implements Condition {

  SqlExpression expression;
  Object value;

  public EqualCondition(SqlExpression expression, Object value) {
    this.expression = expression;
    this.value = value;
  }

  @Override
  public void buildSql(SqlBuilder sql, Statement statement) {
    expression.appendFieldSql(sql, statement);
    if (value!=null) {
      sql.appendText(" = ");
      if (value instanceof Column) {
        ((Column) value).appendFieldSql(sql, statement);
      } else {
        sql.appendParameter();
      }
    } else {
      sql.appendText(" IS NULL");
    }
  }

  @Override
  public void collectParameters(Statement statement) {
    if (value!=null && !(value instanceof Column)) {
      statement.addParameter(value, expression.getType());
    }
  }
}
