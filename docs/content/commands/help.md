---
title: "Help"
description: "Built-in help command, man page generation from javadoc, option descriptions, and terminal formatting in Crest."
weight: 6
---

Crest provides a built-in `help` command that generates documentation automatically from your command definitions and javadoc. No configuration is required -- help is registered and available out of the box.

## Built-in Help Command

The `help` command is registered automatically. It supports listing all commands and showing detailed help for individual commands.

```
myapp help                  # Lists all commands with descriptions
myapp help deploy           # Full man page for "deploy"
myapp help config set       # Man page for sub-command "set" in group "config"
```

### Command Listings

Running `help` without arguments shows all available commands with their descriptions:

```
Commands:

   config     Manage configuration
   deploy     Deploy the application
   generate   Generates a signed JWT token for the given customer
   help
```

The listing is formatted as: 3 spaces, command name (left-aligned, padded to the longest name plus 3 spaces), then description. Padding is computed dynamically.

## Man Page Generation

Running `help <command>` renders a structured man page with sections: NAME, SYNOPSIS, DESCRIPTION, OPTIONS, DEPRECATED, SEE ALSO, and AUTHORS.

The content is sourced from method javadoc, extracted at compile time by the `HelpProcessor` annotation processor and stored in `META-INF/crest/{className}/{commandName}.{index}.properties`.

```java
/**
 * Generates a signed JWT token for the given customer.
 *
 * The token includes the customer ID and expiration date,
 * encrypted with the configured private key.
 *
 * @param customerId the customer account identifier
 * @param expiration when the token should expire
 * @deprecated Use the new token service instead
 * @see TokenService
 * @author Jane Doe
 */
@Command("generate")
public String generate(@Option("customer-id") final String customerId,
                       @Option("expiration") final LocalDate expiration) { ... }
```

The javadoc body becomes the DESCRIPTION section. The `@param` tags provide option descriptions (as a fallback -- see priority below). The `@deprecated`, `@see`, and `@author` tags populate their respective man page sections.

The description text supports markdown-like formatting: headings (`#` or `===`), bullets (`-`), and preformatted blocks (4-space indent).

## @Command `usage` Parameter

The `usage` parameter overrides the auto-generated SYNOPSIS line. If omitted, Crest builds it automatically from the method signature as `commandName [options] arg1 arg2...`.

```java
@Command(value = "commit", usage = "commit [options] <message> <file>")
public void commit(@Option("all") final boolean all,
                   final String message,
                   final File file) { ... }
```

## Command Description Sources

The one-line description shown next to each command in listings comes from two sources, in priority order:

### 1. @Command(description)

The primary source, defined inline in the annotation:

```java
@Command(description = "Set a config value")
public void set(@Option("key") final String key,
                @Option("value") final String value) { ... }
```

### 2. First Sentence of Javadoc

The fallback, extracted at compile time by the annotation processor. The first sentence is determined by splitting on period-space (`". "`):

```java
/**
 * Generates a signed JWT token for the given customer. The token
 * includes the customer ID and expiration date.
 */
@Command("generate")
public String generate(...) { ... }
```

The first sentence ("Generates a signed JWT token for the given customer") is used as the description. This fallback only works for methods (not class-level group descriptions) and requires the annotation processor to run during compilation.

## Option Description Sources

Three sources provide the description shown next to each `--flag` in help output. They are checked in priority order:

### 1. OptionDescriptions.properties

The highest priority source. Create a `ResourceBundle` named `OptionDescriptions.properties` in the same package as the command class:

```properties
# file: com/example/cli/OptionDescriptions.properties
recursive=recurse into directories
links=copy symlinks as symlinks

# Command-specific key takes precedence over the bare key
rsync.progress=don't show progress during transfer
progress=this is not the description that will be chosen
```

Lookup order: `commandName.optionName` first, then bare `optionName`.

### 2. @Option(description)

Inline in the annotation:

```java
@Option(value = "all", description = "commit all changes") final boolean all
```

### 3. Javadoc @param Tags

The lowest priority fallback, extracted at compile time:

```java
/**
 * @param everything indicates all changes should be committed
 */
@Command("commit")
public void commit(@Option("all") final boolean everything) { ... }
```

The `@param` tag name matches the Java parameter name, not the option name. The description text is used as-is.

## Terminal Formatting

Man pages are formatted for terminal display with:

- **Text wrapping** to the detected terminal width
- **ANSI color highlighting** for `--flags` and `` `code` `` spans
- **Justified text** with margin padding for readability
- **Pager support** -- output is piped through `less` if available, allowing scrolling through long help pages
