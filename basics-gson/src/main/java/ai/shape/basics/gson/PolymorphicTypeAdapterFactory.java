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

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.*;

public class PolymorphicTypeAdapterFactory implements TypeAdapterFactory {

  Map<TypeToken<?>, String> typeNames = new LinkedHashMap<>();
  PolymorphicTypeNameStrategy typeNameStrategy = PolymorphicTypeNameStrategy.WRAPPER_OBJECT;

  Set<TypeToken<?>> matchingTypes = new HashSet<>();
  PolymorphicTypeAdapter<?> typeAdapter = null;
  Map<Class<?>,Constructor<?>> accessibleConstructors = Collections.synchronizedMap(new HashMap<>());

  public PolymorphicTypeAdapterFactory typeName(Type type, String name) {
    return typeName(TypeToken.get(type), name);
  }

  public PolymorphicTypeAdapterFactory typeNamesByClass(Map<Type,String> typeNames) {
    typeNames.forEach((type,typeName)->typeName(type, typeName));
    return this;
  }

  public PolymorphicTypeAdapterFactory typesNamesByName(Map<String,Type> typeNames) {
    typeNames.forEach((typeName,type)->typeName(type, typeName));
    return this;
  }

  public PolymorphicTypeAdapterFactory typeName(TypeToken<?> type, String name) {
    typeNames.put(type, name);

    matchingTypes.add(type);
    Class<?> rawClass = type.getRawType();
    TypeToken<?> rawType = TypeToken.get(rawClass);
    if (!rawType.equals(type)) {
      matchingTypes.add(rawType);
    }

    return this;
  }

  /** When specified, the {@link TypePropertyStrategy} {"type":"typeName", ...fields...} is used
   * instead of the default wrapped object strategy {"typeName":{ ...fields...}} */
  public PolymorphicTypeAdapterFactory typePropertyName(String typePropertyName) {
    this.typeNameStrategy = new TypePropertyStrategy(typePropertyName);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
    // TODO check if GSON does caching of the created TypeAdapter for the given type

    // extra caching could be added in this layer if there is only one polymorphic type
    // adapter for the whole hierarchy

    // https://google.github.io/gson/apidocs/com/google/gson/TypeAdapterFactory.html
    // If a factory cannot support a given type, it must return null when that type is passed to create(com.google.gson.Gson, com.google.gson.reflect.TypeToken<T>)

    if (type.getType() instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) type.getType();
      Type[] upperBounds = wildcardType.getUpperBounds();
      if (upperBounds!=null && upperBounds.length==1) {
        type = (TypeToken<T>) TypeToken.get(upperBounds[0]);
      } else {
        throw new RuntimeException("Unsupported wildcard type: "+type);
      }
    }
    if (matchingTypes.contains(type)) {
      if (typeAdapter==null) {
        typeAdapter = new PolymorphicTypeAdapter<T>(type, this, gson);
      }
      return (TypeAdapter<T>) this.typeAdapter;
    }

    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> Constructor<T> getAccessibleConstructor(Class<T> clazz) {
    Constructor<?> constructor = accessibleConstructors.get(clazz);
    if (constructor!=null) {
      return (Constructor<T>) constructor;
    }
    try {
      constructor = clazz.getDeclaredConstructor((Class<?>[]) null);
      constructor.setAccessible(true);
      accessibleConstructors.put(clazz, constructor);
      return (Constructor<T>) constructor;
    } catch (NoSuchMethodException e) {
      throw new RuntimeException("Couldn't get constructor for "+clazz, e);
    }
  }
}
