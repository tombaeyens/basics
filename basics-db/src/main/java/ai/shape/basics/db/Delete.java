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

import static ai.shape.basics.util.Exceptions.assertNotNullParameter;

public class Delete extends Statement {

  Table table;
  Boolean cascade;

  public Delete(Tx tx, Table table, String alias) {
    super(tx);
    assertNotNullParameter(table, "table");
    this.table = table;
    tableAlias(table, alias);
  }

  public int execute() {
    return executeUpdate();
  }

  @Override
  protected SqlBuilder createSqlBuilder() {
    return getDialect().newDeleteSql(this);
  }

  @Override
  public Delete where(Condition whereCondition) {
    return (Delete) super.where(whereCondition);
  }

  public Delete cascade() {
    this.cascade = Boolean.TRUE;
    return this;
  }

  public Table getTable() {
    return table;
  }
}
