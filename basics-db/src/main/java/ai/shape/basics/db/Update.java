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

import java.util.ArrayList;
import java.util.List;

import static ai.shape.basics.util.Exceptions.assertNotNullParameter;

public class Update extends Statement {

  Table table;
  List<UpdateSet> sets;

  public Update(Tx tx, Table table, String alias) {
    super(tx);
    assertNotNullParameter(table, "table");
    this.table = table;
    tableAlias(table, alias);
  }

  public int execute() {
    return executeUpdate();
  }

  @Override
  protected void buildSqlOld(SqlBuilder sqlBuilder) {
    getDialect().buildUpdateSql(sqlBuilder, this);
  }

  @Override
  protected void collectParameters() {
    if (sets!=null) {
      sets.stream().forEach(set->set.collectParameters(this));
    }
    super.collectParameters();
  }

  public Update set(Column column, Object value) {
    if (sets==null) {
      sets = new ArrayList<>();
    }
    sets.add(new UpdateSet(column, value));
    return this;
  }

  @Override
  public Update where(Condition whereCondition) {
    return (Update) super.where(whereCondition);
  }

  public Table getTable() {
    return table;
  }

  public List<UpdateSet> getSets() {
    return sets;
  }
}
