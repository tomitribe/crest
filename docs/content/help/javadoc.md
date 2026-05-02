---
title: "Javadoc"
description: "Use method javadoc for command help. DocumentParser supports paragraphs, headings, bullets, preformatted blocks, and inline emphasis."
weight: 2
---

Javadoc on a `@Command` method becomes the body of its man page. The `HelpProcessor` annotation processor extracts javadoc at compile time and stores it in `META-INF/crest/{className}/{commandName}.{index}.properties`. At runtime, `DocumentParser` parses that text into structured elements that render in the terminal.

```java
/**
 * Generates a signed JWT token for the given customer.
 *
 * The token includes the customer ID and expiration date,
 * encrypted with the configured private key.
 *
 * @param customerId the customer account identifier
 * @param expiration when the token should expire
 */
@Command("generate")
public String generate(@Option("customer-id") final String customerId,
                       @Option("expiration") final LocalDate expiration) { ... }
```

The first sentence ("Generates a signed JWT token for the given customer") becomes the one-line description shown in command listings. The body becomes the DESCRIPTION section.

## Syntax

`DocumentParser` recognizes five kinds of element. The rules are deliberately simple: blank lines separate elements, and structure is signaled by line prefixes.

### Paragraphs

Plain text. Soft-wrapped lines are joined into a single paragraph; blank lines start a new one.

```
This option causes the command to skip files that already exist
in the destination directory.

A subsequent run will detect new files and copy only those.
```

### Headings

Two ways to write a heading. ALL-CAPS lines (no lowercase letters) are detected automatically:

```
SYSTEMS

This command writes to AWS S3 and triggers an AWS Lambda
```

Or use `=` or `#` markers (any number, optional spaces):

```
= Cron Expression Format

# Wildcards
```

The ALL-CAPS form is idiomatic in Crest help and matches traditional man-page convention.

### Bullets

Lines starting with `-`:

```
- The first option
- The second option
- The third option, which has rather longer text
  that wraps onto a continuation line
```

A blank line terminates the bullet list. Continuation lines need not be indented -- any non-blank, non-structural line is treated as a continuation of the active bullet.

### Preformatted blocks

Lines indented by four spaces preserve their formatting verbatim. Use this for code, command examples, or anything that should not be re-flowed:

```
    distribe customer extend 0016S00003EiXTaQAN 2026-12-31
```

The four-space indent is stripped on render.

### Inline emphasis with backticks

Backticks render as **bold** in the terminal. The terminal output is monochrome -- there is no syntax highlighting. Use backticks to mark tokens the reader should focus on: flag names, literal values, identifiers.

```
- `minute` accepts values `0-59` and wildcards `, - * /`
```

`--flags` are detected and rendered bold automatically; you don't need backticks around flag names. Reach for backticks when emphasizing anything else: a value, a config key, a literal character.

## Javadoc tags

Specific javadoc tags map to man-page sections:

- `@deprecated` -- populates the DEPRECATED section
- `@see` -- populates SEE ALSO
- `@author` -- populates AUTHORS

`@param` tags serve as a fallback for option descriptions when neither `OptionDescriptions.properties` nor `@Option(description)` is provided. The tag name matches the Java parameter name (not the option name); the description text is used as-is.

```java
/**
 * @param everything indicates all changes should be committed
 */
@Command("commit")
public void commit(@Option("all") final boolean everything) { ... }
```

`@throws` and `@return` tags are not rendered. Empty `@param` tags add nothing -- skip them, or fill them in with text the user will actually see.

## See also

- [**Example**]({{< ref "example" >}}) -- real javadoc shown alongside its rendered terminal output.
