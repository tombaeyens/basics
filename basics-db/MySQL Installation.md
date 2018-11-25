Log of my MySQL 5.7.23 installation

https://dev.mysql.com/downloads/file/?id=479114

Installation gives a root password in a popup.
That password is expired so you have to change it.

Start & stop the MySQL server on mac can be done in the System Preferences

Open a console with 
```
/usr/local/mysql/bin/mysql -u root -p
```

To change the password:
```sql
SET PASSWORD = PASSWORD('***');
```

I also found this on the web which didn't work on my MySQL 5.7:
```sql
alter user 'root@localhost' IDENTIFIED BY '***';
```

Next, create the shape test user:
```sql
CREATE USER 'shapetest'@'%' IDENTIFIED BY 'shapetest';
GRANT ALL PRIVILEGES ON *.* TO 'shapetest'@'%' WITH GRANT OPTION;
```

Create a /etc/my.cnf file with 
```sql
[mysqld]
sql-mode="NO_ENGINE_SUBSTITUTION"
default_time_zone="+00:00"
```

Reboot the server in the System Preferences

Next open a sql console with user shapetest

```sql
/usr/local/mysql/bin/mysql -u shapetest -p
```

And create the shape database

```sql
CREATE DATABASE shape;
```

