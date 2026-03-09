---
title: "Cell Formatting"
description: "Control how table cells are rendered using PropertyEditor-based formatting with getAsText()."
weight: 5
---

Crest uses Java's `PropertyEditor` mechanism to format individual table cells. This allows you to control the display representation of any type that appears in your table output.

## How Cell Rendering Works

When rendering a cell value, the framework follows this resolution order:

1. **PropertyEditor lookup** -- if a `PropertyEditor` is registered for the field's type and it implements `getAsText()`, that result is used.
2. **toString() fallback** -- if no editor is found, the cell value's `toString()` method is called.

This means any `@Editor` you register for CLI argument parsing will also control how that type appears in table cells, provided it implements `getAsText()`.

## Example: Formatting Instant Values

By default, `java.time.Instant` renders using its `toString()` method, producing ISO-8601 output like `2025-03-08T14:30:00Z`. To display a more readable format in tables, register a `PropertyEditor`:

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

With this editor registered, an `Instant` value of `2025-03-08T14:30:00Z` renders as `2025-03-08 14:30` in table cells.

The same editor serves double duty:
- `setAsText()` handles CLI argument parsing (string to object).
- `getAsText()` handles table cell rendering (object to string).

## Registering Editors

Editors must be discoverable by the framework. There are two ways to register them.

### Via the Loader

Include the editor class in your `Loader` implementation:

```java
public class MyLoader implements Loader {
    @Override
    public Iterator<Class<?>> iterator() {
        return Loader.of(
            // Command classes
            JobCommands.class,
            ReportCommands.class,
            // Editor classes
            InstantEditor.class,
            DurationEditor.class
        ).iterator();
    }
}
```

### Via Main.builder()

Pass the editor class to `load()`:

```java
Main.builder()
        .command(JobCommands.class)
        .command(ReportCommands.class)
        .load(InstantEditor.class)
        .load(DurationEditor.class)
        .build();
```

Crest inspects each class: if annotated with `@Editor`, it registers the editor with Java's `PropertyEditorManager`. The editor then applies globally -- every table cell of that type uses the editor's `getAsText()` output.

## Choosing Between AbstractConverter and PropertyEditorSupport

If you only need CLI argument parsing (string to object) and do not need custom table formatting, use `AbstractConverter`:

```java
@Editor(LocalDate.class)
public class LocalDateEditor extends AbstractConverter {
    @Override
    protected Object toObjectImpl(final String s) {
        return LocalDate.parse(s);
    }
}
```

`AbstractConverter` does not implement `getAsText()`, so table cells will fall back to `toString()`.

If you need both parsing and custom table display, use `PropertyEditorSupport` and implement both `setAsText()` and `getAsText()`:

```java
@Editor(Duration.class)
public class DurationEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(final String text) {
        setValue(Duration.parse(text));
    }

    @Override
    public String getAsText() {
        final Duration d = (Duration) getValue();
        if (d == null) return "";
        final long hours = d.toHours();
        final long minutes = d.toMinutesPart();
        return String.format("%dh %dm", hours, minutes);
    }
}
```

With this editor, a `Duration` of `PT2H30M` renders as `2h 30m` in table cells instead of the default `PT2H30M`.
