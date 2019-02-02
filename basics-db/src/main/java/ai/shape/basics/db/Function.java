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

public abstract class Function implements FieldExpression {

  public static class Lower extends Function {
    FieldExpression expression;

    public Lower(FieldExpression expression) {
      this.expression = expression;
    }

    @Override
    public String getTitle() {
      return "lower("+ expression.getTitle()+")";
    }

    @Override
    public void appendFieldSql(SqlBuilder sql, Statement statement) {
      sql.appendText("lower(");
      expression.appendFieldSql(sql, statement);
      sql.appendText(")");
    }

    @Override
    public DataType getType() {
      return expression.getType();
    }

    @Override
    public void collectTables(List<Table> fieldTables) {
      expression.collectTables(fieldTables);
    }
  }

  public static Lower lowerCase(Column column) {
    return new Lower(column);
  }
}
