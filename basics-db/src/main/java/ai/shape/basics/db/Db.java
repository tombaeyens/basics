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
package ai.shape.basics.db;

import ai.shape.basics.db.dialects.H2Dialect;
import ai.shape.basics.db.dialects.MySQLDialect;
import ai.shape.basics.db.dialects.PostgreSQLDialect;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static ai.shape.basics.util.Exceptions.exceptionWithCause;

public class Db {

  public static final Logger DB_LOGGER = LoggerFactory.getLogger(Db.class);

  public static final String PROPERTY_NAME_JDBC_URL = "jdbcUrl";
  public static final String PROPERTY_NAME_USER = "user";
  public static final String PROPERTY_NAME_PASSWORD = "password";
  public static final String PROPERTY_NAME_PROCESS_REF = "process.ref";

  protected DataSource dataSource;
  protected Dialect dialect;
  protected String processRef;

  /**
   * For docs see https://github.com/brettwooldridge/HikariCP
   *
   * From https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration :
   *
   * A typical MySQL configuration for HikariCP might look something like this:
   *
   * jdbcUrl=jdbc:mysql://localhost:3306/simpsons
   * user=test
   * password=test
   * dataSource.cachePrepStmts=true
   * dataSource.prepStmtCacheSize=250
   * dataSource.prepStmtCacheSqlLimit=2048
   * dataSource.useServerPrepStmts=true
   * dataSource.useLocalSessionState=true
   * dataSource.rewriteBatchedStatements=true
   * dataSource.cacheResultSetMetadata=true
   * dataSource.cacheServerConfiguration=true
   * dataSource.elideSetAutoCommits=true
   * dataSource.maintainTimeStats=false
   */
  public Db(Properties properties) {
    try {
      DB_LOGGER.debug("Creating Db "+properties.getProperty(PROPERTY_NAME_JDBC_URL));
      DB_LOGGER.debug("Creating Db "+properties);
      this.dataSource = createDataSource(properties);
      this.dialect = getDialect(properties);
      this.processRef = initializeProcessRef(properties);

    } catch (Exception e) {
      throw exceptionWithCause("create Db with properties "+properties, e);
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    Properties properties = new Properties();
    public Builder property(String name, String value) {
      if (name!=null && value!=null) {
        properties.setProperty(name, value);
      }
      return this;
    }
    public Db build() {
      return new Db(properties);
    }
  }

  protected Dialect getDialect(Properties properties) {
    String jdbcUrl = properties.getProperty(PROPERTY_NAME_JDBC_URL);
    String dbType = getDbTypeTextFromUrl(jdbcUrl);
    if (dbType.contains("h2")) {
      return H2Dialect.INSTANCE;
    } else if (dbType.contains("postgresql")) {
      return PostgreSQLDialect.INSTANCE;
    } else if (dbType.contains("mysql")) {
      return MySQLDialect.INSTANCE;
    }
    throw new RuntimeException("Database type "+dbType+" not supported "+ PROPERTY_NAME_JDBC_URL +"="+jdbcUrl+" ");
  }

  protected String getDbTypeTextFromUrl(String url) {
    // Calculate the dialect from the url
    if (url.startsWith("jdbc:") && url.length()>6) {
      int endIndex = url.indexOf(":", 5);
      if (endIndex!=-1) {
        return url.substring(5, endIndex);
      }
    }
    return null;
  }

  private DataSource createDataSource(Properties properties) {
    DB_LOGGER.debug("Creating Hikari DataSource with configuration "+properties);
    HikariConfig hikariConfig = new HikariConfig(properties);
    return new HikariDataSource(hikariConfig);
  }

  protected String initializeProcessRef(Properties properties) {
    String processRef = properties.getProperty(PROPERTY_NAME_PROCESS_REF);
    if (processRef==null) {
      processRef = ManagementFactory.getRuntimeMXBean().getName();
      if (processRef==null) {
        try {
          processRef = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
          processRef = "unnamed node";
        }
      }
    }
    return processRef;
  }

  @SuppressWarnings("unchecked")
  public <T> T tx(TxLogic txLogic) {
    Connection connection = null;
    Tx tx = null;
    Exception exception = null;
    try {
      connection = dataSource.getConnection();
      connection.setAutoCommit(false);
      tx = new Tx(this, connection);
      txLogic.execute(tx);
    } catch (Exception e) {
      exception = e;
      if (tx!=null) {
        tx.setRollbackOnly(e);
      }
    }
    if (tx!=null) {
      tx.end();
    }
    if (connection!=null) {
      try {
        connection.close();
      } catch (SQLException e) {
        DB_LOGGER.error("Tx connection close: " + e.getMessage(), e);
      }
    }
    if (exception!=null) {
      if (exception instanceof RuntimeException) {
        throw (RuntimeException) exception;
      } else {
        throw new RuntimeException("Transaction failed: "+exception.getMessage(), exception);
      }
    }
    return tx!=null ? (T) tx.getResult() : null;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public Dialect getDialect() {
    return dialect;
  }

  public String getProcess() {
    return processRef;
  }
}
