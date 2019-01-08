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

import java.util.Properties;

public class Configuration {

  private Properties properties = new Properties();

  /** Loads configuration properties from resource file.
   * Overwrites existing configuration properties. */
  public void loadConfigurationFromResource(String resource) {
    Io.loadPropertiesFromResource(this.properties, resource);
  }

  /** Loads configuration properties from file.
   * Overwrites existing configuration properties. */
  public void loadConfigurationFromFile(String fileName) {
    Io.loadPropertiesFromFile(this.properties, fileName);
  }

  /** Loads the System.getProperties().
   * Overwrites existing configuration properties */
  public void loadConfigurationFromSystemProperties() {
    this.properties.putAll(System.getProperties());
  }

  public void loadConfigurationFromProperties(Properties properties) {
    this.properties.putAll(properties);
  }

  public String getString(String name) {
    return getString(name, null, false);
  }

  public String getString(String name, String defaultValue) {
    return getString(name, defaultValue, false);
  }


  public String getStringRequired(String name) {
    return getString(name, null, true);
  }

  private String getString(String name, String defaultValue, boolean required) {
    String value = properties.getProperty(name);
    if (value==null || "".equals(value)) {
      if (required) {
        throw new RuntimeException("Configuration property "+name+" is required");
      } else {
        return defaultValue;
      }
    }
    return value;
  }

  public Integer getIntegerRequired(String name) {
    return getInteger(name, null, true);
  }

  public Integer getInteger(String name) {
    return getInteger(name, null, false);
  }

  public Integer getInteger(String name, Integer defaultValue) {
    return getInteger(name, defaultValue, false);
  }

  private Integer getInteger(String name, Integer defaultValue, boolean required) {
    String textValue = getString(name, null, required);
    if (textValue==null || "".equals(textValue)) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(textValue);
    } catch (NumberFormatException e) {
      throw new RuntimeException("Invalid number value (integer) for configuration "+name+": "+textValue);
    }
  }

  public Configuration put(String name, String value) {
    properties.put(name, value);
    return this;
  }

  public Configuration remove(String name) {
    properties.remove(name);
    return this;
  }
}
