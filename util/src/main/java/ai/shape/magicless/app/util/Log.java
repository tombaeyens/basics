/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.StringReader;

public class Log {

  /** To be called before any logging is called */
  public static void directSimpleLoggerToStandardOut() {
    System.setProperty("org.slf4j.simpleLogger.logFile", "System.out");
    System.setErr(System.out);
  }

  public static void logLines(Logger logger, String multiLineMessage) {
    logLines(logger, multiLineMessage, null);
  }

  public static void logLines(Logger logger, String multiLineMessage, String linePrefix) {
    BufferedReader reader = new BufferedReader(new StringReader(multiLineMessage));
    reader
      .lines()
      .forEach(line -> logger.debug(linePrefix!=null ? linePrefix+line : line));
  }

  public static void logExceptionInRed(Throwable exception) {
    System.out.print("\033[1;91m");
    exception.printStackTrace();
    System.out.print("\u001B[0m");
  }
}
