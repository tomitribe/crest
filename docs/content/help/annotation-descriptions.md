---
title: "Annotation Descriptions"
description: "Use @Command(description), @Command(usage), and @Option(description) for inline, single-line help text."
weight: 3
---

For short commands and trivial options, inline annotation values are often the cleanest source of help text. They live next to the code they describe and need no annotation processor.

## @Command(description)

The one-line description shown next to each command in listings.

```java
@Command(description = "Set a config value")
public void set(@Option("key") final String key,
                @Option("value") final String value) { ... }
```

If `description` is omitted, Crest falls back to the first sentence of the method's javadoc (if any). The first sentence is determined by splitting on period-space (`". "`).

## @Command(usage)

Overrides the auto-generated SYNOPSIS line. By default Crest builds the SYNOPSIS from the method signature as `commandName [options] arg1 arg2...`. Use `usage` when you want a custom shape:

```java
@Command(value = "commit", usage = "commit [options] <message> <file>")
public void commit(@Option("all") final boolean all,
                   final String message,
                   final File file) { ... }
```

## @Option(description)

The text shown next to each option in the OPTIONS section.

```java
@Option(value = "all", description = "commit all changes") final boolean all
```

## Priority

Crest checks three sources for option descriptions, in this order:

1. [`OptionDescriptions.properties`]({{< ref "resource-bundle" >}}) ResourceBundle in the same package as the command class
2. `@Option(description)` on the option itself
3. `@param` tag in the method's [javadoc]({{< ref "javadoc" >}}), matching the Java parameter name

Whichever source provides text first wins. Mix freely -- you can keep simple descriptions inline and override or share specific ones via the bundle.
