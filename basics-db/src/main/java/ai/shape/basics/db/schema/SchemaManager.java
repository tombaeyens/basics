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

import ai.shape.magicless.app.container.Inject;
import ai.shape.magicless.app.util.Time;
import ai.shape.basics.db.Db;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
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

  SchemaUpdate[] updates;

  public SchemaManager db(Db db) {
    this.db = db;
    return this;
  }

  public SchemaManager updates(SchemaUpdate... updates) {
    this.updates = updates;
    return this;
  }

  ////////////////////////////////////////////////////////////
  //
  //  SEE NOTE TO SELF IN SchemaHistoryTable
  //
  ////////////////////////////////////////////////////////////


  /** ENSURE that previously released SchemaUpdates do not change logically
   * (thay may have run, bugfixes are allowed) and that unreleased changes always are
   * appended at the end. */
  public void ensureCurrentSchema() {
    if (!schemaHistoryExists()) {
      createSchemaHistory();
    }
    if (!isSchemaUpToDate()) {
      if (acquireSchemaLock()) {
        upgradeSchema();
        releaseSchemaLock();
      } else {
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          log.debug("Waiting for other node to finish upgrade got interrupted");
        }
      }
    }
  }

  private boolean isSchemaUpToDate() {
    Set<String> dbSchemaUpdates = getDbSchemaUpdates();
    for (SchemaUpdate update: updates) {
      if (!dbSchemaUpdates.contains(update.getId())) {
        return false;
      }
    }
    return true;
  }

  /** Skips locking of the db and assumes that a) no db has been created yet
   * and b) no other servers will attempt to create the schema concurrently.
   * Like eg for test scenarios. */
  public void createSchema() {
    createSchemaHistory();
    for (int updateIndex=0; updateIndex<updates.length; updateIndex++) {
      final int finalUpdateIndex = updateIndex;
      db.tx(tx->{
        updates[finalUpdateIndex].update(tx);
      });
    }
  }

  protected boolean acquireSchemaLock() {
    return db.tx(tx->{
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
  }

  protected boolean releaseSchemaLock() {
    return db.tx(tx->{
      int updateCount = tx.newUpdate(SchemaHistoryTable.TABLE)
        .set(Columns.DESCRIPTION, null)
        .set(Columns.PROCESS, null)
        .where(and(
          equal(Columns.PROCESS, db.getProcess()),
          equal(Columns.TYPE, TYPE_LOCK)))
        .execute();
      if (updateCount>1) {
        throw new RuntimeException("Inconsistent database state: More than 1 version record in schemaHistory table: "+updateCount);
      }
      tx.setResult(updateCount==1);
    });
  }

  protected void upgradeSchema() {
    Set<String> dbSchemaUpdates = getDbSchemaUpdates();
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
  protected boolean schemaHistoryExists() {
    return db.tx(tx->{
      boolean schemaHistoryExists = tx.getTableNames().stream()
        .map(tableName->tableName.toLowerCase())
        .collect(Collectors.toList())
        .contains(SchemaHistoryTable.TABLE.getName());
      tx.setResult(schemaHistoryExists);
    });
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

  /** The SchemaUpdate IDs that already have been applied on the DB schema */
  protected Set<String> getDbSchemaUpdates() {
    return db.tx(tx->{
      tx.setResult(
        tx.newSelect(Columns.ID)
          .where(equal(Columns.TYPE, TYPE_UPDATE))
          .execute()
          .stream()
          .map(selectResults->selectResults.get(Columns.ID))
          .collect(Collectors.toSet()));
    });
  }
}
