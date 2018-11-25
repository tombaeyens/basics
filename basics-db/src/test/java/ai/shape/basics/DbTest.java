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
package ai.shape.basics;

import ai.shape.basics.db.Db;
import ai.shape.basics.db.DbConfiguration;
import ai.shape.basics.db.schema.SchemaManager;
import ai.shape.basics.tables.User;
import ai.shape.basics.tables.Users;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DbTest {

  static Logger log = LoggerFactory.getLogger(DbTest.class);

  @Test
  public void testDb() throws Exception {
    Db db = new Db(new DbConfiguration()
      .url("jdbc:h2:mem:test"));

    SchemaManager schemaManager = new SchemaManager()
      .db(db)
      .updates(new CreateUserTable());

    schemaManager.ensureCurrentSchema();

    db.tx(tx-> {
      Users.insertUser(tx, new User()
        .id(UUID.randomUUID().toString())
        .firstName("Tom")
        .email("tom@shape.ai")
      );
    });

    db.tx(tx-> {
      log.debug("Deleting all "+Users.findAllUsers(tx).count()+" users");
      tx.newDelete(Users.TABLE).execute();
    });

    // Add drop db to schema manager (which delegates to the dialect)
  }
}
