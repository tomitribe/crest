---
title: "Maven Project Setup"
description: "Add a CLI module to an existing Maven project using the shade plugin and crest-maven-plugin."
weight: 2
---

Most projects already have a multi-module Maven structure with domain logic, services, and APIs. To add a CLI powered by Crest, create a dedicated module that pulls in your existing code and packages it as an executable.

## Project Structure

A typical layout adds a `mytool-cli` module alongside your existing modules:

```
myproject/
  myproject-core/        # Domain logic, services
  myproject-api/         # Shared interfaces
  myproject-cli/         # CLI module (new)
    src/main/java/
      com/example/cli/
        MyCommands.java  # @Command methods
        MyLoader.java    # Loader implementation
    pom.xml
  pom.xml                # Parent POM
```

## CLI Module POM

The CLI module's `pom.xml` has three parts: dependencies on Crest and your project modules, the shade plugin to create an uber jar, and the crest-maven-plugin to generate descriptors and create the executable.

```xml
<project>
    <parent>
        <groupId>com.example</groupId>
        <artifactId>myproject</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>myproject-cli</artifactId>

    <dependencies>
        <!-- Your project modules -->
        <dependency>
            <groupId>com.example</groupId>
            <artifactId>myproject-core</artifactId>
            <version>${project.version}</version>
        </dependency>

        <!-- Crest runtime -->
        <dependency>
            <groupId>org.tomitribe</groupId>
            <artifactId>tomitribe-crest</artifactId>
        </dependency>
        <dependency>
            <groupId>org.tomitribe</groupId>
            <artifactId>tomitribe-crest-api</artifactId>
        </dependency>

        <!-- Classpath scanning (optional — see Loader below) -->
        <dependency>
            <groupId>org.tomitribe</groupId>
            <artifactId>tomitribe-crest-xbean</artifactId>
        </dependency>
    </dependencies>

    <build>
        <defaultGoal>package</defaultGoal>
        <plugins>
            <!-- 1. Create an uber jar with all dependencies -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.2.4</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <shadedArtifactAttached>true</shadedArtifactAttached>
                            <shadedClassifierName>all</shadedClassifierName>
                            <dependencyReducedPomLocation>
                                ${project.build.directory}/reduced-pom.xml
                            </dependencyReducedPomLocation>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>org.tomitribe.crest.Main</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>

            <!-- 2. Generate descriptors + create executable -->
            <plugin>
                <groupId>org.tomitribe</groupId>
                <artifactId>crest-maven-plugin</artifactId>
                <version>${crest.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>descriptor</goal>
                            <goal>executable</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

## What Each Plugin Does

### maven-shade-plugin

Creates a single jar containing all dependencies (your modules, Crest, transitive deps). The `ManifestResourceTransformer` sets `org.tomitribe.crest.Main` as the entry point so `java -jar` works. The `shadedArtifactAttached` option keeps the original jar and produces the uber jar with an `-all` classifier.

### crest-maven-plugin

Provides two goals:

**`descriptor`** — Runs at compile time to generate command descriptor files used by the built-in `help` command. These descriptors extract javadoc content for man-page-style documentation.

**`executable`** — Runs at package time to prepend a shell stub to the shaded jar, making it directly executable on Unix systems without typing `java -jar`. After `mvn package`, you get an executable named after your artifact:

```bash
$ ./myproject-cli --help
```

The default shell stub passes `-Dcmd="$0"` so Crest can show the script name in help output, and `$JAVA_OPTS` so users can set JVM flags via the environment. The executable is attached as a build artifact by default, so `mvn install` and `mvn deploy` publish it alongside the jar.

#### Executable Goal Configuration

All parameters have sensible defaults. Override them only when needed:

| Parameter | Default | Description |
|-----------|---------|-------------|
| `flags` | `-Dcmd="$0" $JAVA_OPTS` | JVM arguments embedded in the shell stub |
| `programFile` | `${project.artifactId}` | Name of the generated executable |
| `classifier` | `all` | Classifier of the shaded jar to use as input |
| `attachProgramFile` | `true` | Attach the executable as a build artifact |
| `scriptFile` | — | Path to a custom shell script instead of the default stub |
| `inputFile` | — | Specific jar file to use instead of finding by classifier |

Example with custom flags:

```xml
<plugin>
    <groupId>org.tomitribe</groupId>
    <artifactId>crest-maven-plugin</artifactId>
    <version>${crest.version}</version>
    <executions>
        <execution>
            <goals>
                <goal>descriptor</goal>
                <goal>executable</goal>
            </goals>
            <configuration>
                <flags>-Dcmd="$0" $JAVA_OPTS -Xmx2G</flags>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Build and Run

```bash
mvn package
./myproject-cli/target/myproject-cli help
```

The `target/` directory will contain:
- `myproject-cli-1.0-SNAPSHOT.jar` — original jar
- `myproject-cli-1.0-SNAPSHOT-all.jar` — uber jar with all dependencies
- `myproject-cli` — directly executable binary

## Command Discovery

Crest needs to find your `@Command` classes. You have three options:

### Option A: Classpath Scanning (easiest)

Add the `tomitribe-crest-xbean` dependency and Crest automatically scans the classpath for `@Command` classes. No additional configuration needed.

### Option B: Explicit Loader (recommended for large projects)

Create a `Loader` that lists your command, interceptor, and editor classes explicitly. This avoids classpath scanning overhead and gives you full control:

```java
public class MyLoader implements Loader {
    @Override
    public Iterator<Class<?>> iterator() {
        return Loader.of(
            DeployCommands.class,
            ConfigCommands.class,
            StatusCommands.class,
            AuditInterceptor.class,
            InstantEditor.class
        ).iterator();
    }
}
```

Register it in `META-INF/services/org.tomitribe.crest.api.Loader`:

```
com.example.cli.MyLoader
```

### Option C: Main.builder()

For full programmatic control, write your own `main()` method instead of using `org.tomitribe.crest.Main`:

```java
public class MyCli {
    public static void main(String[] args) throws Exception {
        new Main.builder()
                .command(DeployCommands.class)
                .command(ConfigCommands.class)
                .load(AuditInterceptor.class)
                .load(InstantEditor.class)
                .name("mytool")
                .version("1.0")
                .build()
                .run(args);
    }
}
```

Then update the shade plugin's `<mainClass>` to `com.example.cli.MyCli`.
