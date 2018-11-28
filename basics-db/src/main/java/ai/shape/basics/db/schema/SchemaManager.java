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
package ai.shape.basics.db.schema;

import ai.shape.basics.db.AlterTableAdd;
import ai.shape.basics.db.Column;
import ai.shape.basics.db.Db;
import ai.shape.basics.db.Table;
import ai.shape.magicless.app.container.Initialize;
import ai.shape.magicless.app.container.Inject;
import ai.shape.magicless.app.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ai.shape.basics.db.Condition.*;
import static ai.shape.basics.db.schema.SchemaHistoryTable.Columns;

public class SchemaManager {

  static Logger log = LoggerFactory.getLogger(SchemaManager.class);

  public static final String ID_LOCK = "lock";

  /** Single record with a known id (version) to ensure that contains the version and
   * is used to lock the db so that only one process updates the schema at a time */
  public static final String TYPE_LOCK = "lock";

  /** Logs when a {@link SchemaUpdate} is performed */
  public static final String TYPE_UPDATE = "update";

  @Inject
  protected Db db;

  List<Table> tables = new ArrayList<>();
  List<SchemaUpdate> updates = new ArrayList<>();

  public SchemaManager db(Db db) {
    this.db = db;
    return this;
  }

  public SchemaManager tables(Table... tables) {
    for (Table table: tables) {
      if (table!=null) {
        this.tables.add(table);
      }
    }
    // If db was set manually (not with a container)
    if (db!=null) {
      // Then trigger the initialization here.
      initialize();
    }
    return this;
  }

  public SchemaManager updates(SchemaUpdate... updates) {
    for (SchemaUpdate update: updates) {
      if (update!=null) {
        this.updates.add(update);
      }
    }
    return this;
  }

  @Initialize
  public void initialize() {
    tables.forEach(table -> db.getDialect().initializeTable(table));
  }

  /** Creates all the tables without any checks in the same order as they are passed in {@link #tables(Table...)}.  To be used in tests. */
  public void createSchema() {
    db.tx(tx->{
      for (Table table: tables) {
        tx.newCreateTable(table).execute();
      }
    });
  }

  /** Drops all the tables without any checks in reverse order as they are passed in {@link #tables(Table...)}.  To be used in tests. */
  public void dropSchema() {
    db.tx(tx->{
      for (int i=tables.size()-1; i>=0; i--) {
        tx.newDropTable(tables.get(i)).execute();
      }
    });
  }

  /** ENSURE that previously released SchemaUpdates do not change logically
   * (thay may have run, bugfixes are allowed) and that unreleased changes always are
   * appended at the end. */
  public void ensureCurrentSchema() {
    Map<String, Table> metaDataTablesByNameLowerCase = getMetaDataTables()
      .stream()
      .collect(Collectors.toMap(
        table->table.getName().toLowerCase(),
        table->table
      ));
    if (!schemaHistoryExists(metaDataTablesByNameLowerCase)) {
      createSchemaHistory();
    }
    if (acquireSchemaLock()) {
      try {
        upgradeSchema(metaDataTablesByNameLowerCase);
      } finally {
        releaseSchemaLock();
      }
    } else {
      throw new RuntimeException("Couldn't acquire schema upgrade lock");
    }
  }

  protected List<Table> getMetaDataTables() {
    return db.tx(tx->{
      tx.setResult(tx.getMetaDataTables());
    });
  }

  protected boolean schemaHistoryExists(Map<String, Table> metaDataTablesByNameLowerCase) {
    return metaDataTablesByNameLowerCase.containsKey(SchemaHistoryTable.TABLE
      .getName()
      .toLowerCase());
  }

  protected void createSchemaHistory() {
    db.tx(tx->{
      tx.newCreateTable(SchemaHistoryTable.TABLE).execute();
      tx.newInsert(SchemaHistoryTable.TABLE)
        .set(Columns.ID, ID_LOCK)
        .set(Columns.TYPE, TYPE_LOCK)
        .execute();
    });
  }

  protected boolean acquireSchemaLock() {
    int maxAttempts = 5;
    long millisBetweenAttempts = 1000;

    int attempts = 0;
    boolean lockAcquired = false;

    while (!lockAcquired && attempts<maxAttempts) {
      log.debug("Attempt "+(attempts+1)+" to lock the schema");
      lockAcquired = db.tx(tx->{
        int updateCount = tx.newUpdate(SchemaHistoryTable.TABLE)
          .set(Columns.DESCRIPTION, db.getProcess() + " is upgrading schema")
          .set(Columns.PROCESS, db.getProcess())
          .where(and(
            isNull(Columns.DESCRIPTION),
            isNull(Columns.PROCESS),
            equal(Columns.TYPE, TYPE_LOCK)))
          .execute();
        if (updateCount>1) {
          throw new RuntimeException("Inconsistent database state: More than 1 version record in schemaHistory table: "+updateCount);
        }
        tx.setResult(updateCount==1);
      });

      attempts++;

      if (lockAcquired) {
        log.debug("Schema lock was acquired");
      } else {
        if (attempts<maxAttempts) {
          try {
            log.debug("Another process has locked the schema.  Waiting " + millisBetweenAttempts + " milliseconds before retrying");
            Thread.sleep(millisBetweenAttempts);
          } catch (InterruptedException e) {
            log.debug("Waiting for other node to finish upgrade got interrupted");
          }
        }
      }
    }

    return lockAcquired;
  }

  protected boolean releaseSchemaLock() {
    Boolean lockReleased = db.tx(tx -> {
      int updateCount = tx.newUpdate(SchemaHistoryTable.TABLE)
        .set(Columns.DESCRIPTION, null)
        .set(Columns.PROCESS, null)
        .where(and(
          equal(Columns.PROCESS, db.getProcess()),
          equal(Columns.TYPE, TYPE_LOCK)))
        .execute();
      if (updateCount > 1) {
        throw new RuntimeException("Inconsistent database state: More than 1 version record in schemaHistory table: " + updateCount);
      }
      tx.setResult(updateCount == 1);
    });
    if (Boolean.TRUE.equals(lockReleased)) {
      log.debug("Schema lock was released");
    } else {
      throw new RuntimeException("Schema lock could not be released");
    }
    return lockReleased;
  }

  protected void upgradeSchema(Map<String, Table> metaDataTablesByNameLowerCase) {
    db.tx(tx->{
      for (Table table: this.tables) {
        String tableNameLowerCase = table.getName().toLowerCase();
        Table metaDataTable = metaDataTablesByNameLowerCase.get(tableNameLowerCase);

        if (metaDataTable!=null) {
          Map<String, Column> metaDataColumnsByNameLowerCase = metaDataTable
            .getColumns()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
              entry->entry.getKey().toLowerCase(),
              entry->entry.getValue()
            ));

          AlterTableAdd alterTableAdd = tx.newAlterTableAdd(table);

          for (Column column: table.getColumns().values()) {
            String columnNameLowerCase = column.getName().toLowerCase();
            Column metaDataColumn = metaDataColumnsByNameLowerCase.get(columnNameLowerCase);
            if (metaDataColumn==null) {
              alterTableAdd.add(column);
            }
          }

          if (alterTableAdd.getColumns().size()>0) {
            alterTableAdd.execute();
          }

        } else {
          tx.newCreateTable(table).execute();
        }
      }
    });

    List<String> dbSchemaUpdates = getDbSchemaUpdates();
    for (SchemaUpdate update: updates) {
      if (!dbSchemaUpdates.contains(update.getId())) {
        db.tx(tx->{
          update.update(tx);

          int updateCount = tx.newInsert(SchemaHistoryTable.TABLE)
            .set(Columns.ID, update.getId())
            .set(Columns.TIME, Time.now())
            .set(Columns.PROCESS, db.getProcess())
            .set(Columns.TYPE, TYPE_UPDATE)
            .set(Columns.DESCRIPTION, "Executed update " + update.getId())
            .execute();
          if (updateCount!=1) {
            throw new RuntimeException("Expected 1 insert of update "+update.getId());
          }
        });
      }
    }
  }

  /** The SchemaUpdate IDs that already have been applied on the DB schema */
  protected List<String> getDbSchemaUpdates() {
    return db.tx(tx->{
      tx.setResult(
        tx.newSelect(Columns.ID)
          .where(equal(Columns.TYPE, TYPE_UPDATE))
          .execute()
          .getAll(selectResults->selectResults.get(Columns.ID)));
    });
  }

  public List<Table> getTables() {
    return tables;
  }

  public List<SchemaUpdate> getUpdates() {
    return updates;
  }
}
