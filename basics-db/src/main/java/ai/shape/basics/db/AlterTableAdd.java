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

import ai.shape.basics.db.constraints.ForeignKey;

import java.util.ArrayList;
import java.util.List;

public class AlterTableAdd extends Statement {

  Table table;
  List<Column> columns = new ArrayList<>();
  List<ForeignKey> foreignKeys = new ArrayList<>();

  public AlterTableAdd(Tx tx, Table table) {
    super(tx);
    this.table = table;
  }

  public AlterTableAdd add(Column column) {
    columns.add(column);
    return this;
  }
  public AlterTableAdd add(ForeignKey foreignKey) {
    foreignKeys.add(foreignKey);
    return this;
  }
  public AlterTableAdd addAll(List<ForeignKey> foreignKeys) {
    this.foreignKeys.addAll(foreignKeys);
    return this;
  }

  public int execute() {
    return executeUpdate();
  }

  @Override
  protected void buildSql(SqlBuilder sqlBuilder) {
    getDialect().buildAlterTableAddSql(sqlBuilder, this);
  }

  protected void logUpdateCount(int updateCount) {
  }

  public Table getTable() {
    return table;
  }

  public List<Column> getColumns() {
    return columns;
  }

  public List<ForeignKey> getForeignKeys() {
    return foreignKeys;
  }
}
