---
title: "Exit Codes"
description: "Control process exit codes with @Exit-annotated exceptions for structured CLI error handling."
weight: 6
---

The `@Exit` annotation controls the process exit code when a command throws an exception. Annotate your exception classes with `@Exit` to map specific error conditions to specific exit codes.

## Basic Usage

Annotate an exception class with `@Exit` and specify the exit code value:

```java
@Exit(1)
public static class InvalidCustomerIdFormatException extends RuntimeException {
    public InvalidCustomerIdFormatException(final String id) {
        super("Invalid customer ID format: " + id);
    }
}

@Exit(28)
public static class NoCustomerIdsSuppliedException extends RuntimeException {
    public NoCustomerIdsSuppliedException() {
        super("Supply at least one --customer-id or --customers file");
    }
}
```

When a command throws one of these exceptions, Crest prints the exception message to stderr and exits with the specified code. Without `@Exit`, uncaught exceptions result in a stack trace and a default exit code.

## Using @Exit in Commands

Throw `@Exit`-annotated exceptions from your command methods or from domain wrapper type constructors:

```java
@Command
public void deploy(@Option("target") @Required final String target,
                   @Option("version") @Required final String version) {
    if (!isValidTarget(target)) {
        throw new InvalidTargetException(target);
    }
    // ... deploy logic
}

@Exit(5)
public static class InvalidTargetException extends RuntimeException {
    public InvalidTargetException(final String target) {
        super("Unknown deployment target: " + target);
    }
}
```

## @Exit with help = true

Use `@Exit(value = ..., help = true)` to print the command's help text after the error message. This is useful when the error indicates the user invoked the command incorrectly:

```java
@Exit(value = 400, help = true)
public class MissingArgumentException extends IllegalArgumentException {
    public MissingArgumentException(final String message) {
        super(message);
    }
}
```

When this exception is thrown, the user sees the error message followed by the full help output for the command, making it clear how to correct their invocation.

## Nesting Exceptions in Wrapper Types

A common pattern is to nest `@Exit`-annotated exceptions inside the domain wrapper type they validate. This keeps the exception, the validation logic, and the exit code together:

```java
public class Product {
    private final String value;

    public Product(final String value) {
        final String lc = value.toLowerCase();
        if (!lc.equals(value)) {
            throw new ProductNotLowercaseException(value);
        }
        this.value = value;
    }

    public String get() { return value; }

    @Exit(2)
    public static class ProductNotLowercaseException extends RuntimeException {
        public ProductNotLowercaseException(final String product) {
            super("Product name must be lowercase: " + product);
        }
    }
}
```

When Crest converts a CLI argument to `Product` and the constructor throws `ProductNotLowercaseException`, the process exits with code 2 and prints the error message.
