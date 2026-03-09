---
title: "Loader"
description: "The central registry for commands, interceptors, and editors, with META-INF/services and Main.builder() integration."
weight: 5
---

The `Loader` is the central registry for all classes Crest needs to discover. It returns `@Command` classes, `@CrestInterceptor` classes, and `@Editor` classes. Crest inspects each class and registers it according to its annotations.

## Implementing a Loader

Implement the `org.tomitribe.crest.api.Loader` interface. The `Loader.of()` helper method makes it easy to return a fixed set of classes:

```java
public class MyLoader implements Loader {
    @Override
    public Iterator<Class<?>> iterator() {
        return Loader.of(
            // Command classes
            ConfigCommands.class,
            S3Commands.class,
            CustomerCommands.class,
            // Interceptor classes
            AuditInterceptor.class,
            // Editor classes
            InstantEditor.class
        ).iterator();
    }
}
```

Crest handles the classification automatically -- it checks each class for `@Editor`, `@CrestInterceptor`, or `@Command` annotations and registers it in the appropriate registry.

## META-INF/services Registration

Register your `Loader` implementation using Java's `ServiceLoader` mechanism. Create a file at `META-INF/services/org.tomitribe.crest.api.Loader` containing the fully qualified class name:

```
com.example.cli.MyLoader
```

Crest discovers and invokes your `Loader` at startup via `ServiceLoader`.

## Main.builder()

For programmatic setup without `ServiceLoader` discovery, use `Main.builder()`. It provides two methods for registering classes:

- **`command(Class<?>)`** -- adds a `@Command` class. If no classes are added via `command()`, discovery falls back to the classpath `Loader`.
- **`load(Class<?>)`** -- adds any class the `Loader` would return: `@Editor`, `@CrestInterceptor`, or `@Command`.

```java
Main.builder()
        .command(ConfigCommands.class)
        .command(S3Commands.class)
        .load(AuditInterceptor.class)
        .load(InstantEditor.class)
        .build();
```

The distinction matters: `command()` is specific to `@Command` classes, while `load()` accepts any class and lets Crest determine its role from annotations. Use `command()` for command classes and `load()` for editors and interceptors.
