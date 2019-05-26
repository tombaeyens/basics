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

public class DmlStatementSqlBuilder<T extends Statement> extends StatementSqlBuilder<T> {

  public DmlStatementSqlBuilder(T statement) {
    super(statement);
  }

  protected void appendTableWithAliasSql(Table table) {
    String alias = statement.getAlias(table);
    if (alias!=null) {
      appendText(table.getName()+" AS "+alias);
    } else {
      appendText(table.getName());
    }
  }

  protected void appendWhereCondition(Condition condition) {
    if (condition!=null) {
      appendText(" \nWHERE ");
      condition.buildSql(this, statement);
    }
  }
}
