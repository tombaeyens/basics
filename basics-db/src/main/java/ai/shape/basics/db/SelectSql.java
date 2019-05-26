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

import static ai.shape.basics.util.Exceptions.assertNotEmptyCollection;

public class SelectSql extends DmlStatementSqlBuilder<Select> {

  public SelectSql(Select select) {
    super(select);
  }

  @Override
  public void buildSqlNew() {
    appendText("SELECT ");
    appendSelectFieldsSql();
    appendText(" \nFROM ");
    appendSelectFromsSql();
    appendWhereCondition(statement.getWhereCondition());
    appendOrderBy(statement.getOrderBy());
    appendText(";");
  }

  protected void appendSelectFieldsSql() {
    List<FieldExpressionWithAlias> expressions = statement.getFields();
    assertNotEmptyCollection(expressions, "fields is empty. Specify at least one non-null Column or Function in Tx.newSelect(...)");

    FieldExpressionWithAlias first = expressions.get(0);
    for (FieldExpressionWithAlias expression : expressions) {
      if (expression !=first) {
        appendText(", ");
      }
      expression.appendFieldSql(this, statement);
    }
  }

  protected void appendSelectFromsSql() {
    List<TableWithJoins> froms = statement.getFroms();
    assertNotEmptyCollection(froms, "froms is empty. Specify at least one non-null select.from(...)");

    TableWithJoins first = froms.get(0);
    for (TableWithJoins from: froms) {
      if (from!=first) {
        appendText(", \n     ");
      }
      appendTableWithAliasSql(from.getTable());
      if (from.getJoins()!=null) {
        for (Join join: from.getJoins()) {
          appendJoin(join);
        }
      }
    }
  }

  protected void appendJoin(Join join) {
    appendText(" \n  ");
    appendText(join.getType()+" JOIN ");
    appendTableWithAliasSql(join.getTable());
    appendText(" ON ");
    Condition on = join.getOn();
    if (on!=null) {
      on.buildSql(this, statement);
    }
  }

  protected void appendOrderBy(OrderBy orderBy) {
    if (orderBy!=null) {
      appendText(" \nORDER BY ");

      Object first = orderBy.getFieldDirections().get(0);
      for (OrderBy.FieldDirection direction: orderBy.getFieldDirections()) {
        if (direction!=first) {
          appendText(", ");
        }
        direction.getExpression().appendFieldSql(this, statement);
        appendText(" "+(direction.isAscending() ? "ASC" : "DESC"));
      }
    }
  }
}
