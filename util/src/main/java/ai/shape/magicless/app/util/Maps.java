/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/** Usage:
 *
 * hashMap(
 *   entry("2360", "Oud-Turnhout");
 *   entry("3021 HN", "Rotterdam");
 * ); */
public class Maps {

  public static class Entry<K,V> {
    K key;
    V value;
    public Entry(K key, V value) {
      this.key = key;
      this.value = value;
    }
  }

  @SafeVarargs
  public static <K,V> HashMap<K,V> hashMap(Entry<K,V>... entries) {
    return buildMap(new HashMap<K,V>(), entries);
  }

  @SafeVarargs
  public static <K,V> LinkedHashMap<K,V> linkedHashMap(Entry<K,V>... entries) {
    return buildMap(new LinkedHashMap<K,V>(), entries);
  }

  @SafeVarargs
  public static <K,V> TreeMap<K,V> treeMap(Entry<K,V>... entries) {
    return buildMap(new TreeMap<K,V>(), entries);
  }

  @SafeVarargs
  static <K, V, M extends Map<K,V>> M buildMap(M map, Entry<K,V>... entries) {
    if (entries!=null) {
      for (Entry<K,V> entry: entries) {
        map.put(entry.key, entry.value);
      }
    }
    return map;
  }

  public static <K,V> Entry<K,V> entry(K key, V value) {
    return new Entry<K,V>(key, value);
  }
}
