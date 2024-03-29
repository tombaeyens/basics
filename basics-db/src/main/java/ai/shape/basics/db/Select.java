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
import ai.shape.basics.util.Exceptions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import static ai.shape.basics.util.Exceptions.assertNotNull;
import static ai.shape.basics.util.Exceptions.assertNotNullParameter;

public class Select extends Statement {

  protected List<FieldExpressionWithAlias> fields = new ArrayList<>();
  protected List<TableWithJoins> froms = new ArrayList<>();

  protected Integer limit;
  protected OrderBy orderBy;
  private FieldExpressionWithAlias expression;

  public Select(Tx tx) {
    super(tx);
  }

  @Override
  protected SqlBuilder createSqlBuilder() {
    return getDialect().newSelectSql(this);
  }

  /* overriden to set the limit */
  @Override
  protected PreparedStatement createPreparedStatement(SqlBuilder sql) {
    PreparedStatement preparedStatement = super.createPreparedStatement(sql);
    if (limit!=null) {
      try {
        preparedStatement.setMaxRows(limit);
      } catch (SQLException e) {
        throw Exceptions.exceptionWithCause("set JDBC limit on prepared statement", e);
      }
    }
    return preparedStatement;
  }

  public SelectResults execute() {
    // If there are froms without fields, add all fields
    List<Table> fieldTables = new ArrayList<>();
    fields.stream().forEach(field->field.getExpression().collectTables(fieldTables));
    List<Table> fromTables = new ArrayList<>();
    froms.stream().forEach(from->from.collectTables(fromTables));

    if (!hasCount()) {
      List<Table> fromTablesNotUsedInFields = new ArrayList<>();
      fromTables.forEach(fromTable->{
        if (!fieldTables.contains(fromTable)) {
          fromTablesNotUsedInFields.add(fromTable);
        }
      });
      fromTablesNotUsedInFields.forEach(fromTableNotUsedInFields->
        fields(fromTableNotUsedInFields.getColumns().values())
      );
    }

    // If there are multiple tables involved
    if (fromTables.size()>1)  {
      // Ensure that all tables have aliases
      fromTables.forEach(fromTable->{
        String alias = getAlias(fromTable);
        if (alias==null) {
          alias = findNextAlias(fromTable);
          tableAlias(fromTable, alias);
        }
      });
    }

    return executeQuery();
  }

  private boolean hasCount() {
    return fields!=null && fields.stream()
      .filter(field -> field!=null && field.getExpression()!=null && field.getExpression().getClass()==Count.class)
      .findFirst()
      .isPresent();
  }

  public Select fields(SqlExpression... expressions) {
    return fields(Arrays.asList(expressions));
  }

  public Select fields(Collection<? extends SqlExpression> expressions) {
    if (expressions!=null) {
      for (SqlExpression expression: expressions) {
        field(expression);
      }
    }
    return this;
  }

  public Select field(SqlExpression expression) {
    return field(expression, null);
  }

  public Select field(SqlExpression expression, String alias) {
    return field(new FieldExpressionWithAlias(expression, alias));
  }

  protected Select field(FieldExpressionWithAlias expressionWithAlias) {
    fields.add(expressionWithAlias);
    return this;
  }

  public Select from(Table table) {
    from(table, null);
    return this;
  }

  public Select from(Table table, String alias) {
    assertNotNullParameter(table, "table");
    froms.add(new TableWithJoins(table));
    tableAlias(table, alias);
    return this;
  }

  public Select join(Table table, Column foreignKeyColumn) {
    return join(table, foreignKeyColumn, Join.TYPE_DEFAULT_INNER);
  }

  public Select joinLeftOuter(Table table, Column foreignKeyColumn) {
    return join(table, foreignKeyColumn, Join.TYPE_LEFT_OUTER);
  }

  public Select joinRightOuter(Table table, Column foreignKeyColumn) {
    return join(table, foreignKeyColumn, Join.TYPE_RIGHT_OUTER);
  }

  protected Select join(Table table, Column foreignKeyColumn, String joinType) {
    ForeignKey foreignKey = foreignKeyColumn.findForeignKey();
    assertNotNull(foreignKey, "No foreign key found between "+table+" in the froms of this select");

    Table fromTable = null;
    if (foreignKey.getFrom().getTable()==table) {
      fromTable = foreignKey.getTo().getTable();
    } else {
      fromTable = foreignKey.getFrom().getTable();
    }
    TableWithJoins tableWithJoins = findFrom(fromTable);

    Column primaryKeyColumn = foreignKey.getTo().getTable().getPrimaryKeyColumn();
    assertNotNull(primaryKeyColumn, "No primary key found in "+table);

    tableWithJoins
      .join(new Join()
        .type(joinType)
        .table(table)
        .on(Condition.equal(foreignKeyColumn, primaryKeyColumn)));
    return this;
  }

  private TableWithJoins findFrom(Table fromTable) {
    for (TableWithJoins from: froms) {
      if (from.getTable()==fromTable) {
        return from;
      }
      List<Join> joins = from.getJoins();
      if (joins!=null) {
        for (Join join: joins) {
          if (join.getTable()==fromTable) {
            return from;
          }
        }
      }
    }
    return null;
  }

  @Override
  public Select where(Condition whereCondition) {
    return (Select) super.where(whereCondition);
  }

  /** Returns JDBC (meaning starts at 1) index of the results. */
  public Integer getSelectorJdbcIndex(SqlExpression sqlExpression) {
    for (int i = 0; i< fields.size(); i++) {
      FieldExpressionWithAlias fieldExpressionWithAlias = fields.get(i);
      if (fieldExpressionWithAlias.getExpression().equals(sqlExpression)) {
        return i+1;
      }
    }
    return null;
  }

  public Select orderAsc(SqlExpression expression) {
    addOrderBy(new OrderBy.Ascending(expression));
    return this;
  }

  public Select orderDesc(SqlExpression expression) {
    addOrderBy(new OrderBy.Descending(expression));
    return this;
  }

  protected void addOrderBy(OrderBy.FieldDirection fieldDirection) {
    if (orderBy==null) {
      orderBy = new OrderBy();
    }
    orderBy.add(fieldDirection);
  }

  public boolean hasOrderBy() {
    return orderBy!=null && !orderBy.isEmpty();
  }

  public Tx getTx() {
    return tx;
  }

  public List<FieldExpressionWithAlias> getFields() {
    return fields;
  }

  public List<TableWithJoins> getFroms() {
    return froms;
  }

  public OrderBy getOrderBy() {
    return orderBy;
  }

  public Integer getLimit() {
    return this.limit;
  }

  public Select limit(Integer limit) {
    this.limit = limit;
    return this;
  }
}
