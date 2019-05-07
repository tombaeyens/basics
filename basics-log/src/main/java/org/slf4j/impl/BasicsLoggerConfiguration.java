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
import java.util.*;

public class BasicsLoggerConfiguration {

  static BasicsLoggerConfiguration current = BasicsLoggerConfiguration.builder()
    .debug("ai.shape")
    .redirectStdErrToStdOut()
    .destination(new BasicsLogDestinationStd(System.out))
    .apply();

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    BasicsLoggerConfiguration configuration = new BasicsLoggerConfiguration();
    Map<String,Integer> levelsByPrefix = new HashMap<>();
    PrintStream originalStdOut;
    PrintStream originalStdErr;
    List<BasicsLogDestination> logDestinations = new ArrayList<>();

    public Builder trace(String prefix) {
      return level(prefix, BasicsLogger.LOG_LEVEL_TRACE);
    }
    public Builder debug(String prefix) {
      return level(prefix, BasicsLogger.LOG_LEVEL_DEBUG);
    }
    public Builder info(String prefix) {
      return level(prefix, BasicsLogger.LOG_LEVEL_INFO);
    }
    public Builder warning(String prefix) {
      return level(prefix, BasicsLogger.LOG_LEVEL_WARN);
    }
    public Builder error(String prefix) {
      return level(prefix, BasicsLogger.LOG_LEVEL_ERROR);
    }
    public Builder level(String prefix, int level) {
      levelsByPrefix.put(prefix, level);
      return null;
    }
    public Builder defaultLevel(int level) {
      configuration.defaultLevel = level;
      return this;
    }

    /** Warning: This method has tradeoffs.  It prevents some synchronization issues between e.printStackTrace()
     * (which writes to System.err) and logging that often writes to System.out.  It also makes that logs show up
     * in the same color in de IDE.  But there is no distinction any more between std err en out and you have
     * to ensure (or hope) that no component has obtained the System.err before. */
    public Builder redirectStdErrToStdOut() {
      originalStdOut = System.out;
      originalStdErr = System.err;
      System.setErr(System.out);
      return this;
    }

    private Builder destination(BasicsLogDestination logDestination) {
      logDestinations.add(logDestination);
      return this;
    }

    public BasicsLoggerConfiguration apply() {
      configuration.apply(levelsByPrefix);
      current = configuration;
      return configuration;
    }
  }

  private String[] configuredPrefixesLongestFirst;
  private Map<String, Integer> levelsByPrefix;
  private int defaultLevel = BasicsLogger.LOG_LEVEL_TRACE;
  private List<BasicsLogDestination> destinations = new ArrayList<>();

  public void apply(Map<String, Integer> levelsByPrefix) {
    Set<String> prefixesLongestFirstSet = new TreeSet<>(new Comparator<String>() {
      @Override
      public int compare(String left, String right) {
        return left.length()-right.length();
      }
    });
    this.configuredPrefixesLongestFirst = prefixesLongestFirstSet.toArray(new String[prefixesLongestFirstSet.size()]);
    this.levelsByPrefix = levelsByPrefix;

    for (BasicsLogger logger: BasicsLoggerFactoryInvisible.loggers.values()) {
      initializeLevel(logger);
    }
  }

  void initializeLevel(BasicsLogger logger) {
    int level = getConfiguredLevel(logger.getName());
    logger.setLevel(level);
  }

  int getConfiguredLevel(String loggerName) {
    for (String configuredPrefix: configuredPrefixesLongestFirst) {
      if (loggerName.startsWith(configuredPrefix)) {
        return levelsByPrefix.get(configuredPrefix);
      }
    }
    return defaultLevel;
  }

  static BasicsLoggerConfiguration get() {
    return current;
  }

  public int getDefaultLevel() {
    return defaultLevel;
  }

  public void log(BasicsLogger basicsLogger, int level, String message, Throwable t) {
    for (BasicsLogDestination destination: destinations) {
      destination.log(basicsLogger, level, message, t);
    }
  }
}
