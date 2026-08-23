/*
 * Copyright 2026 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.help;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * The fc6c4a9 fallback to bare usage is no longer silent.  The annotation
 * processor writes a properties resource for every command method it sees,
 * javadoc'd or not, so an absent resource means the class compiled without
 * annotation processing — the expected failure mode on JDK 23+, where javac
 * skips class-path processors by default.  The warning names the missing
 * resource and the exact maven-compiler-plugin recovery, once per run.
 *
 * A command that merely has no javadoc keeps its resource and renders its
 * bare usage without a word.
 */
public class JavadocHelpMissingResourceTest {

    @Test
    public void test() throws Exception {

        // A javadoc-less command with its resource intact renders silently
        {
            final TestEnvironment env = TestEnvironment.builder().build();
            new Main(Shell.class).main(env, new String[]{"help", "plain"});
            assertEquals("", env.getErr().toString());
        }

        // Delete the resource the processor generated, as a JDK 23+ build
        // without proc:full would never have written it
        final Method method = Shell.class.getMethod("orphan", String.class);
        final String resource = CommandJavadoc.getResourceFileName(method, "orphan");
        final URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        assertNotNull("expected the processor-generated resource " + resource, url);

        final Path file = Paths.get(url.toURI());
        final byte[] original = Files.readAllBytes(file);
        Files.delete(file);

        try {
            final TestEnvironment env = TestEnvironment.builder().build();
            new Main(Shell.class).main(env, new String[]{"help", "orphan"});

            assertEquals("\n" +
                    "Usage: orphan String\n" +
                    "\n", env.getOut().toString());

            final String err = env.getErr().toString();
            assertTrue(err, err.contains("WARNING: Expanded help is unavailable for command 'orphan'."));
            assertTrue(err, err.contains(resource));
            assertTrue(err, err.contains("<artifactId>maven-compiler-plugin</artifactId>"));
            assertTrue(err, err.contains("<proc>full</proc>"));
            assertTrue(err, err.contains("https://github.com/tomitribe/crest/issues/143"));

            // Once per run: a second render adds nothing
            final TestEnvironment again = TestEnvironment.builder().build();
            new Main(Shell.class).main(again, new String[]{"help", "orphan"});
            assertEquals("", again.getErr().toString());

        } finally {
            Files.write(file, original);
        }
    }

    public static class Shell {

        @Command
        public void plain(final String arg) {
        }

        /**
         * The orphan command has javadoc, but the test deletes the compiled
         * resource carrying it to simulate a build that skipped annotation
         * processing.
         */
        @Command
        public void orphan(final String arg) {
        }
    }
}
