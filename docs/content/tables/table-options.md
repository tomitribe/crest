---
title: "Table Options"
description: "Let users override table formatting at runtime with TableOptions and CLI flags."
weight: 3
---

The `TableOptions` parameter allows end users to override table formatting settings at runtime via CLI flags. Add it as a parameter to any `@Table`-annotated command.

## Adding TableOptions to a Command

Include `TableOptions` as a parameter in your command method. No annotation is needed -- Crest recognizes it automatically:

```java
@Command
@Table(fields = "name state schedule", sort = "name")
public Stream<Job> list(final Config config, final TableOptions tableOptions) {
    return jobService.stream(config);
}
```

The `@Table` annotation defines the defaults. When the user provides CLI flags, `TableOptions` captures those overrides and the framework applies them on top of the annotation values.

## CLI Flags

When `TableOptions` is present, the following flags become available to the user:

### --table-border

Override the border style. Accepts any value from the `Border` enum:

```bash
myapp list --table-border=unicodeSingle
myapp list --table-border=githubMarkdown
myapp list --table-border=csv
```

### --no-table-header

Suppress the header row:

```bash
myapp list --no-table-header
```

### --table-sort

Override the sort order. Accepts a space-delimited list of field names:

```bash
myapp list --table-sort=state
myapp list --table-sort="state name"
```

### --table-fields

Override which fields are displayed:

```bash
myapp list --table-fields="name state"
myapp list --table-fields="name schedule command"
```

### --tsv

Shortcut to set the border style to tab-separated values. Useful for piping output to other tools:

```bash
myapp list --tsv
myapp list --tsv | cut -f1,3
```

## Full Example

A command with annotation defaults and user-overridable options:

```java
@Command
@Table(fields = "name state schedule command", sort = "name", border = Border.asciiCompact)
public Stream<Job> list(final Config config, final TableOptions tableOptions) {
    return jobService.stream(config);
}
```

Default output uses ASCII compact borders, sorted by name, showing all four fields. The user can then customize:

```bash
# Use Unicode borders and sort by state
myapp list --table-border=unicodeSingle --table-sort=state

# Show only name and state, no header, as TSV
myapp list --table-fields="name state" --no-table-header --tsv

# Export as CSV
myapp list --table-border=csv > jobs.csv
```
