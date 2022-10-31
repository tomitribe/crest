= CREST

Command-line API styled after JAX-RS

CREST allows you to get to the real work as quickly as possible when writing command line tools in Java.

 * 100% annotation based
 * Use Bean Validation or custom validators on use input
 * Contains Several builtin validations
 * Generates help from annotations
 * Supports default values
 * Use variable substitution on defaults
 * Supports lists and var-ags
 * Supports any java type, usually out of the box

Simply annotate the parameters of any Java method so it can be invoked from a command-line interface
 with near-zero additional work.  Command-registration, help text and validation is taken care of for you.

== Start your project

Use the Maven archetype and run your first command now.  Copy the following commands and paste them into your terminal.

----
mvn archetype:generate -DarchetypeGroupId=org.tomitribe -DarchetypeArtifactId=tomitribe-crest-archetype -DarchetypeVersion=0.22 -DgroupId=org.example -DartifactId=mycommand
cd mycommand/
mvn clean install
./target/mycommand hello
----
If all went well you should see the following output:

----
Hello, World!

----

Yes, you can actually create executable command-line programs in Java!

== Example: rsync as a Crest command

For example, to do something that might be similar to rsync in java, you could create the following
method signature in any java object.

[source,java]
----
package org.example.toolz;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;

public class AnyName {

    @Command
    public void rsync(@Option("recursive") boolean recursive,
                      @Option("links") boolean links,
                      @Option("perms") boolean perms,
                      @Option("owner") boolean owner,
                      @Option("group") boolean group,
                      @Option("devices") boolean devices,
                      @Option("specials") boolean specials,
                      @Option("times") boolean times,
                      @Option("exclude") Pattern exclude,
                      @Option("exclude-from") File excludeFrom,
                      @Option("include") Pattern include,
                      @Option("include-from") File includeFrom,
                      @Option("progress") @Default("true") boolean progress,
                      URI[] sources,
                      URI dest) {

        // TODO write the implementation...
    }
}
----

Some quick notes on `@Command` usage:

  - Multiple classes that use `@Command` are allowed
  - Muttiple `@Command` methods are allowed in a class
  - `@Command` methods in a class may have the same or different name
  - The command name is derived from the method name if not specified in `@Command`

=== Executing the Command

Pack this class in an uber jar with the Crest library and you could execute this command from the command line as follows:

[listing]
----
$ java -jar target/toolz-1.0.0-SNAPSHOT.jar rsync
Missing argument: URI...

Usage: rsync [options] URI... URI

Options:
  --devices
  --exclude=<Pattern>
  --exclude-from=<File>
  --group
  --include=<Pattern>
  --include-from=<File>
  --links
  --owner
  --perms
  --no-progress
  --recursive
  --specials
  --times
----

Of course, if we execute the command without the required arguments it will error out.  This is the value of Crest -- it does this dance for you.

In a dozen and more years of writing tools on different teams, two truths seem to prevail:

 - 90% of writing scripts is parsing and validating user input
 - Don't do that well and you'll be lucky if it gets more than six months of use

Computers are easy, humans are complex.  Let Crest deal with the humans, you just write code.

== Help Text

In the above example we have no details in our help other than what can be generated from inspecting the code.  To add actual descriptions to our
code we simply need to put an `OptionDescriptions.properties` in the same package as our class.

[listing]
----
#code
# <option> = <description>
# <command>.<option> = <description>
# The most specific key always wins

recursive      = recurse into directories
links          = copy symlinks as symlinks
perms          = preserve permissions
owner          = preserve owner (super-user only)
group          = preserve group
times          = preserve times
devices        = preserve device files (super-user only)
specials       = preserve special files
exclude        = exclude files matching PATTERN
exclude-from   = read exclude patterns from FILE
include        = don't exclude files matching PATTERN
include-from   = read include patterns from FILE
progress       = this is not the description that will be chosen
rsync.progress = don't show progress during transfer
----

Some quick notes on `OptionDescription.properties` files:

 - These are Java `java.util.ResourceBundle` objects, so i18n is supported
 - Use `OptionDescription_en.properties` and similar for Locale specific help text
 - In DRY spirit, every `@Command` in the package shares the same `OptionDescription` ResourceBundle and keys
 - Use `<command>.<option>` as the key for situations where sharing is not desired

With the above in our classpath, our command's help will now look like the following:

[listing]
----
$ java -jar target/toolz-1.0.0-SNAPSHOT.jar rsync
Missing argument: URI...

Usage: rsync [options] URI... URI

Options:
  --devices                 preserve device files (super-user only)
  --exclude=<Pattern>       exclude files matching PATTERN
  --exclude-from=<File>     read exclude patterns from FILE
  --group                   preserve group
  --include=<Pattern>       don't exclude files matching PATTERN
  --include-from=<File>     read include patterns from FILE
  --links                   copy symlinks as symlinks
  --owner                   preserve owner (super-user only)
  --perms                   preserve permissions
  --no-progress             don't show progress during transfer
  --recursive               recurse into directories
  --specials                preserve special files
  --times                   preserve times
----

== @Default values

Setting defaults to the `@Option` parameters of our `@Command` method can be done via the `@Default` annotation.  Using as
 simplified version of our `rsync`
 example, we might possibly wish to specify a default `exclude` pattern.


[source,java]
----
@Command
public void rsync(@Option("exclude") @Default(".*~") Pattern exclude,
                  @Option("include") Pattern include,
                  @Option("progress") @Default("true") boolean progress,
                  URI[] sources,
                  URI dest) {

    // TODO write the implementation...
}
----

Some quick notes about `@Option`:

 - `@Option` parameters are, by default, optional
 - When `@Default` is not used, the value will be its equivalent JVM default -- typically `0` or `null`
 - Add `@Required` to force a user to specify a value

Default values will show up in help output automatically, no need to update your `OptionDescriptions.properties`

[listing]
----
Usage: rsync [options] URI... URI

Options:
  --exclude=<Pattern>      exclude files matching PATTERN
                           (default: .*~)
  --include=<Pattern>      don't exclude files matching PATTERN
  --no-progress            don't show progress during transfer
----

=== Advanced

Default values also support interpolations:

[source,java]
----
@Command
public void myCommand(@Option("myoption") @Default("${env.MY_ENV_VAR}") String exclude) {
    // TODO write the implementation...
}
@Command
public void myCommand(@Option("myoption") @Default("${sys.MY_ENV_VAR}") String exclude) {
    // TODO write the implementation...
}
----

`env` is a prefix used to read the default in the environment variables and `sys` to read the system properties.

TIP: you can also register custom `DefaultsContext` in the interpolation registry using `META-INF/services/org.tomitribe.crest.contexts.DefaultsContext`
file to register it (just put a fully qualified implementation per line). The prefix will be the simple name of the implementation in lowercase. For instance
`org.company.MyEnv` will use `myenv`.

Finally the interpolation in such a form supports defaults:

[source,java]
----
@Command
public void myCommand(@Option("myoption") @Default("${env.MY_ENV_VAR:defaultIfEnvNotSet}") String exclude) {
    // TODO write the implementation...
}
----


== @Option Lists and Arrays

There are situations where you might want to allow the same flag to be specified twice.  Simply turn the `@Option` parameter into an
array or list that uses generics.

[source,java]
----
@Command
public void rsync(@Option("exclude") @Default(".*~") Pattern[] excludes,
                  @Option("include") Pattern include,
                  @Option("progress") @Default("true") boolean progress,
                  URI[] sources,
                  URI dest) {

    // TODO write the implementation...
}
----

The user can now specify multiple values when invoking the command by repeating the flag.

[source]
----
$ java -jar target/toolz-1.0.0-SNAPSHOT.jar rsync --exclude=".*\.log" --exclude=".*\.iml"  ...
----

== @Default @Option Lists and Arrays

Should you want to specify these two `exclude` values as the defaults, simply use a *comma* `,` to separate them in `@Default`

[source,java]
----
@Command
public void rsync(@Option("exclude") @Default(".*\\.iml,.*\\.iml") Pattern[] excludes,
                  @Option("include") Pattern include,
                  @Option("progress") @Default("true") boolean progress,
                  URI[] sources,
                  URI dest) {

}
----

If you happen to need comma for something, use *tab* `\t` instead.  When a tab is present in the `@Default` string, it becomes the preferred splitter.

[source,java]
----
@Command
public void rsync(@Option("exclude") @Default(".*\\.iml\t.*\\.iml") Pattern[] excludes,
                  @Option("include") Pattern include,
                  @Option("progress") @Default("true") boolean progress,
                  URI[] sources,
                  URI dest) {

}
----

If you happen to need both tab and comma for something (really????), use *unicode* zero `\u0000` instead.

[source,java]
----
@Command
public void rsync(@Option("exclude") @Default(".*\\.iml\u0000.*\\.iml") Pattern[] excludes,
                  @Option("include") Pattern include,
                  @Option("progress") @Default("true") boolean progress,
                  URI[] sources,
                  URI dest) {

}
----


== @Default and ${variable} Substitution

In the event you want to make defaults contextual, you can use `${some.property}` in the `@Default` string and
 the `java.lang.System.getProperties()` object to supply the value.

[source,java]
----
@Command
public void hello(@Option("name") @Default("${user.name}") String user) throws Exception
    System.out.printf("Hello, %s%n", user);
}
----

== Return Values

In the above we wrote to the console, which is fine for simple things but can make testing hard.  So far our commands are still POJOs and
nothing is stopping us from unit testing them as plain java objects -- except asserting output writen to `System.out`.

Simply return `java.lang.String` and it will be written to `System.out` for you.

[source,java]
----
@Command
public String hello(@Option("name") @Default("${user.name}") String user) throws Exception
    return String.format("Hello, %s%n", user);
}
----

In the event you need to write a significant amount of data, you can return `org.tomitribe.crest.api.StreamingOutput` which is an exact copy of the
equivalent JAX-RS http://docs.oracle.com/javaee/6/api/javax/ws/rs/core/StreamingOutput.html[StreamingOutput] interface.

[source,java]
----
@Command
public StreamingOutput cat(final File file) {
    if (!file.exists()) throw new IllegalStateException("File does not exist: " + file.getAbsolutePath());
    if (!file.canRead()) throw new IllegalStateException("Not readable: " + file.getAbsolutePath());
    if (!file.isFile()) throw new IllegalStateException("Not a file: " + file.getAbsolutePath());

    return new StreamingOutput() {
        @Override
        public void write(OutputStream output) throws IOException {
            final InputStream input = new BufferedInputStream(new FileInputStream(file));
            try {
                final byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                output.flush();
            } finally {
                if (input != null) input.close();
            }
        }
    };
}
----

Note a `null` check is not necessary for the `File file` parameter as Crest will not let the value of any plain argument be unspecified.  All parameters which do not use `@Option` are treated as required

== Stream injections

Command are often linked to console I/O. For that reason it is important to be able to interact
with Crest in/out/error streams. They are provided by the contextual `Environment` instance and using its thread local
you can retrieve them. However to make it easier to work with you can inject them as well.

Out stream (out and error ones) needs to be `PrintStream` typed and input is typed as a `InputStream`.
Just use these types as command parameters and decorate it with `@In`/`@Out`/`@Err`:

[source,java]
----
public class IOMe {
    @org.tomitribe.crest.api.Command
    public static void asserts(@In final InputStream in,
                               @Out final PrintStream out,
                               @Err PrintStream err) {
        // ...
    }
}
----

NOTE: using a parameter typed `Environment` you'll get it injected as well but this one is not in `crest-api`.

== Custom Java Types

You may have been seeing `File` and `Pattern` in the above examples and wondering exactly which Java classes Crest supports parameters to `@Command` methods.
The short answer is, any.  Crest does *not* use `java.beans.PropertyEditor` implementations by default like libraries such as Spring do.

After nearly 20 years of Java's existence, it's safe to say two styles dominate converting a `String` into a Java object:

 * A *Constructor* that take a single String as an argument.  Examples:
 ** `java.io.File(String)`
 ** `java.lang.Integer(String)`
 ** `java.net.URL(String)`
 * A *static method* that returns an instance of the same class.  Examples:
 ** `java.util.regex.Pattern.compile(String)`
 ** `java.net.URI.create(String)`
 ** `java.util.concurrent.TimeUnit.valueOf(String)`


Use either of these conventions and Crest will have no problem instantiating your object with the user-supplied `String` from the command-line args.

This should cover *95%* of all cases, but in the event it does not, you can create a `java.beans.PropertyEditor` and register it with the JVM.
Use your Google-fu to learn how to do that.

The order of precedence is as follows:

 1. Constructor
 2. Static method
 3. `java.beans.PropertyEditor`

== Custom Validation

If we look at our `cat` command we had earlier and yank the very boiler-plate read/write stream logic, all we have left is some code validating the user input.

[source,java]
----
@Command
public StreamingOutput cat(final File file) {
    if (!file.exists()) throw new IllegalStateException("File does not exist: " + file.getAbsolutePath());
    if (!file.canRead()) throw new IllegalStateException("Not readable: " + file.getAbsolutePath());
    if (!file.isFile()) throw new IllegalStateException("Not a file: " + file.getAbsolutePath());

    return new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException {
            IO.copy(file, os);
        }
    };
}
----

This validation code, too, can be yanked.  Crest supports the use of http://beanvalidation.org[Bean Validation] to validate `@Command` method
parameters.

[source,java]
----
@Command
public StreamingOutput cat(@Exists @Readable final File file) {
    if (!file.isFile()) throw new IllegalStateException("Not a file: " + file.getAbsolutePath());

    return new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException {
            IO.copy(file, os);
        }
    };
}
----

Here we've eliminated two of our very tedious checks with Bean Validation annotations that Crest provides out of the box, but we still have one more to
get rid of.  We can eliminate that one by writing our own annotation and using the Bean Validation API to wire it all together.

Here is what an annotation to do the `file.isFile()` check might look like -- let's call the annotation simply `@IsFile`


[source,java]
----
package org.example.toolz;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.tomitribe.crest.val.Exists;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Exists
@Documented
@javax.validation.Constraint(validatedBy = {IsFile.Constraint.class})
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface IsFile {
    Class<?>[] groups() default {};

    String message() default "{org.exampe.toolz.IsFile.message}";

    Class<? extends Payload>[] payload() default {};

    public static class Constraint implements ConstraintValidator<IsFile, File> {

        @Override
        public void initialize(IsFile constraintAnnotation) {
        }

        @Override
        public boolean isValid(File file, ConstraintValidatorContext context) {
            return file.isFile();
        }
    }
}
----

We can then update our code as follows to use this validation and eliminate all our boiler-plate.

[source,java]
----
@Command
public StreamingOutput cat(@IsFile @Readable final File file) {

    return new StreamingOutput() {
        @Override
        public void write(OutputStream os) throws IOException {
            IO.copy(file, os);
        }
    };
}
----

Notice that we also removed `@Exists` from the method parameter?  Since we put `@Exists` on the `@IsFile` annotation,
the `@IsFile` annotation effectively inherits the `@Exists` logic.
Our `@IsFile` annotation could inherit any number of annotations this way.

As the true strength of a great library of tools is the effort put into ensuring correct input, it's very wise to
bite the bullet and proactively invest in creating a reusable set of validation annotations to cover your typical input
types.

Pull requests are *very* strongly encouraged for any annotations that might be useful to others.

=== Bean Validation-less validations

You can also use the built-in crest validator style, it enables to lighten the dependencies by not requiring bean validation.
To do that you must:

. define a custom validation annotation
. implementation the validation as a `Consumer<ParamType>` or `BiConsumer<AnnotationDefinedIn1, ParamType>`

If the validation fails, the implementation just throws an exception with a meaningful error message.

Here is a trivial example to check a `Path` is a directory:

[source,java]
----
 @Target(PARAMETER)
 @Retention(RUNTIME)
 @Validation(CrestDirectory.Impl.class)
 public @interface CrestDirectory {
 }

 public class Impl implements Consumer<Path> {
   @Override
   public void accept(final Path file) {
       if (!Files.isDirectory(file)) {
           throw new IllegalStateException("'" + file + "' is not a directory");
       }
   }
}
----

TIP: the instances of the implementation are looked up by class in the `Environment` and if none matches a plain `new` is done calling the default constructor.

== Maven pom.xml setup

The following sample pom.xml will get you 90% of your way to fun with Crest and project
that will output a small uber jar with all the required dependencies.

[source,xml]
----
<?xml version="1.0"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.example</groupId>
  <artifactId>toolz</artifactId>
  <version>0.3-SNAPSHOT</version>

  <dependencies>
    <dependency>
      <groupId>org.tomitribe</groupId>
      <artifactId>tomitribe-crest</artifactId>
      <version>0.3-SNAPSHOT</version>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.10</version>
      <scope>test</scope>
    </dependency>

    <!-- Add tomitribe-crest-xbean if you want classpath scanning for @Command -->
    <dependency>
      <groupId>org.tomitribe</groupId>
      <artifactId>tomitribe-crest-xbean</artifactId>
      <version>0.3-SNAPSHOT</version>
    </dependency>
  </dependencies>

  <build>
    <defaultGoal>install</defaultGoal>
    <plugins>
      <plugin>
        <artifactId>maven-shade-plugin</artifactId>
        <version>2.1</version>
        <executions>
          <execution>
            <phase>package</phase>
            <goals>
              <goal>shade</goal>
            </goals>
            <configuration>
              <transformers>
                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                  <mainClass>org.tomitribe.crest.Main</mainClass>
                </transformer>
              </transformers>
            </configuration>
          </execution>
        </executions>
      </plugin>
    </plugins>
  </build>

  <repositories>
    <repository>
      <id>sonatype-nexus-snapshots</id>
      <name>Sonatype Nexus Snapshots</name>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>

</project>
----

== Bean Parameter Binding

If you don't want to inject in all your commands the same N parameters you can modelize them as an object.
Just use standard parameters as constructor parameters of the bean:

[source,java]
----
public class ColorfulCmd {
    @Command
    public static void exec(final Color color) {
        // ...
    }
}
----

To identify `Color` as an "option aware" parameter just decorate it with `@Options`:

[source,java]
----
@Options
public class Color { // getters omitted for brevity
    private final int r;
    private final int g;
    private final int b;
    private final int a;

    public Color(@Option("r") @Default("255") final int r,
                 @Option("g") @Default("255") final int g,
                 @Option("b") @Default("255") final int b,
                 @Option("a") @Default("255") final int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
}
----

=== Prefixing options

If you reuse the same parameter N times you'll probably want to prefix options. If we take previous example (`Params`)
you can desire to use `--background.r` and `--foreground.r` (same for g, b, a).

Just use `@Option` in the method parameter to do so:

[source,java]
----
public class ColorfulCmd {
    @Command
    public static void exec(@Option("background.") final Color colorBg, @Option("foreground.") final Color colorFg) {
        // ...
    }
}
----

NOTE: the '.' is not automatically added to allow you use to another convention like '-' or '_' ones for instance.

=== Override defaults

If you reuse the same parameter model accross command parameter you'll surely want to override some default in some cases.
For that purpose just use `@Defaults` and define the mappings you want:

[source,java]
----
public class ColorfulCmd {
    @Command
    public static void exec(@Defaults({
                                @Defaults.DefaultMapping(name = "r", value = "0"),
                                @Defaults.DefaultMapping(name = "g", value = "0"),
                                @Defaults.DefaultMapping(name = "b", value = "0"),
                                @Defaults.DefaultMapping(name = "a", value = "0")
                            })
                            @Option("background.")
                            final Color colorBg,

                            @Defaults({
                                @Defaults.DefaultMapping(name = "r", value = "255"),
                                @Defaults.DefaultMapping(name = "g", value = "255"),
                                @Defaults.DefaultMapping(name = "b", value = "255"),
                                @Defaults.DefaultMapping(name = "a", value = "255")
                            })
                            @Option("foreground.")
                            final Color colorFg) {
        // ...
    }
}
----

=== Interceptors

Sometimes you need to modify the command invocation or "insert" code before/after the command execution. For that purpose crest has some light
interceptor support.

Defining an interceptor is as easy as defining a class with:

[source,java]
----
public static class MyInterceptor {
    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        return crestContext.proceed();
    }
}
----

The constraint for an interceptor are:

- being decorated with `@CrestInterceptor`
- the method needs to be public
- the method needs to table a single parameter of type `CrestContext`

NOTE: you can pass `@CrestInterceptor` a value changing the key used to mark the interceptor.

To let a command use an interceptor or multiple ones just list them ordered in `interceptedBy` parameter:

[source,java]
----
@Command(interceptedBy = { MySecurityInterceptor.class, MyLoggingInterceptor.class, MyParameterFillingInterceptor.class })
public void test1(
         @Option("o1") final String o1,
         @Option("o2") final int o2,
         @Err final PrintStream err,
         @Out final PrintStream out,
         @In final InputStream is,
         @Option("o3") final String o3,
         final URL url) {
    // do something
}
----

Crest supports 3 styles of declaring interceptors

==== Via `@Command(interceptedBy)`

The `@Command` declaration uses the `interceptedBy` attribute to name the interceptor class.

[source,java]
----
public static class Foo {

    @Command(interceptedBy = GreenInterceptor.class)
    public String fighters(final String arg) {
        return arg;
    }
----

The `GreenInterceptor` definition is as usual

[source,java]
----
public class GreenInterceptor {

    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        return crestContext.proceed();
    }
}
----

==== Custom annotation containing `@CrestInterceptor(FooInterceptor.class)`

In this style, we define our own custom annotation `@Red` that names `RedInterceptor` directly

[source,java]
----
@CrestInterceptor(RedInterceptor.class)
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Red {
}
----

...and use it on our `@Command` method as follows

[source,java]
----
public static class Foo {

    @Red
    @Command
    public String fighters(final String arg) {
        return arg;
    }
----

The `RedInterceptor` definition is as usual

[source,java]
----
public class RedInterceptor {

    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        return crestContext.proceed();
    }
}
----

==== Custom annotation containing `@CrestInterceptor` loosely coupled to an implementation

In this style, we define our own custom annotation `@Blue`, but it is not bound to a specific implementation.  The `@CrestInterceptor` does not mention the class.

[source,java]
----
@CrestInterceptor
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Blue {
}
----

The `@Blue` is used on our `@Command` method as in the previous example

[source,java]
----
public static class Foo {

    @Blue
    @Command
    public String fighters(final String arg) {
        return arg;
    }
----

The `BlueInterceptor` definition identifies itself as the implementation of `@Blue` by using that annotation on its class

[source,java]
----
@Blue
public class BlueInterceptor {

    @CrestInterceptor
    public Object intercept(final CrestContext crestContext) {
        return crestContext.proceed();
    }
}
----

This can be useful if you create an API jar where `@Blue` might be contained, but you want to put the implementation in a different jar.  Perhaps there are different implementations, each it it's own jar, and people choose the implementation they want by including the desired implementation jar in the classpath.

In this approach, however, it is necessary to ensure `BlueInterceptor.class` is visible to Crest by creating a `Loader` implementation such as the following

[source,java]
----
package org.example.myapp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Loader implements org.tomitribe.crest.api.Loader {

    @Override
    public Iterator<Class<?>> iterator() {
        final List<Class<?>> classes = new ArrayList<>();
        classes.add(BlueInterceptor.class);
        return classes.listIterator();
    }
}
----

and declaring it in the jar at `META-INF/services/org.tomitribe.crest.api.Loader` with the following contents:

[source]
----
org.example.myapp.Loader
----


==== Example for security

Crest provides a `org.tomitribe.crest.interceptor.security.SecurityInterceptor` which
handles `@RolesAllowed` using the SPI `org.tomitribe.crest.interceptor.security.RoleProvider` to determine
if you can call or not the command contextually.

NOTE: `RoleProvider` is taken from `Environment` services. You can register it through `org.tomitribe.crest.environments.SystemEnvironment` constructor
and just set it as environment on `org.tomitribe.crest.environments.Environment.ENVIRONMENT_THREAD_LOCAL`.


Here a sample command using it:

[source,java]
----
@RolesAllowed("test")
@Command(interceptedBy = SecurityInterceptor.class)
public static String val() {
    return "ok";
}
----

== Maven Archetype

A maven archetype is available to quickly bootstrap small projects complete with the a pom like the above.  Save yourself some time on copy/paste then find/replace.

[listing]
----
mvn archetype:generate \
 -DarchetypeGroupId=org.tomitribe \
 -DarchetypeArtifactId=tomitribe-crest-archetype \
 -DarchetypeVersion=1.0.0-SNAPSHOT
----

== Maven Plugin

If you don't want to rely on runtime scanning to find classes but still want to avoid to list command classes or just reuse crest Main
you can use Maven Plugin to find it and generate a descriptor used to load classes.

Here is how to define it in your pom:

[source,xml]
----
<plugin>
  <groupId>org.tomitribe</groupId>
  <version>${crest.version}</version>
  <artifactId>crest-maven-plugin</artifactId>
    <executions>
      <execution>
        <goals>
          <goal>descriptor</goal>
        </goals>
      </execution>
    </executions>
</plugin>
----

== DeltaSpike Annotation Processor


Adding this dependency to your project:

[source,xml]
----
<dependency>
  <groupId>org.tomitribe</groupId>
  <artifactId>tomitribe-crest-generator</artifactId>
  <version>${crest.version}</version>
  <scope>provided</scope>
</dependency>
----

Crest Generator can integrates with DeltaSpike to generate binding pojo. It will split `@ConfigProperty` on first dot
and create one binding per prefix.

Here is an example:

[source,java]
----
public class DeltaspikeBean {
    @Inject
    @ConfigProperty(name = "app.service.base", defaultValue = "http://localhost:8080")
    private String base;

    @Inject
    @ConfigProperty(name = "app.service.retries")
    private Integer retries;
}
----

It will generate the following binding:

[source,java]
----
package org.tomitribe.crest.generator.generated;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.apache.deltaspike.core.api.config.ConfigResolver;
import org.apache.deltaspike.core.spi.config.ConfigSource;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import static java.util.Collections.singletonList;

public class App {
    private String serviceBase;
    private Integer serviceRetries;

    public App(
        @Option("service-base") @Default("http://localhost:8080") String serviceBase,
        @Option("service-retries") Integer serviceRetries) {
        final Map<String, String> ____properties = new HashMap<>();
        this.serviceBase = serviceBase;
        ____properties.put("app.service.base", String.valueOf(serviceBase));
        this.serviceRetries = serviceRetries;
        ____properties.put("app.service.retries", String.valueOf(serviceRetries));
        ConfigResolver.addConfigSources(Collections.<ConfigSource>singletonList(new ConfigSource() {
            @Override
            public int getOrdinal() {
                return 0;
            }

            @Override
            public Map<String, String> getProperties() {
                return ____properties;
            }

            @Override
            public String getPropertyValue(final String key) {
                return ____properties.get(key);
            }

            @Override
            public String getConfigName() {
                return "crest-app";
            }

            @Override
            public boolean isScannable() {
                return true;
            }
        }));    }

    public String getServiceBase() {
        return serviceBase;
    }

    public void setServiceBase(final String serviceBase) {
        this.serviceBase = serviceBase;
    }

    public Integer getServiceRetries() {
        return serviceRetries;
    }

    public void setServiceRetries(final Integer serviceRetries) {
        this.serviceRetries = serviceRetries;
    }

}
----

Then you just need to reuse it ad a crest command parameter:

[source,java]
----
@Command
public void myCommand(@Option("app-") final App app) {
  // ...
}
----

The nice thing is it will integrate with crest of course but also with DeltaSpike. It means the previous code
will also make DeltaSpike injection respecting `App` configuration (`--app-service-base=... --app-service-retries=3` for instance).

If you create a fatjar using TomEE embedded it means you can handle all your DeltaSpike configuration this way
and you just need to write a TomEE Embedded runner to get DeltaSpike configuration wired from the command line:

[source,java]
----
import org.apache.tomee.embedded.Main;

public final class Runner {
    @Command("run")
    public static void run(@Option("app-") App app) {
        Main.main(new String[] { "--as-war", "--single-classloader" } /*fatjar "as war" deployment*/);
        // automatically @Inject @ConfigProperty will be populated :)
    }
}
----

Potential enhancement(s):

- option to generate TomEE Embedded main?
- Tamaya integration on the same model?
- Owner integration
- ...

== Cli module

Cli module aims to provide a basic integration with JLine.

All starts from `org.tomitribe.crest.cli.api.CrestCli` class. Current version is extensible through inheritance but already provides:

- support of maven plugin commands (crest-commands.txt)
- JLine integration
- Basic pipping support (`mycommand | jgrep foo`)
- History support is you return a file in `org.tomitribe.crest.cli.api.CrestCli.cliHistoryFile`
- `org.tomitribe.crest.cli.api.interceptor.interactive.Interactivable` can be used to mark a parameter as required but compatible with interactive mode
(ie the parameter is asked in interactive mode if missing).

Sample usage:

[source,java]
----
final CrestCli cli = new CrestCli();
cli.run();
----

TIP: `CrestCli` also has a `main(String[])` so it can be used directly as well.

NOTE: if you don't provide an `exit` command one is added by default.

== GraalVM integration

Tomitribe Crest works very smoothly with GraalVM enabling you to get a native binary from your CLI.

You can do it writing manually your `reflections.json` but you can also do it through maven using `Apache Geronimo Arthur` plugin.
In this last case, you can set up your CLI auto-configuration with this setup:

IMPORTANT: requires Tomitribe Crest >= 0.17.

[source,xml]
----
<plugin>
  <groupId>org.apache.geronimo.arthur</groupId>
  <artifactId>arthur-maven-plugin</artifactId>
  <version>1.0.3</version>
  <configuration>
    <graalVersion>21.3.0.r17</graalVersion> <1>
    <main>org.tomitribe.crest.Main</main> <2>
    <extensionProperties> <4>
      <!-- starts with, excludes exists too, don't forget help if you don't override it yourself -->
      <tomitribe.crest.command.includes>
        com.superbiz.command,
        org.tomitribe.crest.cmds.processors.Help
      </tomitribe.crest.command.includes>
      <!-- <tomitribe.crest.editors.includes>....</tomitribe.crest.editors.includes> --> <5>
    </extensionProperties>
    <enableAllSecurityServices>false</enableAllSecurityServices> <6>
  </configuration>
  <dependencies> <3>
    <!-- enable crest auto registration for commands/interceptors -->
    <dependencies>
       <dependency>
         <groupId>org.tomitribe</groupId>
         <artifactId>tomitribe-crest-arthur-extension</artifactId>
         <version>${crest.version}</version>
       </dependency>
     </dependencies>
  </dependencies>
</plugin>
----
<.> Ensure to adjust the Graal and JVM base version (here Graal 21.3.0 in its Java 17 flavor),
<.> Reuse default Crest main,
<.> Enable crest extension for Arthur,
<.> Customize the command scanning, note that you can tune the includes/excludes and the values are comma separated and use a "start with" matching logic,
<.> If you are using `@Editor`, you can control the scanning there too similarly to commands,
<.> This option is deprecated in recent graal versions so avoid a warning using a recent version, no direct link with crest itself,

Then just run: `mvn install arthur:native-image` and you will get a `target/<artifctId>.graal.bin` binary you can share and execute on the built platform.

IMPORTANT: only scanned editors (`@Editor`) are handled by the extension, SPI ones (`META-INF/services/org.tomitribe.crest.api.Editor`) can be used if you register them within GraalVM configuration and enable `ServiceLoader`.

=== GraalVM example

[source,java]
.Cat.java
----
package org.superbiz.crest.demo;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Editor;
import org.tomitribe.util.editor.AbstractConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Cat {
    @Command(usage = "Cat a file.")
    public String cat(final Path file) throws IOException {
        return Files.readString(file);
    }

    @Editor(Path.class)
    public static class PathEditor extends AbstractConverter {
        @Override
        protected Object toObjectImpl(final String text) {
            return Paths.get(text);
        }
    }
}
----

[source,xml]
.pom.xml
----
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.superbiz.demo</groupId>
  <artifactId>demo-crest-arthur</artifactId>
  <version>1.0-SNAPSHOT</version>

  <properties>
    <crest.version>...</crest.version> <!-- >= 0.17 -->
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.tomitribe</groupId>
      <artifactId>tomitribe-crest</artifactId>
      <version>${crest.vesion}</version>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-resources-plugin</artifactId>
        <version>3.2.0</version>
        <configuration>
          <encoding>UTF-8</encoding>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.8.1</version>
        <configuration>
          <release>17</release>
          <source>17</source>
          <target>17</target>
        </configuration>
      </plugin>
      <plugin>
        <groupId>org.apache.geronimo.arthur</groupId>
        <artifactId>arthur-maven-plugin</artifactId>
        <version>1.0.3</version>
        <configuration>
          <graalVersion>21.3.0.r17</graalVersion>
          <main>org.tomitribe.crest.Main</main>
          <graalExtensions>
            <!-- enable crest auto registration for commands/interceptors -->
            <graalExtension>org.tomitribe:tomitribe-crest-arthur-extension:0.17-SNAPSHOT</graalExtension>
          </graalExtensions>
          <extensionProperties>
            <!-- starts with, excludes exists too -->
            <tomitribe.crest.command.includes>
              ${project.groupId}.,
              org.tomitribe.crest.cmds.processors.Help
            </tomitribe.crest.command.includes>
          </extensionProperties>
          <!-- this option is deprecated in recent graal versions -->
          <enableAllSecurityServices>false</enableAllSecurityServices>
        </configuration>
        <dependencies>
          <dependency> <!-- force arthur to support java 17 -->
            <groupId>org.apache.xbean</groupId>
            <artifactId>xbean-asm9-shaded</artifactId>
            <version>4.20</version>
          </dependency>
        </dependencies>
      </plugin>
    </plugins>
  </build>
</project>
----

Once this project created, you can run `mvn clean install arthur:native-image`.
This creates a `./target/demo-crest-arthur.graal.bin` binary and you can execute `cat` command using: `./target/demo-crest-arthur.graal.bin cat <some file>`.

=== Use Crest Maven Plugin Scanning

it is also possible to make Arthur extension use `crest-maven-plugin` scan goal (`descriptor`).
Just set extension property `tomitribe.crest.useInPlaceRegistrations` to `true`:

[source,xml]
----
<plugin>
  <groupId>org.tomitribe</groupId>
  <artifactId>crest-maven-plugin</artifactId>
  <version>${crest.version}</version>
  <executions>
    <execution>
      <id>scan</id>
      <phase>process-classes</phase>
      <goals>
        <goal>descriptor</goal>
      </goals>
    </execution>
  </executions>
</plugin>
<plugin>
  <groupId>org.apache.geronimo.arthur</groupId>
  <artifactId>arthur-maven-plugin</artifactId>
  <version>1.0.3</version>
  <configuration>
    <graalVersion>21.3.0.r17</graalVersion>
    <main>org.tomitribe.crest.Main</main>
    <graalExtensions>
      <graalExtension>org.tomitribe:tomitribe-crest-arthur-extension:0.17-SNAPSHOT</graalExtension>
    </graalExtensions>
    <extensionProperties>
      <!-- reuse crest maven plugin scanning -->
      <tomitribe.crest.useInPlaceRegistrations>true</tomitribe.crest.useInPlaceRegistrations>
    </extensionProperties>
    <enableAllSecurityServices>false</enableAllSecurityServices>
  </configuration>
  <dependencies>
    <dependency> <!-- force arthur to support java 17 -->
      <groupId>org.apache.xbean</groupId>
      <artifactId>xbean-asm9-shaded</artifactId>
      <version>4.20</version>
    </dependency>
  </dependencies>
</plugin>
----

This enables to use the same scanning for both tasks and therefore to have a common and unified scanning for java and native runs.
