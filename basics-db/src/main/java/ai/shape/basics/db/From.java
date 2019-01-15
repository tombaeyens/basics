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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class From {

  protected Table table;
  protected List<Join> joins;

  public From(Table table) {
    this.table = table;
  }

  public Table getTable() {
    return this.table;
  }
  public void setTable(Table table) {
    this.table = table;
  }
  public From table(Table table) {
    this.table = table;
    return this;
  }

  public List<Join> getJoins() {
    return this.joins;
  }
  public void setJoins(List<Join> joins) {
    this.joins = joins;
  }
  public From join(Join join) {
    if (joins==null) {
      joins = new ArrayList<>();
    }
    joins.add(join);
    return this;
  }

  public void collectTables(List<Table> fromTables) {
    if (table!=null) {
      fromTables.add(table);
    }
    if (joins!=null) {
      fromTables.addAll(joins
        .stream()
        .map(join->join.getTable())
        .collect(Collectors.toList()));
    }
  }
}
