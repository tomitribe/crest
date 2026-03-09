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

This generates a project with the correct dependencies, a sample command class, and a `Main` entry point ready to run.
