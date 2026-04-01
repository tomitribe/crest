---
title: "Changelog"
description: "Release history for Crest."
weight: 1
---

A summary of features and significant enhancements in each release.

## 0.41

- **Multi-level command groups.** `@Command("quote line-item")` on a class creates nested groups to any depth. Class and method paths are concatenated following the JAX-RS `@Path` model. Intermediate groups are auto-created. Multiple classes can contribute to the same deep group.
- **Constructor validation improvements.**

## 0.40

- **Hugo documentation site.**
- **Crest Maven Plugin** can now create executable jars.
- **`@Editor` support** in table output.
- Show negated boolean options in help.
- Class include/exclude filtering for command loading.

## 0.39

- **`@Command` descriptions** from Javadoc, extracted at compile time by annotation processor.

## 0.38

- **Split-class command groups.** Multiple classes can contribute sub-commands to the same `@Command` group.

## 0.37

- **`@GlobalOptions` annotation** for flags that apply across all commands.
- Global options support in bash completion.

## 0.36

- **Pluggable bean provider** via `TargetProvider` SPI for CDI and other DI frameworks.

## 0.35

- **`@Editor` support** loaded from `Loader` and used in help output.

## 0.34

- Help output matches method signature order.
- Fix help for overloaded commands.

## 0.33

- CSV and TSV table output no longer pads or wraps rows.

## 0.32

- Remove deprecated `Main.systemDefaults()` builder method.

## 0.31

- **Command name and version in help** from `Main.builder()` or `MANIFEST.MF`.
- Convenience `--csv` and `--tsv` flags for table output.

## 0.30

- **`Main.builder()` API** for programmatic CLI construction.

## 0.29

- **CSV table format** via `@Table`.

## 0.28

- **TSV table format** via `@Table`.

## 0.27

- Fix regression with object-based tables.
- Map key escaping and field name escaping for tables.

## 0.26

- **Map support** in table formatting.

## 0.25

- **Programmatic table builder API.**

## 0.24

- Case-insensitive field/column names in tables.

## 0.23

- Print help when `@Exit(help=true)` is set on exceptions.

## 0.22

- Allow commands to set table options.

## 0.21

- Allow commands to set table options at runtime.

## 0.20

- Prefer `@Options` constructor with Crest annotations.

## 0.19

- Javadoc fallback for help text.
- Archetype fixes.

## 0.18

- **`@Table` annotation** for automatic table formatting of return values.
- Table border styles, headers, field selection, sorting, and word wrapping.
- `Stream` return type support.

## 0.17

- **CDI-style custom interceptor annotations** via `@CrestInterceptor`.
- GraalVM native image support via Apache Arthur extension.

## 0.16

- Windows compatibility fixes.
- Dependency updates (ASM 9, JUnit 4.13.1).

## 0.15

- `@Exit` exceptions control reporting. Help is only printed when requested.

## 0.14

- **Table output** from `String[][]` return values with configurable borders.
- Man page piped to `less`.
- Auto-detect terminal width.

## 0.13

- Improved exception handling for all return types.

## 0.12

- **Bash completion script generation.**
- `Stream` return type support.

## 0.11

- `@DefaultMapping` made repeatable.
- Upgrade to Java 8 compilation.

## 0.10

- **System property and environment variable interpolation** for defaults.
- Nullable `@Options` support.
- Varargs with `@In`/`@Out` streams.

## 0.9

- `@Option(description)` for inline option descriptions.

## 0.8

- Bean validation fixes.
- Ctrl+C handling in CLI.

## 0.7

- **Bean Validation 1.1 support.**
- **Interactive CLI** with history, clear, and alias support.

## 0.6

- **Command interceptors** for cross-cutting concerns like security.

## 0.5

- **Crest Maven Plugin** for compile-time command discovery.
- `@Default` for prefixed bean parameters.
- `@In`/`@Out`/`@Err` stream injection.

## 0.4

- `@Exit` annotation for exception exit codes.
- `Iterable` return type support.

## 0.3

- Maven archetype for quick project setup.
