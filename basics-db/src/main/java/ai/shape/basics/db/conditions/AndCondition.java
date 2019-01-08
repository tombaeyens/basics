
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

package ai.shape.basics.db.conditions;

import ai.shape.basics.db.Condition;
import ai.shape.basics.db.SqlBuilder;
import ai.shape.basics.db.Statement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class AndCondition implements Condition {

  List<Condition> andConditions;

  public AndCondition(Condition[] andConditions) {
    this.andConditions = new ArrayList<>(Arrays
      .stream(andConditions)
      .filter(condition->condition!=null)
      .collect(Collectors.toList()));
  }

  @Override
  public void buildSql(SqlBuilder sql, Statement statement) {
    Condition first = andConditions.get(0);
    for (Condition andCondition: andConditions) {
      if (andCondition!=first) {
        sql.appendText(" \n   AND ");
      }
      andCondition.buildSql(sql, statement);
    }
  }

  @Override
  public void collectParameters(Statement statement) {
    andConditions.forEach(andCondition->andCondition.collectParameters(statement));
  }

  public void add(Condition andCondition) {
    this.andConditions.add(andCondition);
  }
}
