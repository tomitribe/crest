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
import org.tomitribe.crest.api.Loader;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.Archive;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandNameAndVersionTest {


    /**
     * New inner classes must be added here
     */
    private final File common = Archive.archive()
            .add(ColorCommands.class)
            .add(ColorLoader.class)
            .add(SystemPropertiesInLoader.class)
            .add(SystemPropertyCmdLoader.class)
            .add(SystemPropertyCmdNameLoader.class)
            .add(SystemPropertyCmdVersionLoader.class)
            .add(BuilderName.class)
            .add(BuilderVersion.class)
            .asDir();

    /**
     * CLI that gets the version from the standard MANIFEST.MF entries Maven creates
     */
    @Test
    public void manifestImplementationVersion() throws Exception {

        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                        .implementationVersion("3.6.9"))
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "Version 3.6.9%n" +
                ""), result.getOut());
    }

    /**
     * CLI that gets the version from the standard MANIFEST.MF entries Maven creates
     */
    @Test
    public void manifestCommandName() throws Exception {
        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                        .implementationVersion("5.16.1")
                        .commandName("blue"))
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "blue 5.16.1%n" +
                ""), result.getOut());
    }

    @Test
    public void manifestCommandVersion() throws Exception {
        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                        .implementationVersion("5.16.1")
                        .commandName("orange")
                        .commandVersion("2.4.6")
                )
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "orange 2.4.6%n" +
                ""), result.getOut());
    }

    @Test
    public void systemPropertyCmd() throws Exception {
        Java.Result result = Cli.builder()
                .loader(SystemPropertyCmdLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "blue 6.4.1%n" +
                ""), result.getOut());
    }

    @Test
    public void systemPropertyCmdName() throws Exception {
        Java.Result result = Cli.builder()
                .loader(SystemPropertyCmdNameLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "purple 7.9.8%n" +
                ""), result.getOut());
    }

    @Test
    public void systemPropertyCmdVersion() throws Exception {
        Java.Result result = Cli.builder()
                .loader(SystemPropertyCmdVersionLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "Version 9.2%n" +
                ""), result.getOut());
    }

    @Test
    public void envCmd() throws Exception {
        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .addDir(common)
                .env("CMD", "yellow")
                .env("CMD_VERSION", "1.2")
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "yellow 1.2%n" +
                ""), result.getOut());
    }


    @Test
    public void envCmdName() throws Exception {
        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .addDir(common)
                .env("CMD_NAME", "yellow")
                .env("CMD_VERSION", "1.2")
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "yellow 1.2%n" +
                ""), result.getOut());
    }

    @Test
    public void envCmdVersion() throws Exception {
        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .addDir(common)
                .env("CMD_NAME", "brown")
                .env("CMD_VERSION", "3.4")
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "brown 3.4%n" +
                ""), result.getOut());
    }

    @Test
    public void builderName() throws Exception {
        Java.Result result = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(BuilderName.class)
                )
                .addDir(common)
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "pink 3.12%n" +
                ""), result.getOut());
    }

    @Test
    public void builderVersion() throws Exception {
        final Cli cli = Cli.builder()
                .loader(ColorLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(BuilderVersion.class)
                )
                .addDir(common)
                .build();

        {
            Java.Result result = cli.run();
            assertEquals(String.format("Commands: %n" +
                    "                       %n" +
                    "   color               %n" +
                    "   help                %n" +
                    "%n" +
                    "Version 4.11%n" +
                    ""), result.getOut());
        }
        {
            Java.Result result = cli.run("color");
            assertEquals(String.format("Missing sub-command%n" +
                    "Usage: color [subcommand] [options]%n" +
                    "%n" +
                    "Sub commands: %n" +
                    "                       %n" +
                    "   blue                %n" +
                    "   green               %n" +
                    "   red                 %n" +
                    "%n" +
                    "Version 4.11%n"), result.getErr());
        }
    }

    @Command("color")
    public static class ColorCommands {

        @Command
        public String red(final String arg) {
            return "crimson " + arg;
        }

        @Command
        public String green(@Option("value") final String value) {
            return "forrest " + value;
        }

        @Command
        public String blue() {
            return "navy";
        }
    }


    public static class SystemPropertiesInLoader implements Loader {
        /* Set the command information as system properties */

        {
            System.setProperty("cmd.name", "green");
            System.setProperty("cmd.version", "5.3.1");
        }

        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            return classes.iterator();
        }
    }

    public static class SystemPropertyCmdLoader implements Loader {
        /* Set the command information as system properties */

        {
            System.setProperty("cmd", "blue");
            System.setProperty("cmd.version", "6.4.1");
        }

        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            return classes.iterator();
        }
    }
    public static class SystemPropertyCmdNameLoader implements Loader {
        /* Set the command information as system properties */

        {
            System.setProperty("cmd", "blue");
            System.setProperty("cmd.name", "purple"); // should have priority
            System.setProperty("cmd.version", "7.9.8");
        }

        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            return classes.iterator();
        }
    }

    public static class SystemPropertyCmdVersionLoader implements Loader {
        /* Set the command information as system properties */

        {
            System.setProperty("cmd.version", "9.2");
        }

        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            return classes.iterator();
        }
    }

    public static class ColorLoader implements Loader {
        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            return classes.iterator();
        }
    }

    public static class BuilderName {
        public static void main(String[] args) {
            Main.systemDefaults()
                    .command(ColorCommands.class)
                    .name("pink")
                    .version("3.12")
                    .build()
                    .run(args);
        }
    }

    public static class BuilderVersion {
        public static void main(String[] args) {
            Main.systemDefaults()
                    .command(ColorCommands.class)
                    .version("4.11")
                    .build()
                    .run(args);
        }
    }


}
