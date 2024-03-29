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

import ai.shape.com.google.gson.Gson;
import ai.shape.com.google.gson.GsonBuilder;
import ai.shape.com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GsonPolyInterfaceTest {

  static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
    .typeName(new TypeToken<Shape>(){},  "shape")
    .typeName(new TypeToken<Square>(){}, "square")
    .typeName(new TypeToken<Circle>(){}, "circle"))
  .create();

  public interface Shape {
  }

  public static class Square implements Shape {
    int side;
  }

  public static class Circle implements Shape {
    int radius;
  }

  @Test
  public void testPolymorphicSpecificClassReadAsBaseClass() {
    String originalJson = JsonQuotes.quote(
      "{'circle':{" +
      "'radius':5}}");
    Type type = new TypeToken<Shape>(){}.getType();

    Circle circle = gson.fromJson(originalJson, type);

    assertNotNull(circle);
    assertEquals(5, circle.radius);

    String reserializedJson = gson.toJson(circle);
    assertEquals(originalJson, reserializedJson);
  }

}
