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

import ai.shape.magicless.app.util.Reflection;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

public class Table {

  protected String name;
  protected Map<String,Column> columns;

  public Table() {
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Table name(String name) {
    this.name = name;
    return this;
  }

  public Map<String,Column> getColumns() {
    return this.columns;
  }
  public Table column(Column column) {
    if (columns==null) {
      columns = new LinkedHashMap<>();
    }
    column.table = this;
    column.index = columns.size();
    columns.put(column.getName(), column);
    return this;
  }

  public Table columns(Class<?> columnsClass) {
    for (Field field: columnsClass.getDeclaredFields()) {
      Object object = Reflection.getFieldValue(field,null);
      if (object instanceof Column) {
        column((Column)object);
      }
    }
    return this;
  }

  public Column getPrimaryKeyColumn() {
    for (Column column: columns.values()) {
      if (column.isPrimaryKey()) {
        return column;
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "Table(" +name + ")";
  }
}
