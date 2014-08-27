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

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;
import org.tomitribe.util.JarLocation;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class HelpTest extends Assert {


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
                          @Option("exclude") final Pattern exclude,
                          @Option("exclude-from") final File excludeFrom,
                          @Option("include") final Pattern include,
                          @Option("include-from") final File includeFrom,
                          @Option({"progress", "p"}) @Default("true") final boolean progress,
                          final URI[] sources,
                          final URI dest
        ) {

        }
    }

    @Test
    public void testRsync() throws Exception {
        assertCommandHelp(Rsync.class, "rsync");
    }


    public static class DefaultsAndEnums {

        @Command
        public void test(@Option({"r", "recursive"}) final boolean recursive,
                         @Option({"links", "l"}) final boolean links,
                         @Option({"p", "perms"}) final boolean perms,
                         @Option("owner") @Default("${user.name}") final String owner,
                         @Option("group") final boolean group,
                         @Option("devices") final boolean devices,
                         @Option("specials") final boolean specials,
                         @Option("times") final boolean times,
                         @Option("x") final boolean x,
                         @Option({"exclude", "e"}) final Pattern exclude,
                         @Option("exclude-from") final File excludeFrom,
                         @Option("include") @Default(".*") final Pattern include,
                         @Option("include-from") final File includeFrom,
                         @Option("highlight") @Default("orange") final Color highlight,
                         @Option("foreground") @Default("orange") final Color foreground,
                         @Option({"background", "bg"}) final Color background,
                         final URI source,
                         final URI dest
        ) {

        }
    }

    public static enum Color {
        red, green, blue, orange;
    }

    @Test
    public void testDefaultsAndEnums() throws Exception {
        assertCommandHelp(DefaultsAndEnums.class, "test");
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
                         @Option("exclude") final Pattern[] exclude,
                         @Option("exclude-from") final File excludeFrom,
                         @Option("include") @Default(".*") final Pattern include,
                         @Option("include-from") final File[] includeFrom,
                         @Option("highlight") @Default("orange,red") final Color[] highlight,
                         @Option("foreground") @Default("orange") final List<Color> foreground,
                         @Option("background") final Color[] background,
                         final URI source,
                         final URI dest
        ) {

        }
    }

    @Test
    public void testOptionLists() throws Exception {
        generateHelp(getHelpBase(), OptionLists.class);
        assertCommandHelp(OptionLists.class, "test");
    }

    @Test
    public void test() throws Exception {
        final Map<Class, Map<String, Cmd>> parsed = new HashMap<Class, Map<String, Cmd>>();

        final File base = getHelpBase();

        for (final File file : base.listFiles()) {
            final String[] split = file.getName().replace(".txt", "").split("_");
            final String className = split[0];
            final String commandName = split[1];

            final Class<?> clazz = load(className);
            final Map<String, Cmd> commands = getCommands(parsed, clazz);
            final Cmd cmd = commands.get(commandName);

            assertNotNull(cmd);

            assertCommandHelp(clazz, cmd);
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

    @Test
    @Ignore
    public void generateHelpTexts() throws Exception {
        final File helpBase = getHelpBase();

        final Set<Class> classes = getCommandClasses();

        for (final Class clazz : classes) {

            generateHelp(helpBase, clazz);
        }
    }

    public void generateHelp(final File helpBase, final Class clazz) throws FileNotFoundException {
        final Map<String, Cmd> commands;
        try {
            commands = Commands.get(clazz);
        } catch (final Exception e) {
            return;
        }

        for (final Cmd cmd : commands.values()) {
            final String name = cmd.getName();
            final File file = new File(helpBase, helpFileName(clazz, name));

            final PrintStream print = IO.print(file);

            try {
                cmd.help(print);
            } catch (final Exception e) {
                continue;
            } finally {
                print.close();
            }
        }
    }

    public Class<?> load(final String className) throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(className);
    }

    public Map<String, Cmd> getCommands(final Map<Class, Map<String, Cmd>> parsed, final Class<?> aClass) throws ClassNotFoundException {
        final Map<String, Cmd> commands = parsed.get(aClass);
        if (commands != null) {
            return commands;
        }

        parsed.put(aClass, Commands.get(aClass));

        return getCommands(parsed, aClass);
    }

    public File getHelpBase() {
        final File testClasses = JarLocation.jarLocation(HelpTest.class);
        final File module = testClasses.getParentFile().getParentFile();
        return Files.file(module, "src", "test", "resources", "help");
    }

    public String helpFileName(final Class clazz, final String name) {
        return String.format("%s_%s.txt", clazz.getName(), name);
    }


    public static Set<Class> getCommandClasses() throws MalformedURLException {
        final File file = JarLocation.jarLocation(HelpTest.class);
        final Archive archive = ClasspathArchive.archive(Main.class.getClassLoader(), file.toURI().toURL());

        final AnnotationFinder finder = new AnnotationFinder(archive);

        final Set<Class> classes = new TreeSet<Class>(new Comparator<Class>() {
            @Override
            public int compare(final Class o1, final Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (final Method method : finder.findAnnotatedMethods(Command.class)) {
            classes.add(method.getDeclaringClass());
        }
        return classes;
    }
}
