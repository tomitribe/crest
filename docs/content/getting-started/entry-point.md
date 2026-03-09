---
title: "Entry Point"
description: "Configure how Crest discovers commands and launches your CLI application."
weight: 4
---

## The Main Class

Every Crest application needs an entry point that wires up command discovery and dispatches CLI arguments. The simplest approach uses the built-in `Main` class:

```java
import org.tomitribe.crest.Main;

public class MyCli {

    public static void main(final String[] args) throws Exception {
        final Main main = new Main();
        main.run(args);
    }
}
```

When constructed with `new Main()`, Crest discovers commands automatically via the ServiceLoader mechanism (described below). The `run()` method parses the arguments, finds the matching command, and invokes it.

## Main.builder() for Programmatic Setup

For more control, use `Main.builder()` to register commands explicitly and configure behavior:

```java
import org.tomitribe.crest.Main;

public class MyCli {

    public static void main(final String[] args) throws Exception {
        final Main main = Main.builder()
                .command(HelloCommand.class)
                .command(ConfigCommands.class)
                .name("myapp")
                .version("1.0.0")
                .build();

        main.run(args);
    }
}
```

Key builder options:

- `command(Class<?>)` -- register a class containing `@Command` methods.
- `load(Class<?>)` -- register any supporting class (custom editors, interceptors).
- `name(String)` -- set the application name shown in help output. Defaults to the value of system property `cmd` or environment variable `CMD`.
- `version(String)` -- set the version shown in help output.
- `out(PrintStream)` / `err(PrintStream)` / `in(InputStream)` -- redirect I/O streams.
- `exit(Consumer<Integer>)` -- custom exit handler (default: `System::exit`).
- `noexit()` -- suppress `System.exit` calls, useful for testing and embedded use.

If no classes are added via `command()`, the builder falls back to classpath discovery through the `Loader`.

## ServiceLoader Discovery

By default, `new Main()` uses Java's `ServiceLoader` to find an implementation of `org.tomitribe.crest.api.Loader`. The `Loader` interface returns all the classes Crest should inspect for commands, interceptors, and editors.

Create a `Loader` implementation:

```java
import org.tomitribe.crest.api.Loader;
import java.util.Iterator;

public class MyLoader implements Loader {
    @Override
    public Iterator<Class<?>> iterator() {
        return Loader.of(
            HelloCommand.class,
            ConfigCommands.class
        ).iterator();
    }
}
```

Register it in `META-INF/services/org.tomitribe.crest.api.Loader`:

```
com.example.cli.MyLoader
```

With this file on the classpath, `new Main()` automatically discovers all your commands -- no explicit registration needed.

## run() vs exec()

The `Main` class offers two execution methods:

- `main.run(args)` -- runs the command and handles exceptions internally. Use this for your application's `main()` method.
- `main.exec(args)` -- runs the command and returns the result. Exceptions propagate to the caller. Use this when you need the return value programmatically, such as in tests.
