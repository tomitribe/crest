---
title: "Programmatic Table Building"
description: "Build table output programmatically with TableOutput.builder() for full control over data, fields, sorting, and borders."
weight: 4
---

When you need more control than the `@Table` annotation provides, use `TableOutput.builder()` to construct table output programmatically. `TableOutput` implements `PrintOutput`, so it can be returned directly from a command method.

## Basic Usage

```java
@Command
public TableOutput report(final Config config) {
    final List<Account> accounts = loadAccounts(config);

    return TableOutput.builder()
            .data(accounts)
            .fields("id name email status")
            .sort("name")
            .border(Border.asciiCompact)
            .header(true)
            .build();
}
```

## Builder Methods

### data()

Sets the data source for the table. Accepts multiple input types:

```java
// From an Iterable (List, Set, etc.)
.data(accounts)

// From a Stream
.data(accountStream)

// From an array
.data(accountArray)
```

### fields()

Space-delimited field or getter names to include as columns. Supports the same dot notation for nested properties as the `@Table` annotation:

```java
.fields("id name email status")
.fields("accountId customer expiration.date expiration.expired")
```

### sort()

Space-delimited field names for sorting:

```java
.sort("name")
.sort("status name")
```

### border()

Sets the border style from the `Border` enum:

```java
.border(Border.asciiCompact)
.border(Border.unicodeSingle)
.border(Border.githubMarkdown)
```

### header()

Controls whether a header row is included:

```java
.header(true)
.header(false)
```

### options(TableOptions)

Applies user-provided runtime overrides from a `TableOptions` parameter. This is how you integrate `TableOutput.builder()` with CLI flags like `--table-border` and `--table-sort`:

```java
@Command
public TableOutput report(final Config config, final TableOptions tableOptions) {
    final List<Account> accounts = loadAccounts(config);

    return TableOutput.builder()
            .data(accounts)
            .fields("id name email status")
            .sort("name")
            .border(Border.asciiCompact)
            .header(true)
            .options(tableOptions)
            .build();
}
```

### options(Options)

An overloaded variant that accepts the internal `Options` object directly. This is useful for advanced scenarios where you are working with the framework internals.

## Order Semantics

Builder methods are applied in call order. This means the position of `options()` relative to other setters matters:

- Values set **before** `options()` are overridden by non-null values in `TableOptions`.
- Values set **after** `options()` override whatever `TableOptions` provided.
- Null values in `TableOptions` do not override existing settings.

For example, this ensures the user can override everything except the border:

```java
return TableOutput.builder()
        .data(accounts)
        .fields("id name email status")
        .sort("name")
        .header(true)
        .options(tableOptions)    // user overrides applied here
        .border(Border.asciiCompact) // always asciiCompact, even if user passes --table-border
        .build();
```

And this lets the user override everything, with the annotation values serving as defaults:

```java
return TableOutput.builder()
        .data(accounts)
        .fields("id name email status")  // default fields
        .sort("name")                    // default sort
        .border(Border.asciiCompact)     // default border
        .header(true)                    // default header
        .options(tableOptions)           // user overrides win
        .build();
```

## Practical Example

A reporting command that aggregates data from multiple sources and presents it as a table, with full user control over formatting:

```java
@Command
public TableOutput usage(final Config config, final TableOptions tableOptions) {
    final List<Account> accounts = loadAccounts(config);
    final Map<String, UsageStats> stats = loadUsageStats(config);

    final List<AccountUsage> rows = accounts.stream()
            .map(a -> new AccountUsage(a, stats.get(a.getId())))
            .collect(Collectors.toList());

    return TableOutput.builder()
            .data(rows)
            .fields("accountId name plan usage.requests usage.storage")
            .sort("name")
            .border(Border.asciiCompact)
            .header(true)
            .options(tableOptions)
            .build();
}
```

CLI usage:

```bash
# Default output
myapp usage

# Custom border and sort
myapp usage --table-border=unicodeSingle --table-sort="plan name"

# Export for scripting
myapp usage --tsv --no-table-header | awk -F'\t' '{print $3, $4}'
```
