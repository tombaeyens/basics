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

package ai.shape.magicless.app.util;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Map;

/**
 * Convenience methods for throwing runtime exceptions.
 */
public class Exceptions {

  /** throws a RuntimeException with message String.format(message,messageArgs) if the condition is false. */
  public static void assertTrue(boolean condition, String message, Object... messageArgs) {
    if (!condition) {
      throw new RuntimeException(String.format(message, (Object[]) messageArgs));
    }
  }

  /** throws a RuntimeException with message String.format(message,messageArgs) if o is null. */
  public static void assertNotNull(Object o, String message, Object... messageArgs) {
    if (o==null) {
      throw new RuntimeException(String.format(message, (Object[]) messageArgs));
    }
  }

  /** throws a RuntimeException with message "Parameter "+parameterName+" is null" if o is null. */
  public static void assertNotNullParameter(Object o, String parameterName) {
    if (o==null) {
      throw new RuntimeException("Parameter "+parameterName+" is null");
    }
  }

  /** usage: throw newRuntimeException("describe what was being done", e);
   * returns a new RuntimeException with message "Couldn't "+whatWasBeingDone+": "+ exception.getMessage().
   * If exception is null, the exception message is not added. */
  public static RuntimeException exceptionWithCause(String whatWasBeingDone, Throwable exception) {
    return new RuntimeException("Couldn't "+whatWasBeingDone+(exception!=null ? ": "+ exception.getMessage() : ""), exception);
  }

  public static void assertNotEmptyCollection(Collection<?> collection, String name) {
    assertNotNullParameter(collection, name);
    if (collection.isEmpty()) {
      throw new RuntimeException("Collection "+name+" is empty");
    }
  }

  public static void assertNotEmptyArray(Object[] array, String name) {
    assertNotNullParameter(array, name);
    if (array.length==0) {
      throw new RuntimeException("Collection "+name+" is empty");
    }
  }

  public static void assertNotEmptyMap(Map<?,?> map, String name) {
    assertNotNullParameter(map, name);
    if (map.size()==0) {
      throw new RuntimeException("Map "+name+" is empty");
    }
  }

  public static void assertSame(Object o1, Object o2, String message, String... messageArgs) {
    if (o1!=o2) {
      throw new RuntimeException(String.format(message, (Object[]) messageArgs));
    }
  }
}
