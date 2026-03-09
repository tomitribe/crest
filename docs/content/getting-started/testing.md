---
title: "Testing Commands"
description: "Test Crest commands in-process using Main.builder() and PrintString."
weight: 5
---

## Testing with Main.builder()

Crest commands are plain Java methods, but testing through the full CLI pipeline ensures that argument parsing, defaults, validation, and output all work correctly. The `Main.builder()` API makes this straightforward by letting you run commands in-process without ServiceLoader discovery or `System.exit` calls.

## Capturing Output with PrintString

Use `PrintString` from `tomitribe-util` to capture command output. Pass it to the builder via `out()`, then assert against its contents after running the command:

```java
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.util.PrintString;

import static org.junit.Assert.assertEquals;

public class HelloCommandTest {

    @Test
    public void testDefaultGreeting() throws Exception {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(HelloCommand.class)
                .out(out)
                .build();

        main.run("hello");

        assertEquals(String.format("Hello, World!%n"), out.toString());
    }

    @Test
    public void testCustomName() throws Exception {
        final PrintString out = new PrintString();
        final Main main = Main.builder()
                .command(HelloCommand.class)
                .out(out)
                .build();

        main.run("hello", "--name", "Alice");

        assertEquals(String.format("Hello, Alice!%n"), out.toString());
    }
}
```

## Asserting Full Output

Always assert the complete output with `assertEquals` rather than partial matches with `contains`. This catches unexpected extra output, missing newlines, and formatting regressions.

Use `String.format()` with `%n` for platform-independent newline characters. This ensures tests pass on both Unix and Windows:

```java
@Test
public void testHelpListing() throws Exception {
    final PrintString out = new PrintString();
    final Main main = Main.builder()
            .command(HelloCommand.class)
            .out(out)
            .build();

    main.run("help");

    assertEquals(String.format("Commands: %n" +
            "%n" +
            "   hello   Greet someone by name%n" +
            "   help     %n"), out.toString());
}
```

## Capturing Error Output

Use a separate `PrintString` for stderr when testing error scenarios:

```java
@Test
public void testErrorOutput() throws Exception {
    final PrintString out = new PrintString();
    final PrintString err = new PrintString();
    final Main main = Main.builder()
            .command(HelloCommand.class)
            .out(out)
            .err(err)
            .build();

    main.run("hello", "--unknown-flag");

    // Assert against err.toString() for error messages
}
```

## Getting Return Values with exec()

When a command returns a value and you want to inspect it directly rather than through printed output, use `exec()`:

```java
@Test
public void testReturnValue() throws Exception {
    final Main main = Main.builder()
            .command(HelloCommand.class)
            .build();

    final Object result = main.exec("hello", "--name", "Alice");

    assertEquals("Hello, Alice!", result);
}
```

## Key Testing Patterns

- **Use `Main.builder()`** to construct an isolated `Main` instance with only the commands under test.
- **Use `PrintString`** to capture stdout and stderr as strings for assertion.
- **Use `String.format("%n")`** for newlines in expected output so tests are platform-independent.
- **Assert full output** with `assertEquals`, not partial matches with `contains`.
- **Use `run()`** when testing void commands or side effects. Use `exec()` when you need the return value.
