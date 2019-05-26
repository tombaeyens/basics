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

public class UpdateSql extends DmlStatementSqlBuilder<Update> {

  public UpdateSql(Update update) {
    super(update);
  }

  @Override
  public void buildSqlNew() {
    Table table = statement.getTable();
    appendText("UPDATE ");
    appendTableWithAliasSql(table);
    appendText(" \nSET ");
    appendUpdateAssignmentsSql();
    appendWhereCondition(statement.getWhereCondition());
    appendText(";");
  }

  protected void appendUpdateAssignmentsSql() {
    List<UpdateSet> sets = statement.getSets();
    assertNotEmptyCollection(sets, "sets is empty. Specify at least one non-null update.set(...)");

    Object first = sets.get(0);
    for (UpdateSet set: sets) {
      if (set!=first) {
        appendText(", \n    ");
      }
      set.appendSql(this, statement);
    }
  }
}
