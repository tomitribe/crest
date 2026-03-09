---
title: "Type Conversion"
description: "How Crest converts CLI string arguments to Java types using a chain of strategies, and how to leverage domain wrapper types."
weight: 1
---

Crest automatically converts CLI string arguments to Java types. The conversion is handled by `org.tomitribe.util.editor.Converter`, which tries several strategies in order until one succeeds.

## Conversion Chain

When Crest encounters a CLI argument that needs to be converted to a target type, it applies these strategies in order:

1. **PropertyEditor** -- if a `PropertyEditor` is registered for the target type (via `@Editor` or Java's `PropertyEditorManager`), it is used first.
2. **Enum** -- `Enum.valueOf()` is called with case fallbacks: exact match, then uppercase, then lowercase.
3. **Constructor(String)** -- any public constructor taking a single `String` parameter.
4. **Constructor(CharSequence)** -- any public constructor taking a single `CharSequence` parameter.
5. **Static factory method** -- any public static method taking a `String` and returning the target type (e.g., `valueOf`, `of`, `parse`).

This chain means that most standard Java types work out of the box, and your own types can participate simply by providing a `String` constructor or a static factory method.

## Built-in Types

Crest handles these types without any configuration:

- **Primitives and wrappers** -- `int`, `Integer`, `boolean`, `Boolean`, `long`, `Long`, `double`, `Double`, etc.
- **Strings** -- `String`
- **File system** -- `File`, `Path`
- **Network** -- `URI`, `URL`
- **Pattern** -- `Pattern` (compiled regex)
- **Date** -- `Date`
- **Character** -- `Character`
- **Enums** -- all enum types
- **Collections** -- `List<T>`, `Set<T>`, `Map<K,V>`, and arrays

## Domain Wrapper Types

For positional arguments, prefer domain-specific wrapper types over raw `String`. A class with a `public Constructor(String)` is automatically usable as a CLI parameter type. This gives you type safety, validation, and self-documenting method signatures.

### Product Example

```java
public class Product {
    private final String value;

    public Product(final String value) {
        final String lc = value.toLowerCase();
        if (!lc.equals(value)) {
            throw new ProductNotLowercaseException(value);
        }
        if (lc.startsWith("apache")) {
            throw new ProductPrefixException(value);
        }
        this.value = value;
    }

    public String get() { return value; }

    @Override
    public String toString() { return value; }

    @Exit(2)
    public static class ProductNotLowercaseException extends RuntimeException {
        public ProductNotLowercaseException(final String product) {
            super("Product name must be lowercase: " + product);
        }
    }

    @Exit(3)
    public static class ProductPrefixException extends RuntimeException {
        public ProductPrefixException(final String product) {
            super("Product name should not start with 'apache': " + product);
        }
    }
}
```

### CustomerId Example

```java
public class CustomerId {
    private final String id;

    public CustomerId(final String id) {
        if (id.length() != 18 || !id.startsWith("001")) {
            throw new InvalidCustomerIdFormatException(id);
        }
        this.id = id;
    }

    public String get() { return id; }

    @Exit(1)
    public static class InvalidCustomerIdFormatException extends RuntimeException {
        public InvalidCustomerIdFormatException(final String id) {
            super(String.format("Invalid customer ID format '%s'", id));
        }
    }
}
```

### Using Wrapper Types as Parameters

Crest calls the `String` constructor automatically when it encounters these types as parameters:

```java
@Command("list-release")
public Stream<S3File> listRelease(final Product product,
                                  final Version version,
                                  final Config config) { ... }

@Command
public PrintOutput extend(final CustomerId customerId,
                           final ExpirationDate expiration,
                           final Config config) { ... }
```

CLI usage: `list-release tomcat 9.0.1` or `extend 001ABC123456789012 2025-12-31`

## Class Names in Help

A key benefit of wrapper types is that the class name appears in help output as the argument name. Using `final Product product` produces `Usage: list-release Product Version` in help, which is far more informative than `String String`.

## Constructor Validation

Wrapper constructors are a natural place for validation. When a constructor throws an exception, Crest catches it and reports the error to the user. Combine this with `@Exit`-annotated exceptions to control exit codes:

```java
public class Product {
    public Product(final String value) {
        if (!value.equals(value.toLowerCase())) {
            throw new ProductNotLowercaseException(value);
        }
        this.value = value;
    }

    @Exit(2)
    public static class ProductNotLowercaseException extends RuntimeException {
        public ProductNotLowercaseException(final String product) {
            super("Product name must be lowercase: " + product);
        }
    }
}
```

If the user passes `Tomcat` as the product, the constructor throws `ProductNotLowercaseException`, the error message is printed, and the process exits with code 2.

## Conventions for Wrapper Types

- Store the raw value in a `final` field
- Validate in the constructor, throwing `@Exit`-annotated exceptions
- Alternatively, use Bean Validation annotations on the constructor parameter
- Provide a `get()` method and `toString()`
- Implement `Comparable` when ordering matters
- Nest the exception classes inside the wrapper for cohesion
