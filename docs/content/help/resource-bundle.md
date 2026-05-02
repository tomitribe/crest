---
title: "Resource Bundles"
description: "Centralize option descriptions in OptionDescriptions.properties for sharing across commands and i18n."
weight: 4
---

When the same option appears across many commands -- `--verbose`, `--dry-run`, `--config` -- repeating its description on every method is noise. A `ResourceBundle` named `OptionDescriptions.properties` in the same package as the command class lets you write each description once and reuse it.

This is also the source to use for translated help text.

## Placement

```
src/main/resources/com/example/cli/OptionDescriptions.properties
```

```properties
recursive=recurse into directories
links=copy symlinks as symlinks
verbose=enable verbose output
dry-run=show what would happen, but do not change anything
```

## Lookup order

For an option named `progress` on a command named `rsync`, Crest looks up:

1. `rsync.progress` -- command-specific override
2. `progress` -- bare option name

The command-specific key takes precedence, so a generic option can be specialized for one command:

```properties
progress=show progress during transfer
rsync.progress=don't show progress during transfer
```

## Priority

`OptionDescriptions.properties` is the highest-priority source for option descriptions. It overrides both `@Option(description)` and `@param` javadoc tags. See the [Annotation Descriptions]({{< ref "annotation-descriptions" >}}) page for the full priority chain.
