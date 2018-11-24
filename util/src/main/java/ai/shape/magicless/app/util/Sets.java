/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.util.HashSet;
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
}
