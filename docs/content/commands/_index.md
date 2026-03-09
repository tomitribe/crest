---
title: "Commands"
description: "Core annotations and patterns for defining CLI commands, options, and argument handling in Crest."
weight: 2
---

Crest turns Java methods into CLI commands through a small set of annotations. A method annotated with `@Command` becomes a command. Its parameters become options and positional arguments. The framework handles parsing, type conversion, validation, and help generation automatically.

This section covers everything you need to define and organize commands:

- **[Annotations]({{< ref "annotations" >}})** -- `@Command`, `@Option`, `@Default`, and `@Required` form the foundation of every CLI tool built with Crest.
- **[Options Classes]({{< ref "options-classes" >}})** -- Bundle related options into reusable `@Options` classes and compose them across commands.
- **[Command Groups]({{< ref "command-groups" >}})** -- Organize commands into groups with sub-commands using class-level `@Command`.
- **[Return Types]({{< ref "return-types" >}})** -- Choose from `String`, `StreamingOutput`, `PrintOutput`, `Stream`, and more to control command output.
- **[I/O Streams]({{< ref "io-streams" >}})** -- Inject `stdin`, `stdout`, and `stderr` into commands with `@In`, `@Out`, and `@Err`.
- **[Help]({{< ref "help" >}})** -- Built-in help command, man page generation, and option description sources.
