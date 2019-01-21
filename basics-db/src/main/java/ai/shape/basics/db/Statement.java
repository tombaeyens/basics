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

import ai.shape.basics.db.conditions.AndCondition;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public abstract class Statement {

  protected Tx tx;
  Map<Table,String> tableAliases;
  Condition whereCondition;
  List<Parameter> parameters;

  public Statement(Tx tx) {
    this.tx = tx;
  }

  protected int executeUpdate() {
    collectParameters();
    SqlBuilder sql = new SqlBuilder(parameters);
    PreparedStatement jdbcStatement = createPreparedStatement(sql);
    try {
      int updateCount = jdbcStatement.executeUpdate();
      logUpdateCount(updateCount);
      return updateCount;
    } catch (SQLException e) {
      throw exceptionWithCause("execute "+getClass().getSimpleName()+" \n"+sql.getDebugInfo(), e);
    } finally {
      try {
        if (jdbcStatement!=null) jdbcStatement.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  /** Delegates to the appropriate dialect method */
  protected abstract void buildSql(SqlBuilder sqlBuilder);

  protected void logUpdateCount(int updateCount) {
    Db.DB_LOGGER.debug(tx + " " + getPastTense() + " " + updateCount + " rows");
  }

  protected SelectResults executeQuery() {
    collectParameters();
    SqlBuilder sql = new SqlBuilder(parameters);
    PreparedStatement jdbcStatement = createPreparedStatement(sql);
    try {
      ResultSet resultSet = jdbcStatement.executeQuery();
      return new SelectResults((Select)this, resultSet, sql);
    } catch (SQLException e) {
      throw exceptionWithCause("execute query \n"+sql.getDebugInfo()+"\n-->", e);
    } // the statement should not be closed in a finally block because the returned select results should be closed.
  }

  protected PreparedStatement createPreparedStatement(SqlBuilder sql) {
    try {
      buildSql(sql);
      PreparedStatement jdbcStatement = tx.createPreparedStatement(sql.getSql());
      setParameters(jdbcStatement);
      tx.logSQL(sql.getSqlLog());
      return jdbcStatement;
    } catch (Exception e) {
      throw exceptionWithCause("create prepared statement \n"+sql.getDebugInfo()+"\n-->", e);
    }
  }

  public void addParameter(Object value, DataType type) {
    if (parameters==null) {
      parameters = new ArrayList<>();
    }
    parameters.add(new Parameter(value, type));
  }

  public void setParameters(PreparedStatement jdbcStatement) {
    if (parameters!=null) {
      for (int i=0; i<parameters.size(); i++) {
        Parameter parameter = parameters.get(i);
        DataType type = parameter.getType();
        Object value = parameter.getValue();
        int jdbcParameterIndex = i + 1;
        type.setParameter(jdbcStatement, jdbcParameterIndex, value);
        parameter.setLogValue(type.getLogText(value));
      }
    }
  }

  /** override if the statement sets parameters and ensure that the
   * ordering of parameters corresponds to the ordering of the ?
   * that were generated in the sql */
  protected void collectParameters() {
    if (whereCondition!=null) {
      whereCondition.collectParameters(this);
    }
  }

  protected String getPastTense() {
    return getClass().getSimpleName()+"d";
  }

  public String getQualifiedColumnName(Column column) {
    String alias = tableAliases !=null ? tableAliases.get(column.getTable()) : null;
    return alias!=null ? alias+"."+column.getName() : column.getName();
  }

  protected Statement tableAlias(Table table, String alias) {
    if (tableAliases ==null) {
      tableAliases = new LinkedHashMap<>();
    }
    tableAliases.put(table, alias);
    return this;
  }

  protected String getAlias(Table table) {
    return tableAliases.get(table);
  }

  protected String findNextAlias(Table table) {
    String base = table.getName().toUpperCase();
    int length = 1;
    while (length<base.length()+1) {
      String candidate = base.substring(0, length);
      if (!tableAliases.containsValue(candidate)) {
        return candidate;
      }
      length++;
    }
    int index = 2;
    while (tableAliases.containsValue(base+index)) {
      index++;
    }
    return base+index;
  }

  public Statement where(Condition whereCondition) {
    if (this.whereCondition!=null) {
      if (this.whereCondition instanceof AndCondition) {
        ((AndCondition)this.whereCondition).add(whereCondition);
      } else if (whereCondition instanceof AndCondition) {
        ((AndCondition)whereCondition).add(this.whereCondition);
        this.whereCondition = whereCondition;
      } else {
        this.whereCondition = new AndCondition(new Condition[]{this.whereCondition, whereCondition});
      }
    } else {
      this.whereCondition = whereCondition;
    }
    return this;
  }

  public Condition getWhereCondition() {
    return whereCondition;
  }

  public boolean hasWhereCondition() {
    return whereCondition!=null;
  }

  protected Dialect getDialect() {
    return tx.getDb().getDialect();
  }

  public Tx getTx() {
    return tx;
  }
}
