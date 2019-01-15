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

import java.util.Collection;
import java.util.List;

import static ai.shape.basics.util.Exceptions.assertNotEmptyCollection;
import static ai.shape.basics.util.Exceptions.assertNotNull;
import static java.util.stream.Collectors.joining;

public class Dialect {

  public void initializeTable(Table table) {
    for (Column column: table.getColumns().values()) {
      initializeColumn(column);
    }
  }

  /** This method allows for dialects to switch the column types with dialect specific type versions */
  protected void initializeColumn(Column column) {
  }

  // CREATE TABLE //////////////////////////////////////////////////////////////////////////////////////////

  public void buildCreateTableSql(SqlBuilder sql, CreateTable createTable) {
    sql.appendText("CREATE TABLE "+createTable.getTable().getName()+" (");

    Collection<Column> columns = createTable.getTable().getColumns().values();
    appendCreateTableColumnsSql(sql, columns);

    sql.appendText("\n);");
  }

  protected void appendCreateTableColumnsSql(SqlBuilder sql, Collection<Column> columns) {
    boolean first = true;
    for (Column column: columns) {
      if (first) {
        first = false;
      } else {
        sql.appendText(",");
      }
      sql.appendText("\n  ");
      appendCreateTableColumnSql(sql, column);
    }
  }

  protected void appendCreateTableColumnSql(SqlBuilder sql, Column column) {
    assertNotNull(column, "Column %d is null", column.getIndex());
    DataType type = column.getType();
    assertNotNull(type, "Column %d has type null", column.getIndex());

    sql.appendText(column.getName() + " " + type.getSql());

    List<Constraint> constraints = column.getConstraints();
    if (constraints != null) {
      constraints.forEach(constraint -> appendCreateTableColumnConstraintSql(sql, constraint));
    }
  }

  protected void appendCreateTableColumnConstraintSql(SqlBuilder sql, Constraint constraint) {
    sql.appendText(" "+constraint.getCreateTableSql());
  }

  // DROP TABLE //////////////////////////////////////////////////////////////////////////////////////////

  public void buildDropTableSql(SqlBuilder sql, DropTable dropTable) {
    sql.appendText("DROP TABLE ");
    if (dropTable.ifExists) {
      sql.appendText("IF EXISTS ");
    }
    sql.appendText(dropTable.table.getName());
    if (dropTable.cascade) {
      sql.appendText(" CASCADE;");
    }
  }

  // ALTER TABLE //////////////////////////////////////////////////////////////////////////////////////////

  public void buildAlterTableAddSql(SqlBuilder sql, AlterTableAdd alterTableAdd) {
    sql.appendText("ALTER TABLE "+ alterTableAdd.getTable().getName()+" ADD (");
    appendCreateTableColumnsSql(sql, alterTableAdd.getColumns());
    sql.appendText("\n);");
  }

  // SELECT //////////////////////////////////////////////////////////////////////////////////////////

  public void buildSelectSql(SqlBuilder sql, Select select) {
    sql.appendText("SELECT ");
    appendSelectFieldsSql(sql, select);
    sql.appendText(" \nFROM ");
    appendSelectFromsSql(sql, select);
    appendWhereCondition(sql, select.getWhereCondition(), select);
    appendOrderBy(sql, select.getOrderBy(), select);
    sql.appendText(";");
  }

  protected void appendSelectFieldsSql(SqlBuilder sql, Select select) {
    List<ExpressionWithAlias> expressions = select.getFields();
    assertNotEmptyCollection(expressions, "fields is empty. Specify at least one non-null Column or Function in Tx.newSelect(...)");

    ExpressionWithAlias first = expressions.get(0);
    for (ExpressionWithAlias expression : expressions) {
      if (expression !=first) {
        sql.appendText(", ");
      }
      expression.appendFieldSql(sql, select);
    }
  }

  protected void appendSelectFromsSql(SqlBuilder sql, Select select) {
//    // Add from tables for column fields that don't have
//    // their table in the froms
//    List<Table> tables =
//    List<SelectField> fields = select.getFields();
//    if (select.getFroms()==null && fields.size()>0) {
//      Optional<Table> tableOptional = fields.stream()
//        .filter(field -> field instanceof Column)
//        .map(field -> ((Column) field).getTable())
//        .findFirst();
//      if (tableOptional.isPresent()) {
//        select.from(tableOptional.get());
//      }
//    }

    List<From> froms = select.getFroms();
    assertNotEmptyCollection(froms, "froms is empty. Specify at least one non-null select.from(...)");

    From first = froms.get(0);
    for (From from: froms) {
      if (from!=first) {
        sql.appendText(", \n     ");
      }
      appendTableWithAliasSql(sql, select, from.getTable());
      if (from.getJoins()!=null) {
        for (Join join: from.getJoins()) {
          appendJoin(sql, select, join);
        }
      }
    }
  }

  protected void appendTableWithAliasSql(SqlBuilder sql, Statement statement, Table table) {
    String alias = statement.getAlias(table);
    if (alias!=null) {
      sql.appendText(table.getName()+" AS "+alias);
    } else {
      sql.appendText(table.getName());
    }
  }

  protected void appendJoin(SqlBuilder sql, Statement statement, Join join) {
    sql.appendText(join.getType()+" JOIN ");
    appendTableWithAliasSql(sql, statement, join.getTable());
    sql.appendText(" ON ");
    Condition on = join.getOn();
    if (on!=null) {
      on.buildSql(sql, statement);
    }
  }

  protected void appendWhereCondition(SqlBuilder sql, Condition condition, Statement statement) {
    if (condition!=null) {
      sql.appendText(" \nWHERE ");
      condition.buildSql(sql, statement);
    }
  }

  protected void appendOrderBy(SqlBuilder sql, OrderBy orderBy, Select select) {
    if (orderBy!=null) {
      sql.appendText(" \nORDER BY ");

      Object first = orderBy.getFieldDirections().get(0);
      for (OrderBy.FieldDirection direction: orderBy.getFieldDirections()) {
        if (direction!=first) {
          sql.appendText(", ");
        }
        direction.getExpression().appendFieldSql(sql, select);
        sql.appendText(" "+(direction.isAscending() ? "ASC" : "DESC"));
      }
    }
  }

  // INSERT //////////////////////////////////////////////////////////////////////////////////////////

  public void buildInsertSql(SqlBuilder sql, Insert insert) {
    Table table = insert.getTable();
    List<Insert.ColumnValue> columnValues = insert.getColumnValues();
    sql.appendText(
      "INSERT INTO "+table.getName()+" ("+
        columnValues.stream()
          .map(columnValue->columnValue.getColumn().getName())
          .collect(joining(", "))+
      ") \nVALUES (");

    Object first = columnValues.get(0);
    for (Insert.ColumnValue columnValue: columnValues) {
      if (columnValue!=first) {
        sql.appendText(", ");
      }
      sql.appendParameter();
    }

    sql.appendText(");");
  }

  // UPDATE //////////////////////////////////////////////////////////////////////////////////////////

  public void buildUpdateSql(SqlBuilder sql, Update update) {
    Table table = update.getTable();
    sql.appendText("UPDATE ");
    appendTableWithAliasSql(sql, update, table);
    sql.appendText(" \nSET ");
    appendUpdateAssignmentsSql(sql, update);
    appendWhereCondition(sql, update.getWhereCondition(), update);
    sql.appendText(";");
  }

  protected void appendUpdateAssignmentsSql(SqlBuilder sql, Update update) {
    List<UpdateSet> sets = update.getSets();
    assertNotEmptyCollection(sets, "sets is empty. Specify at least one non-null update.set(...)");

    Object first = sets.get(0);
    for (UpdateSet set: sets) {
      if (set!=first) {
        sql.appendText(", \n    ");
      }
      set.appendSql(sql, update);
    }
  }

  // DELETE //////////////////////////////////////////////////////////////////////////////////////////

  public void buildDeleteSql(SqlBuilder sql, Delete delete) {
    Table table = delete.getTable();
    sql.appendText("DELETE FROM ");
    appendTableWithAliasSql(sql, delete, table);
    appendWhereCondition(sql, delete.getWhereCondition(), delete);
    sql.appendText(";");
  }
}
