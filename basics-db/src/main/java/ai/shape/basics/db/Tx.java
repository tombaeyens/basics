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

import ai.shape.basics.util.Io;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static ai.shape.basics.db.Db.DB_LOGGER;
import static ai.shape.basics.util.Exceptions.exceptionWithCause;
import static ai.shape.basics.util.Log.logLines;

public class Tx {

  private static long nextTxId = 1;
  private static ThreadLocal<Tx> currentTx = new ThreadLocal<>();

  protected long id = nextTxId++;
  protected Db db;
  protected Connection connection;
  protected boolean isRollbackOnly = false;
  protected Object result;
  protected Throwable rollbackReason;

  public Tx(Db db, Connection connection) {
    this.db = db;
    this.connection = connection;
    DB_LOGGER.debug(this+" starting");
    currentTx.set(this);
  }

  public static Tx getCurrentTx() {
    return currentTx.get();
  }

  public String toString() {
    return "Tx"+id;
  }

  public Db getDb() {
    return this.db;
  }

  public Connection getConnection() {
    return this.connection;
  }

  public Object getResult() {
    return this.result;
  }
  public void setResult(Object result) {
    this.result = result;
  }
  public Tx result(Object result) {
    this.result = result;
    return this;
  }

  public void setRollbackOnly() {
    setRollbackOnly(null);
  }
  
  public void setRollbackOnly(Throwable rollbackReason) {
    this.isRollbackOnly = true;
    this.rollbackReason = rollbackReason;
  }

  public boolean isRollbackOnly() {
    return isRollbackOnly;
  }

  protected void end() {
    currentTx.set(null);
    if (isRollbackOnly) {
      try {
        DB_LOGGER.warn(this+" rolling back" + (rollbackReason!=null ? " because: " + rollbackReason : ""));
        connection.rollback();
      } catch (SQLException e) {
        DB_LOGGER.error(this+" rollback failed: " + e.getMessage(), e);
      }
    } else {
      try {
        DB_LOGGER.debug(this+" committing");
        connection.commit();
      } catch (SQLException e) {
        DB_LOGGER.error(this+" commit failed: " + e.getMessage(), e);
      }
    }
  }

  public void logSQL(String sql) {
    if (sql!=null && DB_LOGGER.isDebugEnabled()) {
      logLines(DB_LOGGER, sql, this + " ");
    }
  }

  public void executeScriptResource(String resource) {
    String script = Io.getResourceAsString(resource);
    for (String ddl: script.split(";")) {
      try {
        int updates = connection.prepareStatement(ddl).executeUpdate();
        logLines(DB_LOGGER, ddl.trim());

      } catch (SQLException e) {
        throw exceptionWithCause("executing script statement "+ddl, e);
      }
    }
  }

  public List<Table> getMetaDataTables() {
    List<Table> tables = new ArrayList<>();
    try {
      ResultSet tablesResultSet = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
      while (tablesResultSet.next()) {
        Table table = new Table();
        table.name(tablesResultSet.getString(3));

        ResultSetMetaData resultSetMetaData = connection
          .createStatement()
          .executeQuery("select * from " + table.getName())
          .getMetaData();
        for (int i=1; i<=resultSetMetaData.getColumnCount(); i++) {
          Column column = new Column();
          column.name(resultSetMetaData.getColumnLabel(i));
          table.column(column);
        }

        tables.add(table);
      }
      return tables;
    } catch (SQLException e) {
      throw exceptionWithCause("get table names", e);
    }
  }

  public CreateTable newCreateTable(Table table) {
    return new CreateTable(this, table);
  }

  public DropTable newDropTable(Table table) {
    return new DropTable(this, table);
  }

  public AlterTableAdd newAlterTableAdd(Table table) {
    return new AlterTableAdd(this, table);
  }

  public Select newSelect() {
    return new Select(this);
  }

  /** Shortcut for: select fields ... (from table is automatically added for selectFields that are columns) */
  public Select newSelect(SelectField... selectFields) {
    return new Select(this).fields(selectFields);
  }

  /** Shortcut for: select * from table */
  public Select newSelect(Table table) {
    return new Select(this).fields(table);
  }

  public Update newUpdate(Table table) {
    return newUpdate(table, null);
  }

  public Update newUpdate(Table table, String alias) {
    return new Update(this, table, alias);
  }

  public Insert newInsert(Table table) {
    return new Insert(this, table);
  }

  public Delete newDelete(Table table) {
    return newDelete(table, null);
  }

  public Delete newDelete(Table table, String alias) {
    return new Delete(this, table, alias);
  }

  public PreparedStatement createPreparedStatement(String sql) {
    PreparedStatement statement = null;
    try {
      statement = getConnection()
        .prepareStatement(sql);
    } catch (SQLException e) {
      throw exceptionWithCause("prepare "+getClass().getSimpleName().toUpperCase()+" statement: \n"+sql, e);
    }
    return statement;
  }
}
