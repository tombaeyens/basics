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
package be.tombaeyens.magicless;

import be.tombaeyens.magicless.db.Tx;
import be.tombaeyens.magicless.db.schema.SchemaUpdate;
import be.tombaeyens.magicless.tables.Users;

public class CreateUserTable implements SchemaUpdate {

  @Override
  public String getId() {
    return "create-users-table";
  }

  @Override
  public void update(Tx tx) {
    tx.newCreateTable(Users.TABLE).execute();
  }
}
