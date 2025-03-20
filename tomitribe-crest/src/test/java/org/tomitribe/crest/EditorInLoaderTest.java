/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest;

import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Editor;
import org.tomitribe.crest.api.Loader;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Archive;
import org.tomitribe.util.PrintString;

import java.beans.PropertyEditorSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class EditorInLoaderTest {


    /**
     * New inner classes must be added here
     */
    private final File common = Archive.archive()
            .add(ColorCommands.class)
            .add(EditorInLoader.class)
            .add(EnvironmentEditor.class)
            .add(Environment.class)
            .asDir();


    /**
     * CLI that gets the version from the standard MANIFEST.MF entries Maven creates
     */
    @Test
    public void test() throws Exception {

        final Cli cli = Cli.builder()
                .loader(EditorInLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                        .implementationVersion("3.6.9"))
                .addDir(common)
//                .debug()
                .build();

        {
            final Java.Result result = cli.run();
            assertEquals(String.format("Commands: %n" +
                    "                       %n" +
                    "   color               %n" +
                    "   help                %n" +
                    "%n" +
                    "Version 3.6.9%n" +
                    ""), result.getOut());
        }
        {
            final Java.Result result = cli.run("color", "red", "prod");
            assertEquals(String.format("%n" +
                    "crimson PRODUCTION%n"), result.getOut());
        }
        {
            final Java.Result result = cli.run("color", "green", "-h");
            assertEquals(String.format("Unknown options: -h%n" +
                    "%n" +
                    "Usage: color green [options]%n" +
                    "%n" +
                    "Options: %n" +
                    "  --value=<Environment>     enum: prod, dev%n" +
                    "%n" +
                    "Version 3.6.9%n"), result.getErr());
        }
    }

    @Test
    public void argumentHelp() throws Exception {

        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(ColorCommands.class)
                .command(EnvironmentEditor.class)
                .name("color")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        main.run("color", "red");
        assertEquals(String.format("Missing argument: Environment%n" +
                "%n" +
                "Usage: color red  Environment%n" +
                "%n"), err.toString());
    }

    @Test
    public void argument() throws Exception {

        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(ColorCommands.class)
                .command(EnvironmentEditor.class)
                .name("color")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        {
            main.run("color", "red", "prod");
            assertEquals(String.format("crimson PRODUCTION%n"), out.toString());
        }
    }

    @Test
    public void option() throws Exception {

        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(ColorCommands.class)
                .command(EnvironmentEditor.class)
                .name("color")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        {
            main.run("color", "green", "--value=dev");
            assertEquals(String.format("forrest DEVELOPMENT%n"), out.toString());
        }
    }

    @Test
    public void optionHelp() throws Exception {

        final PrintString out = new PrintString();
        final PrintString err = new PrintString();
        final AtomicInteger exit = new AtomicInteger();
        final Main main = Main.builder()
                .command(ColorCommands.class)
                .command(EnvironmentEditor.class)
                .name("color")
                .version("23.5.6")
                .out(out)
                .err(err)
                .exit(exit::set)
                .build();

        {
            main.run("color", "green", "--h");
            assertEquals(String.format("Unknown options: --h%n" +
                    "%n" +
                    "Usage: color green [options]%n" +
                    "%n" +
                    "Options: %n" +
                    "  --value=<Environment>     enum: prod, dev%n" +
                    "%n" +
                    "color 23.5.6%n"), err.toString());
        }
    }


    @Command("color")
    public static class ColorCommands {

        @Command
        public String red(final Environment arg) {
            return "crimson " + arg;
        }

        @Command
        public String green(@Option("value") final Environment value) {
            return "forrest " + value;
        }

        @Command
        public String blue() {
            return "navy";
        }
    }


    public static class EditorInLoader implements Loader {

        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            classes.add(EnvironmentEditor.class);
            return classes.iterator();
        }
    }

    public enum Environment {
        PRODUCTION("prod"),
        DEVELOPMENT("dev");

        private final String name;

        Environment(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Editor(Environment.class)
    public static class EnvironmentEditor extends PropertyEditorSupport {

        public EnvironmentEditor() {
            System.out.println();
        }

        public EnvironmentEditor(final Object source) {
            super(source);
        }

        @Override
        public void setAsText(final String text) throws IllegalArgumentException {
            if (text == null || text.trim().isEmpty()) {
                throw new IllegalArgumentException("Environment value cannot be null or empty");
            }

            final Environment environment = Arrays.stream(Environment.values())
                    .filter(env -> env.getName().equalsIgnoreCase(text))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid environment: " + text));

            setValue(environment);
        }

        @Override
        public String getAsText() {
            final Environment environment = (Environment) getValue();
            return (environment != null) ? environment.getName() : "";
        }
    }

}
