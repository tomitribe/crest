---
title: "Installation"
description: "Add Crest to your project with Maven coordinates or generate a new project with the archetype."
weight: 1
---

## Maven Dependencies

Crest is split into two artifacts: the runtime and the API. Add both to your `pom.xml`:

```xml
<dependency>
    <groupId>org.tomitribe</groupId>
    <artifactId>tomitribe-crest</artifactId>
</dependency>
<dependency>
    <groupId>org.tomitribe</groupId>
    <artifactId>tomitribe-crest-api</artifactId>
</dependency>
```

The `tomitribe-crest-api` artifact contains the annotations (`@Command`, `@Option`, `@Default`, etc.) and interfaces your code compiles against. The `tomitribe-crest` artifact is the runtime that discovers commands, parses arguments, and invokes methods.

## Maven Archetype

To scaffold a new Crest project quickly, use the Maven archetype:

```bash
mvn archetype:generate \
    -DarchetypeGroupId=org.tomitribe \
    -DarchetypeArtifactId=tomitribe-crest-archetype
```

This generates a project with the correct dependencies and a sample command class — but no `main()` method of its own. `org.tomitribe.crest.Main` is the entry point: the crest-maven-plugin's `descriptor` goal records the command classes at build time and its `executable` goal produces a self-executing binary, so after `mvn package` the tool runs as `./target/<artifactId> <command>`. The sample command also demonstrates typed positional arguments — a small `Name` value object rather than a bare `String` — so the usage line names what the argument is.
