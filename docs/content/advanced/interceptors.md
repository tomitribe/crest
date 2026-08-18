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
- **`getParameters()`** -- returns a mutable list of resolved parameters, positionally matching the command method's signature. You can inspect or modify parameter values before calling `proceed()`.
- **`getOptions()`** -- returns the mutable option values of the invocation, keyed by option name: every option declared by the command or by any interceptor bound to it. Where the command declares the option, the entry is a live view over the same storage as `getParameters()` -- a write through either is seen through both. Replacing a value is seen by every interceptor later in the chain and by the command itself. Writes are type checked against the option's declared type, and the key set is fixed: values may be replaced, entries cannot be added or removed.
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

## Ordering with @Priority

Interceptors run in ascending `@Priority` order: the lower the value, the earlier (outermost) the interceptor runs. An interceptor with no `@Priority` runs at 5, the middle of the space. Interceptors with equal priority run in the order they are declared on the command method.

```java
@Priority(2)
public class AuthInterceptor {
    @CrestInterceptor
    public Object intercept(final CrestContext ctx) { ... }
}
```

The value is a double so a new interceptor can always be slotted between two existing priorities without renumbering: between 6 and 7 there is 6.5, between 6 and 6.5 there is 6.4, and so on. The valid range is greater than 0 and less than 11, exclusive on both ends -- the endpoints are deliberately not allowed so no interceptor can take the last spot and shut others out. If 1 is taken you can run earlier with 0.9; if 0.9 is taken there is 0.8. There is always room.

Priority orders the whole chain regardless of which binding style attached each interceptor.

## Interceptor Options

An interceptor usually has logic of its own, and logic wants configuration: a filtering interceptor needs its patterns, a retry interceptor its attempt count, an auditing interceptor its log destination. Interceptors declare these needs directly, as `@Option` parameters and `@Options` beans on the `@CrestInterceptor` method -- exactly as a command method would declare its own:

```java
public class FilterInterceptor {
    @CrestInterceptor
    public Object intercept(final CrestContext ctx,
                            @Option("include") final Pattern include,
                            @Option("exclude") final Pattern exclude) {
        ...
        return ctx.proceed();
    }
}
```

Binding the interceptor gives the command those options. They parse from the command line, appear in the command's help and bash completion, and are passed to the interceptor when it runs:

```
list-vms --include=web-.* --exclude=stopped --region=us-east
```

Here `--include` and `--exclude` belong to `FilterInterceptor` and `--region` belongs to the command method -- the user sees one seamless command. The interceptor is a complete, self-contained feature: its logic and its command-line surface travel together, and every command it is bound to gains both.

A command and an interceptor may declare the *same* option when the declarations are identical -- same name, same type, same default -- in which case they share one value and the option appears once in help. Any other collision is ambiguous and fails at deploy time with an error naming both declarers.

Interceptor options must all be named. A positional parameter on an interceptor method is rejected at deploy time -- a positional argument contributed by an invisible participant could not be understood by anyone reading the command.

### Shared Option Values

`getOptions()` holds one converted value per option name, shared by every declarer. An interceptor can also replace a value, and everything later in the chain -- interceptors and the command itself -- sees the replacement:

```java
@Priority(2)
public class ClampInterceptor {
    @CrestInterceptor
    public Object intercept(final CrestContext ctx,
                            @Option("amount") final Integer amount) {
        if (amount != null && amount > 100) {
            ctx.getOptions().put("amount", 100);
        }
        return ctx.proceed();
    }
}
```

Each interceptor's option arguments materialize at its turn in the chain, so an interceptor sees the option values as left by every interceptor that ran before it. `@Options` beans are derived values: replace a constituent option by name and any bean built from it is derived again before its receiver sees it -- mutate the constituent, not the bean.

## Deploy-Time Binding

Interceptor bindings resolve when the `Main` is constructed, once all commands and interceptors are registered. A custom annotation with no matching interceptor, a referenced class with no `@CrestInterceptor` method, an out-of-range priority, or an option conflict fails at startup -- a misconfigured deployment dies loudly before any command runs, rather than on first invocation.

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
