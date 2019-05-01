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

import ai.shape.com.google.gson.stream.JsonReader;
import ai.shape.com.google.gson.stream.JsonWriter;

public class TypePropertyStrategy implements PolymorphicTypeNameStrategy {

  String typePropertyName;

  public TypePropertyStrategy(String typePropertyName) {
    this.typePropertyName = typePropertyName;
  }

  @Override
  public Object read(JsonReader in, PolymorphicTypeAdapter<?> typeAdapter) throws Exception {
    TypePropertyJsonReader typeIn = new TypePropertyJsonReader(in, typePropertyName);
    FieldsReader fieldsReader = new FieldsReader(typeIn, typeAdapter);
    in.beginObject();
    String typeName = typeIn.readTypeName();
    Object bean = fieldsReader.instantiateBean(typeName);
    fieldsReader.readFields(bean);
    in.endObject();
    return bean;
  }

  @Override
  public void write(JsonWriter out, String typeName, PolymorphicTypeAdapter<?> typeAdapter, Object value) throws Exception {
    FieldsWriter fieldsWriter = new FieldsWriter(out, value, typeAdapter);
    out.beginObject();
    out.name(typePropertyName);
    out.value(typeName);
    fieldsWriter.writeFields(typeName);
    out.endObject();
  }
}
