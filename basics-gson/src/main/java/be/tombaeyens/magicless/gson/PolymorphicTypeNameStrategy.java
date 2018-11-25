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
package be.tombaeyens.magicless.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/** represents how the type information is encoded in the JSON
 *
 * WRAPPER_OBJECT -> { "circle" : { "radius": 10 }}
 * typical_but_inferior_type_name_strategy -> { "type": "circle", "radius": 10 }
 */
public interface PolymorphicTypeNameStrategy {

  /** { "circle" : { "radius": 10 }} */
  PolymorphicTypeNameStrategy WRAPPER_OBJECT = new PolymorphicTypeNameStrategy() {
    @Override
    public Object read(JsonReader in, PolymorphicTypeAdapter<?> typeAdapter) throws Exception{
      FieldsReader fieldsReader = new FieldsReader(in, typeAdapter);
      in.beginObject();
      String typeName = in.nextName();
      Object bean = fieldsReader.instantiateBean(typeName);
      in.beginObject();
      fieldsReader.readFields(bean);
      in.endObject();
      in.endObject();
      return bean;
    }
    @Override
    public void write(JsonWriter out, String typeName, PolymorphicTypeAdapter<?> typeAdapter, Object value) throws Exception {
      FieldsWriter fieldsWriter = new FieldsWriter(out, value, typeAdapter);
      out.beginObject();
      out.name(typeName);
      out.beginObject();
      fieldsWriter.writeFields(typeName);
      out.endObject();
      out.endObject();
    }
  };

  Object read(JsonReader in, PolymorphicTypeAdapter<?> typeAdapter) throws Exception;

  void write(JsonWriter out, String typeName, PolymorphicTypeAdapter<?> typeAdapter, Object value) throws Exception;
}
