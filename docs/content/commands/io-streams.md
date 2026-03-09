---
title: "I/O Streams"
description: "Inject stdin, stdout, and stderr into Crest commands using @In, @Out, and @Err annotations."
weight: 5
---

Crest commands can receive the standard I/O streams -- stdin, stdout, and stderr -- as injected parameters. This gives commands direct control over input and output when return types alone are not sufficient.

## @Out

Injects a `PrintStream` connected to stdout. Use it when a command needs to write output incrementally rather than returning a single value.

```java
@Command
public void deploy(@Out final PrintStream out,
                   @Option("target") final String target) {
    out.println("Deploying to " + target);
    performDeploy(target);
    out.println("Deployment complete");
}
```

## @Err

Injects a `PrintStream` connected to stderr. Use it for error messages, warnings, and diagnostic output that should not mix with normal stdout content.

```java
@Command
public void process(@Out final PrintStream out,
                    @Err final PrintStream err,
                    @Option("input") final File input) {
    if (!input.exists()) {
        err.println("Warning: input file not found, using defaults");
    }
    out.println("Processing...");
}
```

## @In

Injects an `InputStream` connected to stdin. Use it when a command needs to read interactive input or piped data.

```java
@Command
public void load(@In final InputStream in,
                 @Out final PrintStream out) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = reader.readLine()) != null) {
        out.println("Read: " + line);
    }
}
```

## Combining I/O Annotations

A single command can use any combination of `@In`, `@Out`, and `@Err` alongside regular options and arguments:

```java
@Command
public void transform(@In final InputStream in,
                      @Out final PrintStream out,
                      @Err final PrintStream err,
                      @Option("format") @Default("json") final String format,
                      @Option("verbose") @Default("false") final boolean verbose) {
    if (verbose) {
        err.println("Reading from stdin, output format: " + format);
    }
    // Read from in, transform, write to out
}
```

## Hidden from Help

I/O stream parameters annotated with `@In`, `@Out`, and `@Err` are automatically hidden from help output. They do not appear in the command's option list or synopsis, since they are not user-facing options.

```java
@Command(description = "Process input data")
public void process(@Out final PrintStream out,
                    @Err final PrintStream err,
                    @Option("format") final String format) { ... }
```

Running `help process` shows only the `--format` option. The `@Out` and `@Err` parameters are invisible to the user.

## Testing with I/O Streams

The injected streams make commands easy to test. Use `Main.builder()` to redirect I/O:

```java
@Test
public void testOutput() {
    final PrintString out = new PrintString();
    final Main main = Main.builder()
            .command(MyCommands.class)
            .out(out)
            .build();

    main.run("deploy", "--target", "staging");

    assertTrue(out.toString().contains("Deploying to staging"));
}
```
