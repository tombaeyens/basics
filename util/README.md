### Purpose

A library to parse program arguments

### Usage

When passing program arguments like this:

``` 
java -jar myjar.jar -port 4567 -env dev -disabledFeatures login preview
```

You can parse the values like this:

```
public static void main(String[] args) {
  try {
    Configuration configuration = new Options()
      .usage("java -jar myjar.jar <options>")
      .option(new Option()
        .names("verbose", "v")
        .description("More detailed logging.")) // no valueType means it's a boolean flag option without text
      .option(new Option()
        .names("port", "p")
        .description("The port on which the service is bound.")
        .valueInteger(0, 65000)
        .defaultValue("8080")) // specifying a default text implies a text is optional
      .option(new Option()
        .name("env")
        .description("The environment.")
        .valueEnum("dev", "test", "prod")) // No default text means a text is required.
      .option(new Option()
        .name("disabledFeatures")
        .description("Disables all specified features.")
        .valueMultiEnum("search", "login", "preview")) // Allows multiple values
      .parse(args)
      .assertNoErrors(); // throws runtime exception with syntax description in case of problems

    boolean verbose = configuration.has("verbose");
    int port = configuration.get("port");
    String env = configuration.get("env");
    List<String> disabledFeatures = configuration.getList("disabledFeatures");

    // ...

  } catch (ConfigurationException e) {
    // This will print the error(s) and the complete option syntax documentation
    System.out.println(e.getMessage());
  }
}
```

### Features

* Fluent interface to specify options
* Parsing args, generating error messages and tips to resolve the problem
* Options can be configured to have no text, an optional text, exactly one text and multiple values
* Parsing text values to java types
* Documentation generation
* Password masking in error messages

### Example error message

```
Configuration errors:
Invalid text 45bb67 for -port,-p with type integer[0..65000]

Configurations:
-port 45bb67 -env dev -disabledFeatures login preview

Configuration properties:
-verbose,-v         Option flag without values.
-port,-p            The port on which the service is bound. A integer[0..65000] text is optional. Default text is 8080
-env                The environment. A enum[dev,test,prod] text is optional.
-disabledFeatures   Disables all specified features. Zero or more enum[search,login,preview] values can be specified.
```