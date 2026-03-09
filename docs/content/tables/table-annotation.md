---
title: "The @Table Annotation"
description: "Annotate commands returning collections with @Table for automatic tabular output with field selection, sorting, and border control."
weight: 1
---

The `@Table` annotation transforms collection-based return types into formatted tabular output. It works on commands that return `Stream`, `List`, `Set`, or arrays.

## Basic Usage

Place `@Table` on a `@Command` method alongside the return type:

```java
@Command
@Table(fields = "id name version", sort = "name", border = Border.unicodeSingle)
public List<Package> list() {
    return packageService.findAll();
}
```

The framework uses reflection to extract values from each element in the returned collection, building rows from the specified fields.

## Parameters

### fields

A space-delimited list of getter or field names to include as columns. Each name is resolved against the element type using standard JavaBean conventions.

```java
@Table(fields = "id name version status")
```

Nested properties are supported using dot notation. This follows the getter chain into child objects:

```java
@Command
@Table(fields = "accountId customer cores software expiration.date expiration.expired",
       sort = "customer")
public Stream<Subscription> info(final Config config) {
    return subscriptionService.list(config);
}
```

Here `expiration.date` calls `getExpiration()` on each `Subscription`, then `getDate()` on the resulting object.

### sort

A space-delimited list of field names to sort by. Sorting is applied in the order specified:

```java
@Table(fields = "name state schedule command", sort = "name")
```

### header

Controls whether a header row is included. Defaults to `true`:

```java
@Table(fields = "key value", header = true)
```

### border

Selects the border style from the `Border` enum. Defaults to `Border.asciiCompact`:

```java
@Table(fields = "id name version", border = Border.unicodeSingle)
```

See [Border Styles]({{< ref "border-styles" >}}) for the full list of available styles.

## Return Type Examples

The `@Table` annotation works with any iterable or stream return type:

```java
// With Stream
@Command
@Table(fields = "name state schedule command", sort = "name")
public Stream<Job> listJobs(final Config config) {
    return jobService.stream(config);
}

// With List
@Command
@Table(fields = "id name version", sort = "name", border = Border.unicodeSingle)
public List<Package> listPackages() {
    return packageService.findAll();
}

// With Set (useful for Map entries)
@Command
@Table(fields = "key value", sort = "key")
public Set<Map.Entry<Object, Object>> list(final Config config, final String path) {
    return configService.getProperties(config, path).entrySet();
}
```

## Combined Example

A full command combining nested fields, sorting, and a custom border:

```java
@Command
@Table(fields = "accountId customer cores software expiration.date expiration.expired",
       sort = "customer",
       border = Border.githubMarkdown)
public Stream<Subscription> subscriptions(final Config config) {
    return subscriptionService.list(config);
}
```
