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
package be.tombaeyens.magicless.tables;

import be.tombaeyens.magicless.db.Column;
import be.tombaeyens.magicless.db.SelectResults;
import be.tombaeyens.magicless.db.Table;
import be.tombaeyens.magicless.db.Tx;

import java.util.stream.Stream;

public class Users extends Table {

  public static final Column ID = new Column()
    .name("id")
    .typeVarchar(1024)
    .primaryKey();

  public static final Column FIRST_NAME = new Column()
    .name("firstName")
    .typeVarchar(1024);

  public static final Column LAST_NAME = new Column()
    .name("lastName")
    .typeVarchar(1024);

  public static final Column EMAIL = new Column()
    .name("email")
    .typeVarchar(1024);

  public static final Users TABLE = new Users();

  private Users() {
    name("users");
    column(ID);
    column(FIRST_NAME);
    column(LAST_NAME);
    column(EMAIL);
  }

  public static Stream<User> findAllUsers(Tx tx) {
    return tx.newSelect(TABLE)
      .execute()
      .stream()
      .map(Users::createUser);
  }

  private static User createUser(SelectResults selectResults) {
    return new User()
      .id(selectResults.get(ID))
      .firstName(selectResults.get(FIRST_NAME))
      .lastName(selectResults.get(LAST_NAME))
      .email(selectResults.get(EMAIL));
  }

  public static void insertUser(Tx tx, User user) {
    tx.newInsert(TABLE)
      .set(ID, user.getId())
      .set(FIRST_NAME, user.getFirstName())
      .set(LAST_NAME, user.getLastName())
      .set(EMAIL, user.getEmail())
      .execute();
  }
}
