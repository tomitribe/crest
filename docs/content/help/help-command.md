---
title: "The Help Command"
description: "How the built-in help command works: command listings, man-page sections, and terminal rendering."
weight: 1
---

The `help` command is registered automatically. It supports listing all commands and showing detailed help for individual commands.

```
myapp help                  # Lists all commands with descriptions
myapp help deploy           # Full man page for "deploy"
myapp help config set       # Man page for sub-command "set" in group "config"
```

## Command listings

Running `help` without arguments lists all available commands with their one-line descriptions:

```
Commands:

   config     Manage configuration
   deploy     Deploy the application
   generate   Generates a signed JWT token for the given customer
   help
```

The format is: 3 spaces, command name (left-aligned, padded to the longest name plus 3 spaces), then description.

## Man page sections

Running `help <command>` renders a structured man page with the following sections:

- `NAME` -- the command name and its one-line description
- `SYNOPSIS` -- the command-line shape
- `DESCRIPTION` -- the body of the command's javadoc, formatted by `DocumentParser`
- `OPTIONS` -- one entry per option with its description
- `DEPRECATED` -- from the `@deprecated` javadoc tag
- `SEE ALSO` -- from `@see` javadoc tags
- `AUTHORS` -- from `@author` javadoc tags

Each section is populated from javadoc, annotation values, or resource bundles, depending on the source you've chosen. See [Javadoc]({{< ref "javadoc" >}}), [Annotation Descriptions]({{< ref "annotation-descriptions" >}}), and [Resource Bundles]({{< ref "resource-bundle" >}}).

## Terminal rendering

Output is formatted for terminal display:

- Text is wrapped to the detected terminal width
- Section headings, `--flags`, and backtick-wrapped tokens render in **bold** -- monochrome, with no syntax highlighting
- Bullets render with the `o` glyph used by traditional man pages
- Output is piped through `less` if available, allowing scroll through long help pages

For a complete rendered example, see [Example]({{< ref "example" >}}).
