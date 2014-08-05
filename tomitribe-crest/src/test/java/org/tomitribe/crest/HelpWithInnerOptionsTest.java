/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.util.IO;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class HelpWithInnerOptionsTest extends Assert {


    @Test
    public void testRsync() throws Exception {
        assertCommandHelp(Rsync.class, "rsync");
    }


    @Test
    public void testDefaultsAndEnums() throws Exception {
        assertCommandHelp(DefaultsAndEnums.class, "test");
    }

    @Test
    public void testOptionLists() throws Exception {
        assertCommandHelp(OptionLists.class, "test");
    }

    @Options
    public static class Patterns {

        public Patterns(
                @Option("exclude") final Pattern exclude,
                @Option("exclude-from") final File excludeFrom,
                @Option("include") final Pattern include,
                @Option("include-from") final File includeFrom
        ) {
        }
    }

    public static class Rsync {

        @Command
        public void rsync(@Option("recursive") final boolean recursive,
                          @Option("links") final boolean links,
                          @Option("perms") final boolean perms,
                          @Option("owner") final boolean owner,
                          @Option("group") final boolean group,
                          @Option("devices") final boolean devices,
                          @Option("specials") final boolean specials,
                          @Option("times") final boolean times,
                          Patterns patterns,
                          @Option("progress") @Default("true") final boolean progress,
                          final URI[] sources,
                          final URI dest
        ) {

        }
    }

    public static class DefaultsAndEnums {

        @Command
        public void test(@Option("recursive") final boolean recursive,
                         @Option("links") final boolean links,
                         @Option("perms") final boolean perms,
                         @Option("owner") @Default("${user.name}") final String owner,
                         @Option("group") final boolean group,
                         @Option("devices") final boolean devices,
                         @Option("specials") final boolean specials,
                         @Option("times") final boolean times,
                         Patterns patterns,
                         @Option("highlight") @Default("orange") final Color highlight,
                         @Option("foreground") @Default("orange") final Color foreground,
                         @Option("background") final Color background,
                         final URI source,
                         final URI dest
        ) {

        }
    }

    public static enum Color {
        red, green, blue, orange;
    }

    public static class OptionLists {

        @Command
        public void test(@Option("recursive") final List<Boolean> recursive,
                         @Option("links") final boolean[] links,
                         @Option("perms") final boolean perms,
                         @Option("owner") @Default("${user.name}") final String owner,
                         @Option("group") final boolean group,
                         @Option("devices") final boolean devices,
                         @Option("specials") final boolean specials,
                         @Option("times") final boolean times,
                         Patterns patterns,
                         @Option("highlight") @Default("orange,red") final Color[] highlight,
                         @Option("foreground") @Default("orange") final List<Color> foreground,
                         @Option("background") final Color[] background,
                         final URI source,
                         final URI dest
        ) {

        }
    }

    private void assertCommandHelp(final Class clazz, final String name) throws IOException {
        final Map<String, Cmd> commands = Commands.get(clazz);
        assertCommandHelp(clazz, commands.get(name));
    }

    private void assertCommandHelp(final Class clazz, final Cmd cmd) throws IOException {
        assertNotNull(cmd);
        final URL resource = clazz.getResource("/help/" + helpFileName(clazz, cmd.getName()));
        assertNotNull(resource);

        final String expected = IO.slurp(resource);

        final PrintString actual = new PrintString();
        cmd.help(actual);

        assertEquals(expected.replace("\r\n", "\n"), actual.toString().replace("\r\n", "\n"));

    }

    public Map<String, Cmd> getCommands(final Map<Class, Map<String, Cmd>> parsed, final Class<?> aClass) throws ClassNotFoundException {
        final Map<String, Cmd> commands = parsed.get(aClass);
        if (commands != null) {
            return commands;
        }

        parsed.put(aClass, Commands.get(aClass));

        return getCommands(parsed, aClass);
    }

    public String helpFileName(final Class clazz, final String name) {
        return String.format("%s_%s.txt", clazz.getName(), name);
    }


}
