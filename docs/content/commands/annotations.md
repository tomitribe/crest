---
title: "Annotations"
description: "The core annotations for defining commands, options, defaults, and required parameters in Crest."
weight: 1
---

## @Command

The `@Command` annotation marks a method as a CLI command. The method name becomes the command name by default, or you can specify a custom name.

```java
@Command
public String greet(@Option("name") @Default("World") final String name) {
    return "Hello, " + name;
}
```

CLI usage: `greet --name Alice`

### Custom Command Names

Use the `value` parameter to override the default method name. This is especially useful when the desired command name is a Java reserved word:

```java
@Command("import")
public void _import(@Option("file") final File file) {
    // "import" is a reserved word, so the method is named _import
}
```

### Descriptions

Provide a one-line description that appears in help listings:

```java
@Command(description = "Deploy the application to the target environment")
public void deploy(@Option("target") final String target) { ... }
```

### Custom Usage

Override the auto-generated synopsis line with the `usage` parameter:

```java
@Command(value = "commit", usage = "commit [options] <message> <file>")
public void commit(@Option("all") final boolean all,
                   final String message,
                   final File file) { ... }
```

### Class-Level @Command

When placed on a class, `@Command` defines a command group. All `@Command`-annotated methods inside the class become sub-commands. See [Command Groups]({{< ref "command-groups" >}}) for details.

```java
@Command(value = "config", description = "Manage configuration")
public class ConfigCommands {

    @Command(description = "Set a config value")
    public void set(@Option("key") final String key,
                    @Option("value") final String value) { ... }

    @Command(description = "Get a config value")
    public void get(@Option("key") final String key) { ... }
}
```

CLI usage: `config set --key db.host --value localhost`

## @Option

Marks a method parameter as a named CLI option. Parameters without `@Option` are treated as positional arguments.

```java
@Command
public void upload(@Option("customer-id") final String customerId,
                   @Option("dry-run") @Default("false") final boolean dryRun,
                   final URI source) { ... }
```

CLI usage: `upload --customer-id acme --dry-run /data`

In this example, `customerId` and `dryRun` are named options, while `source` is a positional argument.

### Aliases

Provide multiple names for the same option by passing an array:

```java
@Command
public void copy(@Option({"f", "force"}) final boolean force,
                 @Option({"r", "recursive"}) final boolean recursive,
                 final URI source,
                 final URI dest) { ... }
```

CLI usage: `copy --force --recursive /src /dest` or `copy -f -r /src /dest`

### Inline Description

Add a description directly in the annotation for help output:

```java
@Command
public void commit(@Option(value = "all", description = "commit all changed files") final boolean all,
                   @Option(value = "message", description = "commit message") final String message) { ... }
```

## @Default

Provides a default value for an option when the user does not supply one. The string is converted to the parameter's type using Crest's type conversion.

```java
@Command
public void connect(@Option("host") @Default("localhost") final String host,
                    @Option("port") @Default("5432") final int port,
                    @Option("ssl") @Default("true") final boolean ssl) { ... }
```

CLI usage: `connect` uses all defaults; `connect --port 3306` overrides only the port.

### Variable Substitution

Default values support system property and environment variable substitution using `${...}` syntax:

```java
@Command
public void deploy(@Option("owner") @Default("${user.name}") final String owner,
                   @Option("region") @Default("${AWS_REGION}") final String region,
                   @Option("target") final String target) { ... }
```

At runtime, `${user.name}` resolves to the system property `user.name`, and `${AWS_REGION}` resolves to the environment variable `AWS_REGION`.

## @Required

Enforces that an option must be provided by the user. If the option is missing, the framework throws a validation error with a descriptive message.

```java
@Command
public void register(@Option("email") @Required final String email,
                     @Option("name") @Required final String name,
                     @Option("newsletter") @Default("false") final boolean newsletter) { ... }
```

CLI usage: `register --email user@example.com --name "Jane Doe"` succeeds; `register --name "Jane Doe"` fails with a missing `--email` error.

`@Required` and `@Default` are mutually exclusive in practice -- if a parameter has a default, it does not need to be required.
