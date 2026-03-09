---
title: "Return Types"
description: "Supported return types for Crest commands including String, StreamingOutput, PrintOutput, Stream, and void."
weight: 4
---

Crest commands can return several types. The framework handles writing the output to stdout automatically, so command methods stay focused on logic rather than I/O.

## String

The simplest return type. The returned string is printed to stdout followed by a newline.

```java
@Command
public String hello(@Option("name") @Default("World") final String name) {
    return "Hello, " + name;
}
```

```
$ hello --name=Alice
Hello, Alice
```

## StreamingOutput

For large or streaming output, return a `StreamingOutput`. The framework passes an `OutputStream` to write to directly. This avoids buffering the entire output in memory.

```java
@Command
public StreamingOutput export(final Config config) {
    return outputStream -> {
        final PrintWriter pw = new PrintWriter(outputStream);
        for (final Record record : loadRecords(config)) {
            pw.println(record.toCsv());
        }
        pw.flush();
    };
}
```

`StreamingOutput` is a functional interface with a single `write(OutputStream)` method, making it concise with a lambda.

## PrintOutput

Similar to `StreamingOutput`, but provides a `PrintStream` instead of a raw `OutputStream`. This is convenient when you want `println`, `printf`, and other `PrintStream` methods.

```java
@Command
public PrintOutput upload(final CustomerIds customerIds,
                          @Option("dry-run") @Default("true") final Boolean dryRun) {
    return out -> {
        for (final String id : customerIds.getIds()) {
            if (dryRun) {
                out.println("Would upload for customer: " + id);
            } else {
                out.println("Uploading for customer: " + id);
                performUpload(id);
            }
        }
    };
}
```

## Stream, List, and Iterable

Commands can return `Stream<T>`, `List<T>`, `Set<T>`, or any `Iterable<T>`. Each element is printed to stdout on its own line using `toString()`.

```java
@Command
public List<String> listEnvironments() {
    return List.of("dev", "staging", "prod");
}
```

```
$ list-environments
dev
staging
prod
```

When combined with the `@Table` annotation, collection returns are formatted as tabular output:

```java
@Command
@Table(fields = "name state schedule command", sort = "name")
public Stream<Job> list(final Config config) {
    return loadJobs(config).stream();
}
```

See the [Tables]({{< ref "/tables" >}}) section for details on tabular formatting.

## void

Commands that return `void` produce no output. Use `void` when the command communicates through side effects (writing files, making API calls) or when you handle output manually via injected I/O streams.

```java
@Command
public void deploy(@Option("target") final String target,
                   @Out final PrintStream out) {
    performDeploy(target);
    out.println("Deployed to " + target);
}
```

See [I/O Streams]({{< ref "io-streams" >}}) for details on `@Out` and related annotations.
