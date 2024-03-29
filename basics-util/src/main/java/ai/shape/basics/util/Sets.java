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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Sets {

  @SafeVarargs
  public static <T> Set<T> hashSet(T... elements) {
    HashSet<T> set = new HashSet<>();
    if (elements!=null) {
      for (T element: elements) {
        set.add(element);
      }
    }
    return set;
  }

  public static boolean isNotEmpty(Collection<?> collection) {
    return collection!=null && !collection.isEmpty();
  }

  public static boolean isNotEmpty(Map<?,?> map) {
    return map!=null && !map.isEmpty();
  }

  @SafeVarargs
  public static <T> Set<T> flatHashSet(Set<T>... sets) {
    HashSet<T> flatSets = new HashSet<>();
    if (sets!=null) {
      for (Set<T> set: sets) {
        flatSets.addAll(set);
      }
    }
    return flatSets;
  }
}
