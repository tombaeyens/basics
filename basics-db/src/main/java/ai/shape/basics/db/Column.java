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

import ai.shape.basics.db.constraints.ForeignKey;
import ai.shape.basics.db.constraints.NotNull;
import ai.shape.basics.db.constraints.PrimaryKey;
import ai.shape.basics.db.types.*;

import java.util.ArrayList;
import java.util.List;

public class Column implements FieldExpression {

  protected Table table;
  protected String name;
  protected DataType type;
  protected List<Constraint> constraints;
  /** index in the list of table columns */
  protected int index;

  @Override
  public void collectTables(List<Table> fieldTables) {
    fieldTables.add(table);
  }

  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public Column name(String name) {
    this.name = name;
    return this;
  }

  public DataType getType() {
    return this.type;
  }

  public void setType(DataType type) {
    this.type = type;
  }
  public Column type(DataType type) {
    this.type = type;
    return this;
  }
  public Column typeVarchar(int n) {
    type(new VarcharType(n));
    return this;
  }
  public Column typeClob() {
    type(new ClobType());
    return this;
  }
  public Column typeJson() {
    type(new JsonType());
    return this;
  }
  public Column typeInteger() {
    type(new IntegerType());
    return this;
  }
  public Column typeLong() {
    type(new LongType());
    return this;
  }
  public Column typeDouble() {
    type(new DoubleType());
    return this;
  }
  public Column typeFloat() {
    type(new FloatType());
    return this;
  }
  public Column typeTimestamp() {
    type(new TimestampType());
    return this;
  }
  public Column typeBoolean() {
    type(new BooleanType());
    return this;
  }

  public List<Constraint> getConstraints() {
    return this.constraints;
  }
  public void setConstraints(List<Constraint> constraints) {
    this.constraints = constraints;
  }
  public Column constraint(Constraint constraint) {
    if (constraints==null) {
      constraints = new ArrayList<>();
    }
    constraints.add(constraint);
    return this;
  }

  public Column primaryKey() {
    constraint(new PrimaryKey());
    return this;
  }

  public Column foreignKey(Column column) {
    constraint(new ForeignKey(this, column));
    return this;
  }

  public Column notNull() {
    constraint(new NotNull());
    return this;
  }

  @Override
  public String getTitle() {
    return getName();
  }

  @Override
  public void appendFieldSql(SqlBuilder sql, Statement statement) {
    sql.appendText(statement.getQualifiedColumnName(this));
  }

  public Table getTable() {
    return table;
  }

  public int getIndex() {
    return index;
  }

  public boolean isPrimaryKey() {
    if (constraints!=null) {
      for (Constraint constraint: constraints) {
        if (constraint instanceof PrimaryKey) {
          return true;
        }
      }
    }
    return false;
  }

  public ForeignKey findForeignKey() {
    if (constraints!=null) {
      for (Constraint constraint: constraints) {
        if (constraint instanceof ForeignKey) {
          return (ForeignKey)constraint;
        }
      }
    }
    return null;
  }

  @Override
  public String toString() {
    return "Column(" + name + ')';
  }
}
