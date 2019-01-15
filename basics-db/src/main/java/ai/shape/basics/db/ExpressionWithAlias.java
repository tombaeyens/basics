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

import java.util.List;

public class ExpressionWithAlias {

  protected Expression expression;
  protected String alias;

  public ExpressionWithAlias(Expression expression, String alias) {
    this.expression = expression;
    this.alias = alias;
  }

  public boolean isColumn() {
    return expression instanceof Column;
  }

  public boolean isColumn(Column column) {
    return expression==column;
  }

  public Expression getExpression() {
    return this.expression;
  }
  public void setExpression(Expression expression) {
    this.expression = expression;
  }
  public ExpressionWithAlias expression(Expression expression) {
    this.expression = expression;
    return this;
  }

  public String getAlias() {
    return this.alias;
  }
  public void setAlias(String alias) {
    this.alias = alias;
  }
  public ExpressionWithAlias alias(String alias) {
    this.alias = alias;
    return this;
  }

  public void appendFieldSql(SqlBuilder sql, Select select) {
    expression.appendFieldSql(sql, select);
    if (alias!=null) {
      sql.appendText(" AS "+alias);
    }
  }
}
