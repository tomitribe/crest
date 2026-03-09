---
title: "Command Groups"
description: "Organize commands into groups and sub-commands using class-level @Command annotations."
weight: 3
---

As a CLI tool grows, a flat list of commands becomes hard to navigate. Crest supports command groups -- a parent command that contains sub-commands -- using the class-level `@Command` annotation.

## Defining a Command Group

Place `@Command` on the class itself to define a group. The class-level `value` becomes the group name, and each `@Command`-annotated method inside becomes a sub-command.

```java
@Command(value = "config", description = "Manage configuration")
public class ConfigCommands {

    @Command(description = "Add a new config value")
    public void add(final Name name,
                    @Required @Option("value") final String value) { ... }

    @Command(description = "Remove a config value")
    public void remove(final Name name) { ... }

    @Command("import")
    public void _import(final Config config,
                        @Option("file") final File file) { ... }
}
```

CLI usage:

```
config add --value="myvalue" myname
config remove myname
config import --file=settings.json
```

Help output:

```
Usage: config [subcommand] [options]

Sub commands:

   add      Add a new config value
   import
   remove   Remove a config value
```

## Multiple Classes Contributing to a Group

Multiple classes can contribute sub-commands to the same group. Use the same `@Command` value on each class:

```java
@Command(value = "user", description = "User management")
public class UserCommands {

    @Command(description = "Create a new user")
    public void create(@Option("name") @Required final String name,
                       @Option("email") @Required final String email) { ... }

    @Command(description = "Delete a user")
    public void delete(@Option("id") @Required final String id) { ... }
}

@Command("user")
public class UserQueryCommands {

    @Command(description = "List all users")
    public void list(@Option("filter") final String filter) { ... }

    @Command(description = "Show user details")
    public void show(@Option("id") @Required final String id) { ... }
}
```

Both classes contribute to the `user` group. The resulting CLI has four sub-commands: `user create`, `user delete`, `user list`, and `user show`.

## Description Resolution

When multiple classes contribute to the same group, the group description comes from whichever class has a non-empty `@Command(description)`. All contributing classes are checked, so it does not matter which class is registered first.

In the example above, `UserCommands` provides the description `"User management"`, while `UserQueryCommands` does not specify one. The group description resolves to `"User management"` regardless of registration order.

If multiple classes provide a description for the same group, one of them will be used. To avoid ambiguity, define the description on a single class.

## Sub-Command Help

The built-in `help` command works with groups. Use `help <group> <subcommand>` to see the full man page for a sub-command:

```
myapp help config          # Shows sub-command listing for the config group
myapp help config add      # Shows full man page for "config add"
```
