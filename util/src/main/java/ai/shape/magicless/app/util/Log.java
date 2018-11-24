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
