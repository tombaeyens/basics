### The modified gson library needed

The basics-gson requires a small change to the gson library.

So far the pull request https://github.com/google/gson/pull/1455 is not yet accepted.

In order to build this library, you need to clone the 
master branch in https://github.com/tombaeyens/gson
and build it locally with 

```
mvn clean install
```
