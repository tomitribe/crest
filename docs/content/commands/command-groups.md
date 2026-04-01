---
title: "Command Groups"
description: "Organize commands into groups and sub-commands using class-level @Command annotations."
weight: 3
---

As a CLI tool grows, a flat list of commands becomes hard to navigate. Crest supports command groups -- a parent command that contains sub-commands -- using the class-level `@Command` annotation.

## Defining a Command Group

Place `@Command` on the class itself to define a group. The class-level `value` becomes the group name, and each `@Command`-annotated method inside becomes a sub-command.

```java
/**
 * Manage configuration
 */
@Command("config")
public class ConfigCommands {

    /** Add a new config value */
    @Command
    public void add(final Name name,
                    @Required @Option("value") final String value) { ... }

    /** Remove a config value */
    @Command
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
/**
 * User management
 */
@Command("user")
public class UserCommands {

    /** Create a new user */
    @Command
    public void create(@Option("name") @Required final String name,
                       @Option("email") @Required final String email) { ... }

    /** Delete a user */
    @Command
    public void delete(@Option("id") @Required final String id) { ... }
}

@Command("user")
public class UserQueryCommands {

    /** List all users */
    @Command
    public void list(@Option("filter") final String filter) { ... }

    /** Show user details */
    @Command
    public void show(@Option("id") @Required final String id) { ... }
}
```

Both classes contribute to the `user` group. The resulting CLI has four sub-commands: `user create`, `user delete`, `user list`, and `user show`.

## Description Resolution

When multiple classes contribute to the same group, the group description comes from whichever class has a non-empty `@Command(description)`. All contributing classes are checked, so it does not matter which class is registered first.

In the example above, `UserCommands` provides the description `"User management"`, while `UserQueryCommands` does not specify one. The group description resolves to `"User management"` regardless of registration order.

If multiple classes provide a description for the same group, one of them will be used. To avoid ambiguity, define the description on a single class.

## Deeper Command Groups

Command groups can be nested to any depth by using space-separated names in the `@Command` value. Each token becomes a level in the command hierarchy.

```java
/**
 * Quote management
 */
@Command("quote")
public class QuoteCommands {

    /** Create a quote */
    @Command
    public void create(@Option("name") final String name) { ... }

    /** Remove a quote */
    @Command
    public void remove(@Option("id") final String id) { ... }
}

/**
 * Manage line items
 */
@Command("quote line-item")
public class QuoteLineItemCommands {

    /** Create a line item */
    @Command
    public void create(@Option("product") final String product,
                       @Option("quantity") final int quantity) { ... }

    /** Delete a line item */
    @Command
    public void delete(@Option("id") final String id) { ... }

    /** List line items */
    @Command
    public void list() { ... }
}
```

CLI usage:

```
quote create --name="Acme proposal"
quote remove --id=42
quote line-item create --product=Widget --quantity=10
quote line-item delete --id=7
quote line-item list
```

The resulting hierarchy:

```
quote
├── create       Create a quote
├── remove       Remove a quote
└── line-item
    ├── create   Create a line item
    ├── delete   Delete a line item
    └── list     List line items
```

Intermediate groups are created automatically. If `QuoteLineItemCommands` declares `@Command("quote line-item")` but no class declares `@Command("quote")`, the `quote` group is auto-created with no description. If a class later provides a `@Command("quote")` class with a javadoc description, the description merges in.

### Method-Level Paths

The class-level and method-level `@Command` values are concatenated, following the same model as JAX-RS `@Path`. A method can contribute additional group levels:

```java
@Command("quote")
public class QuoteCommands {

    /** Create a quote */
    @Command
    public void create(@Option("name") final String name) { ... }

    /** Create a line item */
    @Command("line-item create")
    public void lineItemCreate(@Option("product") final String product) { ... }

    /** Delete a line item */
    @Command("line-item delete")
    public void lineItemDelete(@Option("id") final String id) { ... }
}
```

This produces the same hierarchy as the separate-class example above. The two approaches can be mixed freely -- some sub-commands defined inline via method paths, others contributed by separate classes.

## Sub-Command Help

The built-in `help` command works with groups. Use `help <group> <subcommand>` to see the full man page for a sub-command:

```
myapp help config          # Shows sub-command listing for the config group
myapp help config add      # Shows full man page for "config add"
```
