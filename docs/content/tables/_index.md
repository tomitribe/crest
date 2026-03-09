---
title: "Table Formatting"
description: "Format command output as structured tables with configurable borders, sorting, and field selection."
weight: 3
---

Crest can automatically format collection-based command output into structured tables. Commands that return `Stream`, `List`, `Set`, or arrays can be annotated with `@Table` to produce clean tabular output with configurable fields, sorting, borders, and headers.

This section covers the `@Table` annotation, available border styles, runtime table options, programmatic table building with `TableOutput.builder()`, and custom cell formatting through `PropertyEditor` registration.
