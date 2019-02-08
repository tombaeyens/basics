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

public class AlterTableAddColumn extends Statement {

  // column is singular because H2 does not support
  // ALTER TABLE ADD with multiple things to add
  Column column;

  public AlterTableAddColumn(Tx tx, Column column) {
    super(tx);
    this.column = column;
  }

  public int execute() {
    return executeUpdate();
  }

  @Override
  protected void buildSql(SqlBuilder sqlBuilder) {
    getDialect().buildAlterTableAddColumnSql(sqlBuilder, this);
  }

  protected void logUpdateCount(int updateCount) {
  }

  public Column getColumn() {
    return column;
  }

  public Table getTable() {
    return column.getTable();
  }
}
