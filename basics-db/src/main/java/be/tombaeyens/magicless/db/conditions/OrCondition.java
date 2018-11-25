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
package be.tombaeyens.magicless.db.conditions;

import be.tombaeyens.magicless.db.Condition;
import be.tombaeyens.magicless.db.SqlBuilder;
import be.tombaeyens.magicless.db.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OrCondition implements Condition {

  List<Condition> orConditions;

  public OrCondition(Condition[] orConditions) {
    this.orConditions = new ArrayList<>(Arrays.asList(orConditions));
  }

  @Override
  public void buildSql(SqlBuilder sql, Statement statement) {
    Condition first = orConditions.get(0);
    sql.appendText("( ");
    for (Condition andCondition: orConditions) {
      if (andCondition!=first) {
        sql.appendText(" \n   OR ");
      }
      andCondition.buildSql(sql, statement);
    }
    sql.appendText(" ) \n");
  }

  @Override
  public void collectParameters(Statement statement) {
    orConditions.forEach(orCondition->orCondition.collectParameters(statement));
  }

  public void add(Condition orCondition) {
    this.orConditions.add(orCondition);
  }
}
