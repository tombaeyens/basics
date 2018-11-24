/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */

package ai.shape.magicless.app.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Url {

  public static String encodeUtf8(String text) {
    try {
      return URLEncoder.encode(text, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw Exceptions.exceptionWithCause("encode "+text, e);
    }

  }
}
