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

public class DropTable extends Statement {

  protected Table table;
  protected boolean ifExists;
  protected boolean cascade;

  public DropTable(Tx tx, Table table) {
    super(tx);
    this.table = table;
  }

  public DropTable ifExists() {
    this.ifExists = true;
    return this;
  }

  public DropTable cascade() {
    this.cascade = true;
    return this;
  }

  @Override
  protected void buildSqlOld(SqlBuilder sql) {
    getDialect().buildDropTableSql(sql, this);
  }

  public void execute() {
    executeUpdate();
  }

  protected void logUpdateCount(int updateCount) {
  }

}
