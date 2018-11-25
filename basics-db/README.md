### Warning

Not finished.  Idea is starting to shape nicely, but it's not at all finished.

### 
This is intended to become a layer on top of JDBC with following features: 

* Support for multiple database dialects: Write readable DDL and SQL in your code and this framework will generate the right SQL for the DB.
* Improved API to create Db and resource-approach to transactions
* Model for representing the DB schema with fluent builders.  This allows the schema to be specified easily as part of the Java codebase.
* DB version control:
  * Creating schema based on the DB schema beans
  * Mechanism for auto-upgrading the db schema on startup (See SchemaManager and SchemaHistory table) 
* Fluent builders for statements like create table, select, update, delete that are based on the Table's and Column's
  * Parameter handling

### Db creation

```
Db db = new Db(new DbConfiguration()
      .url("jdbc:h2:mem:test"));
```

* Db contains a DataSource.
* Database dialect is derived from the url

### Transaction

```java
db.tx(tx->{
  tx.newXxxx()...;
});
```

To rollback the transaction, either call `tx.setRollbackOnly()` or throw an exception out of the lamda.

To get a return text from the transaction, use 

```java
AnyType result = db.tx(tx->{
  tx.setResult(...);
});
```

### Schema upgrade

Locking: In case of cluster deployment, the scenario to bring down all servers, then you can restart 
them all at once.  The first one will lock the schema and upgrade it.  The other nodes will wait 
till the first one completes the upgrade. 

### Status

`tx.newCreateTable()`, `tx.newSelect()`, `tx.newUpdate()` have first implementations.  Pretty much untested.
I was working on the SchemaManager to implement `schemaManger.ensureCurrentSchema()`, which performs schema 
creation or schema upgrade.

