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

package ai.shape.basics.gson;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypeNames {

  protected Map<String,Type> typesByName = new HashMap<>();
  protected Map<Type,String> namesByType = new HashMap<>();

  public TypeNames typeName(Type type, String typeName) {
    if (typesByName.containsKey(typeName)) {
      throw new RuntimeException("typeName "+typeName+" already exists");
    }
    typesByName.put(typeName, type);
    namesByType.put(type, typeName);
    return this;
  }

  public Map<String, Type> getTypesByName() {
    return typesByName;
  }

  public Map<Type, String> getNamesByType() {
    return namesByType;
  }

  public Type getType(String typeName) {
    return typesByName.get(typeName);
  }

  public String getName(Type type) {
    return namesByType.get(type);
  }
}
