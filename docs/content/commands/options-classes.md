---
title: "Options Classes"
description: "Bundle related options into reusable classes with @Options, compose them across commands, and use @GlobalOptions."
weight: 2
---

As CLI tools grow, groups of related options tend to appear together across multiple commands. Rather than duplicating the same `@Option` parameters on every method, Crest lets you bundle them into a reusable class annotated with `@Options`.

## Defining an Options Class

An `@Options` class defines its options through its constructor parameters. Each constructor parameter uses the same `@Option`, `@Default`, and `@Required` annotations as regular command parameters.

```java
@Options
public class Config {
    private final String name;
    private final String env;

    public Config(@Option("config") @Default("default") final String name,
                  @Option("env") @Default("prod") final String env) {
        this.name = name;
        this.env = env;
    }

    public String getName() { return name; }
    public String getEnv() { return env; }
}
```

```java
@Options
public class CustomerIds {
    private final List<String> customerIds;
    private final File customerIdFile;

    public CustomerIds(@Option("customer-id") final List<String> customerIds,
                       @Option("customers") final File customerIdFile) {
        this.customerIds = customerIds;
        this.customerIdFile = customerIdFile;
    }

    public List<String> getCustomerIds() { return customerIds; }
    public File getCustomerIdFile() { return customerIdFile; }
}
```

## Using Options in Commands

Inject an `@Options` class into a command as a plain parameter -- no annotation needed on the parameter itself. Crest recognizes the type and expands its options into the command's option set.

```java
@Command
public void deploy(final Config config,
                   final CustomerIds customers,
                   @Option("version") final String version) { ... }
```

CLI usage: `deploy --config=staging --env=dev --customer-id=acme --version=2.1.0`

The `--config`, `--env`, and `--customer-id` options come from the `Config` and `CustomerIds` classes, while `--version` is defined directly on the method.

## Composing Multiple Options

A single command can accept any number of `@Options` classes. This lets you compose option sets freely:

```java
@Command
public void upload(final Config config,
                   final CustomerIds customers,
                   @Option("dry-run") @Default("true") final Boolean dryRun) { ... }

@Command
public void download(final Config config,
                     @Option("output") final File outputDir) { ... }
```

Both commands share the `Config` options, but only `upload` includes `CustomerIds`. Each command gets a clean, focused signature while reusing common option definitions.

## Constructor Parameters

The constructor is the only place to define options in an `@Options` class. Crest calls the constructor with the parsed option values, so the class can validate and store them however it likes:

```java
@Options
public class DateRange {
    private final LocalDate start;
    private final LocalDate end;

    public DateRange(@Option("start") @Required final LocalDate start,
                     @Option("end") @Required final LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        this.start = start;
        this.end = end;
    }

    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
}
```

## Nillable Options

By default, Crest always constructs the `@Options` object, even if the user provides none of its options. Use `nillable = true` to allow the object to be `null` when no values are provided:

```java
@Options(nillable = true)
public class Pagination {
    public Pagination(@Option("page") final int page,
                      @Option("size") @Default("20") final int size) { ... }
}
```

```java
@Command
public void list(final Pagination pagination,
                 @Option("filter") final String filter) {
    if (pagination == null) {
        // No pagination options were provided
    }
}
```

This is useful when the presence or absence of an option group is meaningful.

## @GlobalOptions

`@GlobalOptions` works the same as `@Options`, but the options are automatically available to every command without needing to add the class as a parameter.

```java
@GlobalOptions
public class Verbosity {
    public Verbosity(@Option("verbose") @Default("false") final boolean verbose,
                     @Option("quiet") @Default("false") final boolean quiet) { ... }
}
```

With `@GlobalOptions`, every command in the application automatically recognizes `--verbose` and `--quiet` without explicitly declaring a `Verbosity` parameter.
