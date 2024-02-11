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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainBuilderTest {

    @Test
    public void nameAndVersion() throws IOException {
        final File jar = Archive.archive()
                .add("META-INF/services/org.tomitribe.crest.api.Loader", "org.tomitribe.crest.MainBuilderTest.Cli")
                .add("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n" +
                        "Created-By: Maven JAR Plugin 3.3.0\n" +
                        "Build-Jdk-Spec: 11\n" +
                        "Specification-Title: Distribe :: CLI\n" +
                        "Specification-Version: 0.49\n" +
                        "Specification-Vendor: Tomitribe Corporation\n" +
                        "Implementation-Title: Distribe :: CLI\n" +
                        "Implementation-Version: 0.49-SNAPSHOT\n" +
                        "Implementation-Vendor: Tomitribe Corporation\n" +
                        "Main-Class: org.tomitribe.crest.Main\n")
                .add(CliLoader.class)
                .add(MainBuilderTest.class)
                .add(ColorCommands.class)
                .toJar();


    }

    public static class CliLoader implements Loader {
        //  Set the command information as system properties
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
}
