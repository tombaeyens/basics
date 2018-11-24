/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */

package ai.shape.magicless.app.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Lists {

  @SafeVarargs
  public static <T> List<T> of(T... elements) {
    return arrayList(elements);
  }

  @SafeVarargs
  public static <T> List<T> arrayList(T... elements) {
    if (elements==null) {
      return null;
    }
    List<T> list = new ArrayList<>(elements.length);
    for (int i=0; i<elements.length; i++) {
      list.add(elements[i]);
    }
    return list;
  }

  public static <T> T removeLast(List<T> list) {
    return list.remove(list.size()-1);
  }

  /** the last element or null if the list is null or empty */
  public static <T> T getLast(List<T> list) {
    return list!=null && !list.isEmpty() ? list.get(list.size()-1) : null;
  }

  /** replaces the last element with the given element if the list
   * is not null and not empty.  The method returns without exception
   * and without effect if the list is null or empty */
  public static <T> T setLast(List<T> list, T element) {
    if (list!=null && !list.isEmpty()) {
      return list.set(list.size()-1, element);
    }
    return null;
  }

  /** Returns a new ArrayList in reverse order */
  public static <T> List<T> reverse(List<T> source) {
    ArrayList<T> reverseList = new ArrayList<>(source);
    Collections.reverse(reverseList);
    return reverseList;
  }
}
