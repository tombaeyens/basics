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
package ai.shape.basics.db.schema;

import ai.shape.basics.db.Column;
import ai.shape.basics.db.Table;

/**
 * Contains the schema lock record and one record per {@link SchemaUpdate}
 * that was performed on a database.
 */
public class SchemaHistoryTable extends Table {

  public static final SchemaHistoryTable TABLE = new SchemaHistoryTable();

  private SchemaHistoryTable() {
    name("schemaHistory");
    columns(Columns.class);
  }

  interface Columns {
    Column ID = new Column()
      .name("id")
      .typeVarchar(1024)
      .primaryKey();

    Column DESCRIPTION = new Column()
      .name("description")
      .typeVarchar(1024);

    Column TIME = new Column()
      .name("time")
      .typeTimestamp();

    Column PROCESS = new Column()
      .name("process")
      .typeVarchar(255);

    Column TYPE = new Column()
      .name("type")
      .typeVarchar(1024);
  }

}
