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

import ai.shape.basics.db.conditions.*;

import java.util.List;

public interface Condition {

  public static Condition equal(SelectField column, Object value) {
    return new EqualCondition(column, value);
  }

  public static Condition isNull(Column column) {
    return new IsNullCondition(column);
  }

  public static Condition and(Condition... andConditions) {
    return new AndCondition(andConditions);
  }

  public static Condition or(Condition... andConditions) {
    return new OrCondition(andConditions);
  }

  public static Condition like(Column column, String pattern) {
    return new LikeCondition(column, pattern);
  }

  public static Condition gte(Column column, Object value) {
    return new GreaterThanOrEqualCondition(column, value);
  }

  public static Condition in(Column column, List<?> values) {
    return new InCondition(column, values);
  }

  public static Condition notNull(Column column) {
    return new NotNullCondition(column);
  }

  void buildSql(SqlBuilder sql, Statement statement);

  void collectParameters(Statement statement);
}
