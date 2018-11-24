/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.time.LocalDateTime;

public class Time {

  static LocalDateTime now = null;

  public static LocalDateTime now() {
    return now!=null ? now : LocalDateTime.now();
  }

  /** to be used by tests */
  public static void setNow(LocalDateTime now) {
    Time.now = now;
  }
}
