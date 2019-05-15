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

import java.util.List;

import static ai.shape.basics.util.Exceptions.assertNotNull;

public class CreateTableSqlColumn extends SqlDelegator {

  public CreateTableSqlColumn(SqlBuilder target) {
    super(target);
  }

  public void append(Column column) {
    sqlSeparator(",");
    sql2("\n  ");

    assertNotNull(column, "column %d is null", column.getIndex());

    DataType type = column.getType();
    assertNotNull(type, "column.type is null for column with index %d", column.getIndex());

    sql2(column.getName() + " " + getDialect().getTypeSql(type));

    List<Constraint> constraints = column.getConstraints();
    if (constraints != null) {
      CreateTableSqlColumnConstraint constraintSqlAppender = getDialect().getCreateTableColumnConstraintSqlAppender(target);
      constraints.forEach(constraint -> constraintSqlAppender.append(constraint));
    }
  }
}
