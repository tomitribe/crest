---
title: "Interceptors"
description: "Define cross-cutting concerns with @CrestInterceptor and attach them to commands via direct reference or custom annotations."
weight: 2
---

Interceptors let you define cross-cutting concerns -- logging, timing, auditing, authorization -- that apply to commands without modifying the command logic itself. They follow an around-invoke pattern similar to Java EE interceptors.

## Defining an Interceptor

An interceptor is a class with a method annotated `@CrestInterceptor`. The method must accept a `CrestContext` parameter and return `Object`:

```java
public class TimingInterceptor {
    @CrestInterceptor
    public Object time(final CrestContext ctx) {
        final long start = System.currentTimeMillis();
        try {
            return ctx.proceed();
        } finally {
            System.err.println(ctx.getName() + " took " +
                (System.currentTimeMillis() - start) + "ms");
        }
    }
}
```

The method name can be anything -- only the `@CrestInterceptor` annotation matters.

## CrestContext

The `CrestContext` object provides access to the command being invoked:

- **`proceed()`** -- continues the interceptor chain and ultimately invokes the command method. You must call this to let the command execute.
- **`getMethod()`** -- returns the command's `java.lang.reflect.Method`.
- **`getParameters()`** -- returns a mutable list of resolved parameters. You can inspect or modify parameter values before calling `proceed()`.
- **`getName()`** -- returns the command name as a string.
- **`getParameterMetadata()`** -- returns metadata about parameter types, names, and nesting information.

## Attaching Interceptors

### Direct Attachment via interceptedBy

The simplest way to attach an interceptor is to reference its class directly in the `@Command` annotation:

```java
@Command(interceptedBy = TimingInterceptor.class)
public String deploy(@Option("target") final String target) { ... }
```

Multiple interceptors can be chained:

```java
@Command(interceptedBy = {AuditInterceptor.class, TimingInterceptor.class})
public String deploy(@Option("target") final String target) { ... }
```

## Custom Interceptor Annotations

Instead of listing interceptor classes in `@Command(interceptedBy)`, you can create a custom annotation that represents the interceptor. This produces cleaner, more readable code. There are two patterns.

### Pattern A: Explicit @CrestInterceptor(class)

The custom annotation directly names its interceptor class using `@CrestInterceptor(ClassName.class)`:

```java
@CrestInterceptor(AuditInterceptor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Audited {
}

public class AuditInterceptor {
    @CrestInterceptor
    public Object intercept(final CrestContext ctx) {
        log(ctx.getName(), ctx.getParameters());
        return ctx.proceed();
    }
}
```

Usage is clean and declarative:

```java
@Audited
@Command
public String transfer(@Option("from") final String from,
                        @Option("to") final String to) { ... }
```

### Pattern B: Indirect Resolution

The custom annotation is itself annotated with `@CrestInterceptor` (without a class reference), and the interceptor class is annotated with the custom annotation. The framework discovers the interceptor by matching the annotation:

```java
@CrestInterceptor
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Timed {
}

@Timed  // Links this interceptor to the @Timed annotation
public class TimedInterceptor {
    @CrestInterceptor
    public Object intercept(final CrestContext ctx) {
        final long start = System.nanoTime();
        try {
            return ctx.proceed();
        } finally {
            System.err.printf("%s: %dms%n", ctx.getName(),
                (System.nanoTime() - start) / 1_000_000);
        }
    }
}
```

Usage:

```java
@Timed
@Command
public String process(@Option("input") final File input) { ... }
```

With Pattern B, the interceptor class must be returned by a `Loader` (or registered via `Main.builder().load()`) so the framework can discover it and match it to the annotation.

### @Table Uses Pattern B

The built-in `@Table` annotation is an example of Pattern B. `@Table` is itself a `@CrestInterceptor` annotation, and the `TableInterceptor` class is annotated with `@Table`. This is why `@Table` works as both a configuration annotation (with `fields`, `sort`, `border` parameters) and an interceptor trigger.

Custom annotations can carry parameters just like `@Table` does. The interceptor reads these parameters from the method's annotations at runtime via `CrestContext.getMethod()`.
