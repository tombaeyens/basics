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

import java.net.URL;

public class Resources {

  public static void main(String[] args) {
    System.out.println(getCodeSource(Resources.class));
  }

  public static URL getCodeSourceLocation(Class<?> clazz) {
    return clazz.getProtectionDomain().getCodeSource().getLocation();
  }

  /** For example
   * file:/Code/shape/basics/basics-util/target/classes/
   * file:/Code/shape/basics/basics-util/target/basics-util-1.0.0-SNAPSHOT.jar */
  public static String getCodeSource(Class<?> clazz) {
    return getCodeSourceLocation(clazz).toString();
  }

  /** For example
   * /Code/shape/basics/basics-util/target/classes/
   * /Code/shape/basics/basics-util/target/basics-util-1.0.0-SNAPSHOT.jar */
  public static String getCodeSourceFile(Class<?> clazz) {
    return getCodeSourceLocation(clazz)
      .toString()
      .substring(5);
  }

  /** Returns the directory without the end part.
   * If the directory does not end on the given end, null is returned.
   * Eg when getCodeSourceFile(c)} returns /Code/shape/basics/basics-util/target/classes/,
   * getCodeSourceFileWithoutEnd(c, "target/classes/") returns /Code/shape/basics/basics-util/ */
  public static String getCodeSourceFileWithoutEnd(Class<?> clazz, String end) {
    String dir = getCodeSourceFile(clazz);
    if (dir.endsWith(end)) {
      return dir.substring(0, dir.length()-end.length());
    }
    return null;
  }

  /** Same as {@link #getCodeSourceFileWithoutEnd(Class, String)} but throws
   * a RuntimeException if the code source file dir does not end with the given end.*/
  public static String getCodeSourceFileWithoutEndRequired(Class<?> clazz, String end) {
    String dir = getCodeSourceFile(clazz);
    if (!dir.endsWith(end)) {
      throw new RuntimeException("Code source "+dir+" does not end with "+end);
    }
    return dir.substring(0, dir.length()-end.length());
  }
}