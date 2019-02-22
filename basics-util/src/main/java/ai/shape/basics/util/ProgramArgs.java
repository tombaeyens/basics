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

import java.util.*;
import java.util.stream.Collectors;

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
          Option option = syntax.getOption(arg);
          if (option!=null) {
            options.put(option.getShortName(), "");
            options.put(option.getLongName(), "");
            currentOption = option;
          } else {
            nonOptions.add(arg);
          }
        } else {
          options.put(currentOption.getShortName(), arg);
          options.put(currentOption.getLongName(), arg);
          currentOption = null;
        }
      }
    }
  }

  public Map<String, String> getOptions() {
    return options;
  }

  public String getOption(String name) {
    if (!options.containsKey(name)) {
      throw new RuntimeException("Option "+name+" is required:\n"+Arrays.toString(args)+"\n"+syntax.getOptionDocs("  "));
    }
    return options.get(name);
  }

  public String getOption(String name, String defaultValue) {
    return options.containsKey(name) ? options.get(name) : defaultValue;
  }

  public List<String> getNonOptions() {
    return nonOptions;
  }

  /** throws RuntimeException if the option value is a string that cannot be parsed as an integer with Integer.parseInt() */
  public int getOptionInt(String name, int defaultValue) {
    String valueString = getOption(name, null);
    if (valueString!=null) {
      try {
        return Integer.parseInt(valueString);
      } catch (NumberFormatException e) {
        throw new RuntimeException("Invalid integer value for option "+name+": "+valueString);
      }
    } else {
      return defaultValue;
    }
  }

  public static class Syntax {
    Map<String,Option> optionsByShortName = new HashMap<>();
    Map<String,Option> optionsByLongName = new HashMap<>();
    List<Option> options = new ArrayList<>();
    public Syntax option(String shortName, String longName, String description) {
      Option option = new Option()
        .shortName(shortName)
        .longName(longName)
        .description(description);
      if (shortName!=null) {
        optionsByShortName.put(shortName, option);
      }
      if (longName!=null) {
        optionsByLongName.put(longName, option);
      }
      options.add(option);
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
            && arg.length()>1) {
          String shortName = arg.substring(1);
          return optionsByShortName.get(shortName);
        }
      }
      return null;
    }
    public ProgramArgs parse(String[] args) {
      return new ProgramArgs(this, args);
    }

    public String getOptionDocs() {
      return getOptionDocs("");
    }
    public String getOptionDocs(String prefix) {
      return options.stream()
        .map(option->prefix+option.toDoc())
        .collect(Collectors.joining("\n"));
    }
  }

  private static class Option {
    protected String shortName;
    protected String longName;
    protected String description;

    public String getDescription() {
      return this.description;
    }
    public void setDescription(String description) {
      this.description = description;
    }
    public Option description(String description) {
      this.description = description;
      return this;
    }


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

    public String toDoc() {
      return Lists.of(
        (shortName!=null ? "-"+shortName : null),
        (longName!=null ? "--"+longName : null),
        description)
        .stream()
        .filter(field -> field!=null)
        .collect(Collectors.joining(" "));
    }
  }

  public static Syntax syntax() {
    return new Syntax();
  }

  public String getOptionDocs() {
    return syntax.getOptionDocs();
  }
}
