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

import java.util.*;

/** basic program arguments parsing
 * Usage:
 *
 * ProgramArgs programArgs = ProgramArgs.syntax()
 *   .option("i", "import")
 *   .option("e", "export")
 *   .parse(args);
 *
 * program --import -e myexport one two three
 *
 * programArgs.getOption("i")      -> "" (ProgramArgs.NO_VALUE)
 * programArgs.getOption("import") -> "" (ProgramArgs.NO_VALUE)
 * programArgs.getOption("e")      -> "myexport"
 * programArgs.getOption("export") -> "myexport"
 * programArgs.getNonOptions()     -> ["one", "two", "three"] (List<String>)
 */
public class ProgramArgs {

  public static final String NO_VALUE = "";

  Syntax syntax;
  String[] args;
  Map<String, String> options = new LinkedHashMap<>();
  List<String> nonOptions = new ArrayList<>();

  public ProgramArgs(Syntax syntax, String[] args) {
    this.syntax = syntax;
    this.args = args;

    Option currentOption = null;
    if (args!=null) {
      for (int i=0; i<args.length; i++) {
        String arg = args[i];
        if (currentOption==null) {
          options.put(currentOption.getShortName(), arg);
          options.put(currentOption.getLongName(), arg);
          currentOption = null;
        } else {
          Option option = syntax.getOption(arg);
          if (option!=null) {
            options.put(currentOption.getShortName(), "");
            options.put(currentOption.getLongName(), "");
            currentOption = option;
          } else {
            nonOptions.add(arg);
          }
        }
      }
    }
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public String getOption(String name, String defaultValue) {
    return options.containsKey(name) ? options.get(name) : defaultValue;
  }

  public List<String> getNonOptions() {
    return nonOptions;
  }

  public static class Syntax {
    Map<String,Option> optionsByShortName = new HashMap<>();
    Map<String,Option> optionsByLongName = new HashMap<>();
    public Syntax option(String shortName, String longName) {
      Option option = new Option()
        .shortName(shortName)
        .longName(longName);
      if (shortName!=null) {
        optionsByShortName.put(shortName, option);
      }
      if (longName!=null) {
        optionsByLongName.put(longName, option);
      }
      return this;
    }
    Option getOption(String arg) {
      if (arg!=null) {
        if (arg.startsWith("--")
            && arg.length()>2) {
          String longName = arg.substring(2);
          return optionsByLongName.get(longName);
        }
        if (arg.startsWith("-")
            && arg.length()>2) {
          String shortName = arg.substring(2);
          return optionsByShortName.get(shortName);
        }
      }
      return null;
    }
    public ProgramArgs parse(String[] args) {
      return new ProgramArgs(this, args);
    }
  }

  private static class Option {
    protected String shortName;
    protected String longName;

    public String getLongName() {
      return this.longName;
    }
    public void setLongName(String longName) {
      this.longName = longName;
    }
    public Option longName(String longName) {
      this.longName = longName;
      return this;
    }

    public String getShortName() {
      return this.shortName;
    }
    public void setShortName(String shortName) {
      this.shortName = shortName;
    }
    public Option shortName(String shortName) {
      this.shortName = shortName;
      return this;
    }

  }

  public static Syntax syntax() {
    return new Syntax();
  }
}
