---
title: "Editors"
description: "Register custom PropertyEditor implementations with @Editor for CLI argument parsing and table cell formatting."
weight: 4
---

The `@Editor` annotation registers a `PropertyEditor` for a specific Java type. Editors control how CLI string arguments are converted to objects and how objects are displayed in table output.

## AbstractConverter -- Minimal Editor

For simple string-to-object conversion, extend `AbstractConverter` and implement `toObjectImpl`:

```java
@Editor(LocalDate.class)
public class LocalDateEditor extends AbstractConverter {
    @Override
    protected Object toObjectImpl(final String s) {
        return LocalDate.parse(s);
    }
}
```

This is sufficient when you only need CLI argument parsing and `toString()` provides acceptable display output.

## PropertyEditorSupport -- Full Control

For full control over both parsing and display, extend `PropertyEditorSupport` and implement `setAsText` (string-to-object) and `getAsText` (object-to-string):

```java
@Editor(Environment.class)
public class EnvironmentEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        final Environment env = Arrays.stream(Environment.values())
                .filter(e -> e.getName().equalsIgnoreCase(text))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid environment: " + text));
        setValue(env);
    }

    @Override
    public String getAsText() {
        final Environment env = (Environment) getValue();
        return env != null ? env.getName() : "";
    }
}
```

### Instant Formatting Example

A common use case is formatting `java.time.Instant` for readable table output:

```java
@Editor(Instant.class)
public class InstantEditor extends PropertyEditorSupport {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    .withZone(ZoneOffset.UTC);

    @Override
    public void setAsText(final String text) {
        setValue(Instant.parse(text));
    }

    @Override
    public String getAsText() {
        final Instant instant = (Instant) getValue();
        return instant != null ? FMT.format(instant) : "";
    }
}
```

With this editor, `2025-03-08T14:30:00Z` on the CLI is parsed as an `Instant`, and in table output it renders as `2025-03-08 14:30`.

## Registration

Editors must be registered with Crest so the framework can discover them. There are two approaches:

### Via Loader

Include the editor class in your `Loader` implementation alongside command and interceptor classes:

```java
public class MyLoader implements Loader {
    @Override
    public Iterator<Class<?>> iterator() {
        return Loader.of(
            ConfigCommands.class,
            InstantEditor.class,
            LocalDateEditor.class
        ).iterator();
    }
}
```

### Via Main.builder()

Pass the editor class to `load()` on the builder:

```java
Main.builder()
        .command(ConfigCommands.class)
        .load(InstantEditor.class)
        .load(LocalDateEditor.class)
        .build();
```

Crest inspects each class: if annotated with `@Editor`, it registers the editor automatically.

## Dual Role: CLI Parsing and Table Display

An editor serves two purposes:

- **`setAsText(String)`** -- called during CLI argument parsing to convert the user's input string into the target object.
- **`getAsText()`** -- called during table rendering to convert the object back into a display string.

Table cells are rendered by first checking for a registered `PropertyEditor` for the field's type. If an editor is found, `getAsText()` is used. Otherwise, the cell falls back to `toString()`.

This means a single `@Editor` class controls both how a type is parsed from the command line and how it appears in `@Table` output, keeping the two representations consistent.
