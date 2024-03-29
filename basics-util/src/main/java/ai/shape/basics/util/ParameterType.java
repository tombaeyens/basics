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

package ai.shape.basics.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ParameterType implements Type, ParameterizedType {

  Type rawType;
  Type[] actualTypeArguments;
  Type ownerType;

  public ParameterType(Type rawType, Type... actualTypeArguments) {
    this.rawType = rawType;
    this.actualTypeArguments = actualTypeArguments;
  }

  public ParameterType ownerType(Type ownerType) {
    this.ownerType = ownerType;
    return this;
  }

  @Override
  public Type[] getActualTypeArguments() {
    return actualTypeArguments;
  }

  @Override
  public Type getRawType() {
    return rawType;
  }

  @Override
  public Type getOwnerType() {
    return ownerType;
  }

  @Override
  public String getTypeName() {
    return rawType.getTypeName() +
      "<" +
      Arrays.stream(actualTypeArguments)
        .map(actualTypeArgument->actualTypeArgument.getTypeName())
        .collect(Collectors.joining(",")) +
      ">";
  }
}
