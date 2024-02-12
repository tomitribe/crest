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

import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Loader;
import org.tomitribe.crest.api.Option;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CommandNameAndVersionTest {

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
                .add(ColorCommands.class)
                .add(ColorLoader.class)
                .add(SystemPropertiesInLoader.class)
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
                .add(ColorCommands.class)
                .add(ColorLoader.class)
                .add(SystemPropertiesInLoader.class)
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
                .add(ColorCommands.class)
                .add(ColorLoader.class)
                .add(SystemPropertiesInLoader.class)
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
    @Ignore
    public void systemPropertyCmd() throws Exception {
        Java.Result result = Cli.builder()
                .loader(SystemPropertiesInLoader.class)
                .manifest(Manifest.builder()
                        .mainClass(Main.class)
                )
                .add(ColorCommands.class)
                .add(ColorLoader.class)
                .add(SystemPropertiesInLoader.class)
                .debug()
                .build()
                .run();

        assertEquals(String.format("Commands: %n" +
                "                       %n" +
                "   color               %n" +
                "   help                %n" +
                "%n" +
                "green 5.3.1%n" +
                ""), result.getOut());
    }

    @Test
    public void systemPropertyCmdName() throws Exception {
    }

    @Test
    public void systemPropertyCmdVersion() throws Exception {
    }

    @Test
    public void envCmdName() throws Exception {
    }

    @Test
    public void envCmdVersion() throws Exception {
    }

    @Test
    public void builderName() throws Exception {
    }

    @Test
    public void builderVersion() throws Exception {
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
        /* Set the command information as system properties */ {
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

    public static class ColorLoader implements Loader {
        @Override
        public Iterator<Class<?>> iterator() {
            final List<Class<?>> classes = new ArrayList<>();
            classes.add(ColorCommands.class);
            return classes.iterator();
        }
    }

}
