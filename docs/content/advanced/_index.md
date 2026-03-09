---
title: "Advanced"
description: "Advanced features for type conversion, interceptors, validation, custom editors, and more."
weight: 4
---

Crest provides a rich set of advanced features beyond basic command definition. These capabilities let you build sophisticated CLI tools with custom type handling, cross-cutting concerns, input validation, and fine-grained control over the framework's behavior.

- **[Type Conversion]({{< ref "type-conversion" >}})** -- How Crest converts CLI strings to Java types, including the conversion chain, built-in types, and domain wrapper types.
- **[Interceptors]({{< ref "interceptors" >}})** -- Define cross-cutting concerns with `@CrestInterceptor` and attach them to commands via direct reference or custom annotations.
- **[Validation]({{< ref "validation" >}})** -- Bean Validation (JSR-380) integration with built-in file validators and custom constraint support.
- **[Editors]({{< ref "editors" >}})** -- Register custom `PropertyEditor` implementations with `@Editor` for CLI parsing and table display.
- **[Loader]({{< ref "loader" >}})** -- The central registry for commands, interceptors, and editors, with `META-INF/services` and `Main.builder()` integration.
- **[Exit Codes]({{< ref "exit-codes" >}})** -- Control process exit codes with `@Exit`-annotated exceptions.
