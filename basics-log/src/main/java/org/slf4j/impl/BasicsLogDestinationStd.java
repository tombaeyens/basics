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

package org.slf4j.impl;

import java.io.PrintStream;
import java.util.Date;

public class BasicsLogDestinationStd implements BasicsLogDestination {

  /** Typically System.out or System.err */
  PrintStream stream;

  public BasicsLogDestinationStd(PrintStream stream) {
    this.stream = stream;
  }


  @Override
  public void log(BasicsLogger basicsLogger, int level, String message, Throwable t) {
    stream.println(message);


//    StringBuilder buf = new StringBuilder(32);
//
//    // Append date-time if so configured
//    if (CONFIG_PARAMS.showDateTime) {
//      if (CONFIG_PARAMS.dateFormatter != null) {
//        buf.append(getFormattedDate());
//        buf.append(' ');
//      } else {
//        buf.append(System.currentTimeMillis() - START_TIME);
//        buf.append(' ');
//      }
//    }
//
//    // Append current thread name if so configured
//    if (CONFIG_PARAMS.showThreadName) {
//      buf.append('[');
//      buf.append(Thread.currentThread().getName());
//      buf.append("] ");
//    }
//
//    if (CONFIG_PARAMS.levelInBrackets)
//      buf.append('[');
//
//    // Append a readable representation of the log level
//    String levelStr = renderLevel(level);
//    buf.append(levelStr);
//    if (CONFIG_PARAMS.levelInBrackets)
//      buf.append(']');
//    buf.append(' ');
//
//    // Append the name of the log instance if so configured
//    if (CONFIG_PARAMS.showShortLogName) {
//      if (shortLogName == null)
//        shortLogName = computeShortName();
//      buf.append(String.valueOf(shortLogName)).append(" - ");
//    } else if (CONFIG_PARAMS.showLogName) {
//      buf.append(String.valueOf(name)).append(" - ");
//    }
//
//    // Append the message
//    buf.append(message);
//
//    write(buf, t);
  }

//  protected String renderLevel(int level) {
//    switch (level) {
//      case LOG_LEVEL_TRACE:
//        return "TRACE";
//      case LOG_LEVEL_DEBUG:
//        return "DEBUG";
//      case LOG_LEVEL_INFO:
//        return "INFO ";
//      case LOG_LEVEL_WARN:
//        return "WARN ";
//      case LOG_LEVEL_ERROR:
//        return "ERROR";
//    }
//    throw new IllegalStateException("Unrecognized level [" + level + "]");
//  }
//
//  void write(StringBuilder buf, Throwable t) {
//    PrintStream targetStream = CONFIG_PARAMS.outputChoice.getTargetPrintStream();
//
//    targetStream.println(buf.toString());
//    writeThrowable(t, targetStream);
//    targetStream.flush();
//  }
//
//  protected void writeThrowable(Throwable t, PrintStream targetStream) {
//    if (t != null) {
//      t.printStackTrace(targetStream);
//    }
//  }
//
//  private String getFormattedDate() {
//    Date now = new Date();
//    String dateText;
//    synchronized (CONFIG_PARAMS.dateFormatter) {
//      dateText = CONFIG_PARAMS.dateFormatter.format(now);
//    }
//    return dateText;
//  }
//
//  private String computeShortName() {
//    return name.substring(name.lastIndexOf(".") + 1);
//  }

}
