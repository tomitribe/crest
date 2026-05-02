---
title: "Help"
description: "Built-in help command, javadoc-driven man pages, and three sources for command and option descriptions."
weight: 3
---

Crest provides a built-in `help` command that generates documentation automatically from your command definitions. No configuration is required -- help is registered and available out of the box.

## The feature

- **[The Help Command]({{< ref "help-command" >}})** -- what `myapp help` does, command listings, man-page sections, and how output is rendered in the terminal.

## Three approaches to writing help

Crest pulls description text from three sources, in priority order. Mix and match across commands -- pick the approach that fits each case.

- **[Javadoc]({{< ref "javadoc" >}})** -- the richest approach. Method javadoc becomes the DESCRIPTION section, with paragraphs, headings, bullets, and preformatted blocks. `@param` tags fall back as option descriptions. Best for commands that need real documentation.
- **[Annotation Descriptions]({{< ref "annotation-descriptions" >}})** -- `@Command(description)` and `@Option(description)` for inline, single-line text. Best for short commands and trivial options.
- **[Resource Bundles]({{< ref "resource-bundle" >}})** -- `OptionDescriptions.properties` for centralized, per-package option text. Best for shared vocabularies and i18n.

## Writing good help

- **[Best Practices]({{< ref "best-practices" >}})** -- conventions for help text that actually helps the next person to run the command.
- **[Example]({{< ref "example" >}})** -- a real command shown end-to-end: source javadoc and rendered terminal output.
