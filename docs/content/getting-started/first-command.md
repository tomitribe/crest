---
title: "Your First Command"
description: "Write a simple CLI command using @Command, @Option, and @Default."
weight: 3
---

## A Simple Hello Command

In Crest, a public method annotated with `@Command` becomes a CLI command. The method name is the command name, and its parameters become the command's options and arguments.

```java
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

public class HelloCommand {

    @Command
    public String hello(@Option("name") @Default("World") final String name,
                        @Option("greeting") @Default("Hello") final String greeting) {
        return greeting + ", " + name + "!";
    }
}
```

This single class gives you a fully working CLI command with named options, default values, and automatic help generation.

## Running the Command

With no arguments, the defaults kick in:

```bash
$ myapp hello
Hello, World!
```

Override one or both options:

```bash
$ myapp hello --name=Alice
Hello, Alice!

$ myapp hello --name=Alice --greeting=Hi
Hi, Alice!
```

## Built-in Help

Crest automatically registers a `help` command. Running `help` lists all available commands with their descriptions:

```bash
$ myapp help
Commands:

   hello
   help
```

Running `help` on a specific command shows its full usage, including every option and its default value:

```bash
$ myapp help hello
```

To add a description that appears in the command listing, use the `description` parameter on `@Command`:

```java
@Command(description = "Greet someone by name")
public String hello(@Option("name") @Default("World") final String name,
                    @Option("greeting") @Default("Hello") final String greeting) {
    return greeting + ", " + name + "!";
}
```

Now the listing shows:

```bash
$ myapp help
Commands:

   hello   Greet someone by name
   help
```

## How It Works

- `@Command` marks the method as a CLI command. The method name (`hello`) becomes the command name.
- `@Option("name")` turns the parameter into a named option (`--name`).
- `@Default("World")` provides a fallback value when the option is not supplied.
- The return type is `String`, so Crest prints the returned value to stdout automatically.

Parameters without `@Option` are treated as positional arguments. Parameters without `@Default` are required unless their type is `boolean` (which defaults to `false`).
