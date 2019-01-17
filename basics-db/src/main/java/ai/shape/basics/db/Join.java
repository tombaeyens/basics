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

public class Join {

  public static final String TYPE_DEFAULT_INNER = "";
  public static final String TYPE_LEFT_OUTER = "LEFT OUTER";
  public static final String TYPE_RIGHT_OUTER = "LEFT OUTER";

  protected Table table;
  protected String type;
  protected Condition on;

  public void collectTables(List<Table> fromTables) {
    fromTables.add(table);
  }

  public Condition getOn() {
    return this.on;
  }
  public void setOn(Condition on) {
    this.on = on;
  }

  public Join on(Condition on) {
    this.on = on;
    return this;
  }

  public Table getTable() {
    return this.table;
  }
  public void setTable(Table table) {
    this.table = table;
  }
  public Join table(Table table) {
    this.table = table;
    return this;
  }

  public String getType() {
    return this.type;
  }
  public void setType(String type) {
    this.type = type;
  }
  public Join type(String type) {
    this.type = type;
    return this;
  }
}
