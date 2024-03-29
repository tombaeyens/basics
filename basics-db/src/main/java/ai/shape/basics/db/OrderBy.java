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

import java.util.ArrayList;
import java.util.List;

public class OrderBy {

  List<FieldDirection> fieldDirections = new ArrayList<>();

  public static abstract class FieldDirection {
    SqlExpression expression;
    public FieldDirection(SqlExpression expression) {
      this.expression = expression;
    }

    public SqlExpression getExpression() {
      return expression;
    }

    public abstract boolean isAscending();
  }

  public static class Ascending extends FieldDirection {
    public Ascending(SqlExpression expression) {
      super(expression);
    }
    public boolean isAscending() {
      return true;
    }
  }

  public static class Descending extends FieldDirection {
    public Descending(SqlExpression expression) {
      super(expression);
    }
    public boolean isAscending() {
      return false;
    }
  }

  public void add(FieldDirection fieldDirection) {
    fieldDirections.add(fieldDirection);
  }

  public List<FieldDirection> getFieldDirections() {
    return fieldDirections;
  }

  public boolean isEmpty() {
    return fieldDirections.isEmpty();
  }
}
