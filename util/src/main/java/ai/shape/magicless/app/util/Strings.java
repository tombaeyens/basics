/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.util.Arrays;

public class Strings {

  public static String generate(char c, int multiplier) {
    char[] chars = new char[multiplier];
    Arrays.fill(chars, 0, multiplier, c);
    return new String(chars);
  }
}
