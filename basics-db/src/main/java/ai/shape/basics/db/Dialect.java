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

public class Dialect {

  public void initializeTable(Table table) {
    for (Column column: table.getColumns().values()) {
      initializeColumn(column);
    }
  }

  /** This method is called before the dialect is used and allows for dialects to switch the column types with dialect specific type versions */
  protected void initializeColumn(Column column) {
  }

  // TYPES //////////////////////////////////////////////////////////////////////////////////////////

  protected String getTypeSql(DataType type) {
    return type.getSql();
  }

  // CREATE TABLE //////////////////////////////////////////////////////////////////////////////////////////

  public SqlBuilder newCreateTableSql(CreateTable createTable) {
    return new CreateTableSql(createTable);
  }

  public CreateTableColumnSql newCreateTableColumnSql(SqlBuilder target) {
    return new CreateTableColumnSql(target);
  }

  public CreateTableColumnConstraintSql newCreateTableColumnConstraintSql(SqlBuilder target) {
    return new CreateTableColumnConstraintSql(target);
  }

  // ALTER TABLE //////////////////////////////////////////////////////////////////////////////////////////

  public SqlBuilder newAlterTableAddColumnSql(AlterTableAddColumn alterTableAddColumn) {
    return new AlterTableAddColumnSql(alterTableAddColumn);
  }

  public SqlBuilder newAlterTableAddForeignKeySql(AlterTableAddForeignKey alterTableAddForeignKey) {
    return new AlterTableAddForeignKeySql(alterTableAddForeignKey);
  }

  // DROP TABLE //////////////////////////////////////////////////////////////////////////////////////////

  public SqlBuilder newDropTableSql(DropTable dropTable) {
    return new DropTableSql(dropTable);
  }

  // DML STATEMENTS //////////////////////////////////////////////////////////////////////////////////////////

  public SqlBuilder newSelectSql(Select select) {
    return new SelectSql(select);
  }

  public SqlBuilder newInsertSql(Insert insert) {
    return new InsertSql(insert);
  }

  public SqlBuilder newUpdateSql(Update update) {
    return new UpdateSql(update);
  }

  public SqlBuilder newDeleteSql(Delete delete) {
    return new DeleteSql(delete);
  }
}
