/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest;

import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.util.Files;
import org.tomitribe.crest.util.IO;
import org.tomitribe.crest.util.JarLocation;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class HelpTest extends Assert {

    @Test
    public void test() throws Exception {
        final Map<Class, Map<String, Cmd>> parsed = new HashMap<Class, Map<String, Cmd>>();

        final File base = getHelpBase();

        for (File file : base.listFiles()) {
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

    private void assertCommandHelp(Class clazz, Cmd cmd) throws IOException {
        final URL resource = clazz.getResource("help/" + helpFileName(clazz, cmd.getName()));
        if (resource == null) return;

        final String expected = IO.slurp(resource);

        final PrintString actual = new PrintString();
        cmd.help(actual);

        assertEquals(expected, actual.toString());

    }

    @Test
    @Ignore
    public void generateHelpTexts() throws Exception {
        final File helpBase = getHelpBase();

        final Set<Class> classes = getCommandClasses();

        for (Class clazz : classes) {

            final Map<String, Cmd> commands;
            try {
                commands = Commands.get(clazz);
            } catch (Exception e) {
                continue;
            }

            for (Cmd cmd : commands.values()) {
                final String name = cmd.getName();
                final File file = new File(helpBase, helpFileName(clazz, name));

                final PrintStream print = IO.print(file);

                try {
                    cmd.help(print);
                } catch (Exception e) {
                    print.close();
                    continue;
                }
            }
        }
    }

    public Class<?> load(String className) throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(className);
    }

    public Map<String, Cmd> getCommands(Map<Class, Map<String, Cmd>> parsed, final Class<?> aClass) throws ClassNotFoundException {
        final Map<String, Cmd> commands = parsed.get(aClass);
        if (commands != null) return commands;

        parsed.put(aClass, Commands.get(aClass));

        return getCommands(parsed, aClass);
    }

    public File getHelpBase() {
        final File testClasses = JarLocation.jarLocation(HelpTest.class);
        final File module = testClasses.getParentFile().getParentFile();
        return Files.file(module, "src", "test", "resources", "help");
    }

    public String helpFileName(Class clazz, String name) {
        return String.format("%s_%s.txt", clazz.getName(), name);
    }


    public static Set<Class> getCommandClasses() throws MalformedURLException {
        final File file = JarLocation.jarLocation(HelpTest.class);
        final Archive archive = ClasspathArchive.archive(Main.class.getClassLoader(), file.toURI().toURL());

        final AnnotationFinder finder = new AnnotationFinder(archive);

        final Set<Class> classes = new TreeSet<Class>(new Comparator<Class>() {
            @Override
            public int compare(Class o1, Class o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (Method method : finder.findAnnotatedMethods(Command.class)) {
            classes.add(method.getDeclaringClass());
        }
        return classes;
    }
}
