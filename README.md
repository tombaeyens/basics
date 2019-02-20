### Build preparation

To build, you first need to install our modified version of the gson library

```
mvn install:install-file -Dfile=../basics/basics-gson/src/main/gson-customized/gson-2.8.6-SNAPSHOT.jar -DgroupId=com.google.code.gson -DartifactId=gson -Dversion=2.8.6-SNAPSHOT -Dpackaging=jar
```

So far our pull request https://github.com/google/gson/pull/1455 is not yet accepted.
The pull request also serves as documentation on what changed.

### Build

```
mvn clean install
```

As a result of the build, the jars are added to your local repo and available in the 
target directories.
