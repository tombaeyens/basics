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

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ai.shape.basics.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GsonPolyTypePropertyTest {

  // @formatter:off
  static Gson gson = new GsonBuilder().registerTypeAdapterFactory(new PolymorphicTypeAdapterFactory()
    .typePropertyName("type")
    .typeName(new TypeToken<Shape>() {}, "shape")
    .typeName(new TypeToken<Square>() {}, "square")
    .typeName(new TypeToken<Circle>() {}, "circle"))
  .create();
  // @formatter:on

  public static class Shape {
    String color;
    List<Shape> neighbours;
  }

  public static class Square extends Shape {
    Shape nestedShape;
  }

  public static class Circle extends Shape {
  }

  @Test
  public void testPolymorphicSpecificClassReadAsBaseClassWithTypeProperty() {
    String originalJson = JsonQuotes.quote(
      "{'type':'circle',"+
      "'color':'green'" +
      "}");
    Type type = new TypeToken<Shape>() {}.getType();
    Circle circle = gson.fromJson(originalJson, type);
    assertNotNull(circle);
    assertEquals("green", circle.color);
    String reserializedJson = gson.toJson(circle);
    assertEquals(originalJson, reserializedJson);
  }

  @Test
  public void testTypePropertyAtTheEnd() {
    String originalJson = JsonQuotes.quote(
      "{'color':'green',"+
        "'type':'circle'}");
    Type type = new TypeToken<Shape>() {}.getType();
    Circle circle = gson.fromJson(originalJson, type);
    assertNotNull(circle);
    assertEquals("green", circle.color);
    String reserializedJson = gson.toJson(circle);
    // Reserializing will put the type as the first property
    assertEquals(JsonQuotes.quote(
      "{'type':'circle',"+
      "'color':'green'}"),
      reserializedJson);
  }

  @Test
  public void testTypePropertyObjectCaching() {
    String originalJson = JsonQuotes.quote(
      "{ 'color':'green',"+
        "  'nestedShape':{" +
        "    'color':'blue'," +
        "    'nestedShape':{" +
        "      'color':'rainbow'," +
        "      'type':'circle'" +
        "    }," +
        "    'type':'square'" +
        "  }," +
        "  'type':'square'" +
        "}");

    Type type = new TypeToken<Shape>() {}.getType();
    Square square = gson.fromJson(originalJson, type);
    assertNotNull(square);
    assertEquals("green", square.color);

    Square nestedSquare = (Square) square.nestedShape;
    assertNotNull(nestedSquare);
    assertEquals("blue", nestedSquare.color);

    Circle nestedCircle = (Circle) nestedSquare.nestedShape;
    assertNotNull(nestedCircle);
    assertEquals("rainbow", nestedCircle.color);

    String reserializedJson = gson.toJson(square);
    // Reserializing will put the type as the first property
    assertEquals(JsonQuotes.quote(
      "{" +
      "'type':'square'," +
      "'color':'green',"+
      "'nestedShape':{" +
        "'type':'square'," +
        "'color':'blue'," +
        "'nestedShape':{" +
          "'type':'circle'," +
          "'color':'rainbow'" +
         "}" +
       "}" +
     "}"), reserializedJson);
  }

  @Test
  public void testTypePropertyMinimalArray() {
    String originalJson = JsonQuotes.quote(
      "" +
        "{ 'neighbours':[]," +
        "  'type':'square'" +
        "}");
    Type type = new TypeToken<Shape>() {}.getType();
    Square square = gson.fromJson(originalJson, type);
  }

  @Test
  public void testTypePropertyArrayCaching() {
    String originalJson = JsonQuotes.quote(
      "" +
        "{ 'color':'green',"+
        "  'neighbours':[ " +
        "    { 'color':'blue'," +
        "      'neighbours': [" +
        "        { 'color':'rainbow'," +
        "          'type':'circle'" +
        "        }" +
        "      ]," +
        "      'type':'square'" +
        "    } " +
        "  ]," +
        "  'type':'square'" +
        "}");

    Type type = new TypeToken<Shape>() {}.getType();
    Square square = gson.fromJson(originalJson, type);
    assertNotNull(square);
    assertEquals("green", square.color);

    Square nestedSquare = (Square) square.neighbours.get(0);
    assertNotNull(nestedSquare);
    assertEquals("blue", nestedSquare.color);

    Circle nestedCircle = (Circle) nestedSquare.neighbours.get(0);
    assertNotNull(nestedCircle);
    assertEquals("rainbow", nestedCircle.color);

    String reserializedJson = gson.toJson(square);
    // Reserializing will put the type as the first property
    assertEquals(JsonQuotes.quote(
      "" +
        "{'type':'square'," +
         "'color':'green'," +
         "'neighbours':[" +
           "{'type':'square'," +
            "'color':'blue'," +
            "'neighbours':[" +
              "{'type':'circle'," +
               "'color':'rainbow'}]}]}"), reserializedJson);
  }
}
