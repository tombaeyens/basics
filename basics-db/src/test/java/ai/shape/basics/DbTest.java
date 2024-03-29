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
import ai.shape.basics.db.schema.SchemaManager;
import ai.shape.basics.tables.UsersDao;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbTest {

  static Logger log = LoggerFactory.getLogger(DbTest.class);

  @Test
  public void testDb() throws Exception {
    Db db = Db.builder()
      .property(Db.CONFIGURATION_NAME_JDBC_URL, "jdbc:h2:mem:test")
      .build();

    // creates the schema history
    log.debug("Schema manager 1: creating the schema history table");
    new SchemaManager()
      .db(db)
      .ensureCurrentSchema();

    // creates the users table V1
    log.debug("Schema manager 2: creating the users table");
    new SchemaManager()
      .db(db)
      .tables(UsersDao.TABLE_V1)
      .ensureCurrentSchema();

    // adds columns to the users table
    log.debug("Schema manager 3: adding columns to the users table");
    new SchemaManager()
      .db(db)
      .tables(UsersDao.TABLE_V2)
      .ensureCurrentSchema();
  }
}
