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

public class SqlBuilder {

  Dialect dialect;
  List<Parameter> parameters;
  int nextParameterIndex = 0;
  StringBuilder sql = new StringBuilder();
  StringBuilder sqlLog = new StringBuilder();

  public SqlBuilder(List<Parameter> parameters) {
    this.parameters = parameters;
  }

  public SqlBuilder(Statement statement) {
    this.parameters = statement.parameters;
    this.dialect = statement.getDialect();
  }

  public void buildSqlNew() {
  }

  /** Appends the sqlText to the sql statement appendText */
  public SqlBuilder appendText(String sqlText) {
    sql.append(sqlText);
    sqlLog.append(sqlText);
    return this;
  }

  public SqlBuilder appendParameter() {
    Parameter parameter = parameters.get(nextParameterIndex++);
    DataType type = parameter.getType();
    sql.append(type.getParameterText());
    sqlLog.append(type.getLogText(parameter.getValue()));
    return this;
  }

  public String getSql() {
    return sql.toString();
  }

  public String getSqlLog() {
    return sqlLog.toString();
  }

  public String getDebugInfo() {
    StringBuilder debugInfo = new StringBuilder();
    if (sql.length()>0) {
      debugInfo.append("sql used in statement:\n"+sql.toString()+"\n");
    }
    if (sqlLog.length()>0) {
      debugInfo.append("sql with parameters:\n"+sqlLog.toString()+"\n");
    }
    return debugInfo.toString();
  }

  public Dialect getDialect() {
    return dialect;
  }
}
