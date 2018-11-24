/*
 * Copyright (c) 2018 Shape.ai - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited. Proprietary and confidential.
 * Written by Tom Baeyens <tom@shape.ai>, 2018
 */
package ai.shape.magicless.app.util;

import java.util.Properties;

public class Configuration {

  private Properties properties = new Properties();

  /** Loads configuration properties from resource file.
   * Overwrites existing configuration properties. */
  public void loadConfigurationFromResource(String resource) {
    Io.loadPropertiesFromResource(properties, resource);
  }

  /** Loads configuration properties from file.
   * Overwrites existing configuration properties. */
  public void loadConfigurationFromFile(String fileName) {
    Io.loadPropertiesFromFile(properties, fileName);
  }

  /** Loads the System.getProperties().
   * Overwrites existing configuration properties */
  public void loadConfigurationFromSystemProperties() {
    properties.putAll(System.getProperties());
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
