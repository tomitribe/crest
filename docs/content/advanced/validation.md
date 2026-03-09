---
title: "Validation"
description: "Bean Validation (JSR-380) integration with built-in file validators and custom constraint support."
weight: 3
---

Crest integrates with Bean Validation (JSR-380) to validate command parameters before the command method executes. If validation fails, the framework reports the error to the user automatically.

## Built-in Validators

Crest provides built-in validation annotations for common file system checks:

- **`@Exists`** -- the file or directory must exist
- **`@Readable`** -- the file must be readable
- **`@Writable`** -- the file must be writable
- **`@Executable`** -- the file must be executable
- **`@Directory`** -- the path must be a directory

These annotations can be combined on a single parameter:

```java
@Command
public void process(@Option("input") @Exists @Readable final File input,
                    @Option("output") @Directory final File outDir) { ... }
```

If the user provides a path that does not exist for `--input`, the framework reports a validation error before the command runs.

## Custom Validators

Create custom validation annotations using the standard `@Constraint` mechanism from Bean Validation. Define the annotation and its validator class together:

```java
@Exists
@Constraint(validatedBy = {IsFile.Constraint.class})
@Target({PARAMETER})
@Retention(RUNTIME)
public @interface IsFile {
    String message() default "{org.example.IsFile.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    class Constraint implements ConstraintValidator<IsFile, File> {
        @Override
        public boolean isValid(final File file,
                               final ConstraintValidatorContext ctx) {
            return file.isFile();
        }
    }
}
```

This `@IsFile` annotation composes `@Exists` (so the file must exist) and adds its own check that the path is a regular file (not a directory). Use it on command parameters just like the built-in annotations:

```java
@Command
public void analyze(@Option("config") @IsFile final File config,
                    @Option("output") @Directory @Writable final File outDir) { ... }
```

### Validation on Wrapper Types

Bean Validation annotations can also be placed on constructor parameters of domain wrapper types. This lets you centralize validation logic in the type itself:

```java
public class Port {
    private final int value;

    public Port(@Min(1) @Max(65535) final String port) {
        this.value = Integer.parseInt(port);
    }

    public int get() { return value; }
}
```
