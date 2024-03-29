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

import java.util.Arrays;

public class Strings {

  public static String generate(char c, int multiplier) {
    char[] chars = new char[multiplier];
    Arrays.fill(chars, 0, multiplier, c);
    return new String(chars);
  }

  public static boolean isNonEmpty(String string) {
    return string!=null && string.length()>0;
  }

  public static String removeEnd(String string, String end) {
    if (string==null || end==null) {
      return string;
    }
    return string.substring(0, string.length()-end.length());
  }
}
