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

import ai.shape.basics.db.Table;
import ai.shape.basics.db.Column;

/**
 * NOTE TO SELF: this documentation reflects the direction that I want change towards.  Impl doesn't yet match.
 *
 * Supports schema evolutions in a cluster without the need for bringing the cluster down
 * if the developers setParameters the following db upgrade procedure:
 *
 * 1) Create new table/columns
 * 2) Any process can start doing double writing to the old as well as to the new table/columns
 * 3) Ensure that all processes are on the right getWriter version
 * 4) Duplicate the old data (recent updates may already have been done in the new table/columns)
 * 5) Remove the writes to the old table/columns
 * 6) Remove the old table/columns
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
