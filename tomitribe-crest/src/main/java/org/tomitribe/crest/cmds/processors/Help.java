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
package org.tomitribe.crest.cmds.processors;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.help.CommandJavadoc;
import org.tomitribe.crest.help.Document;
import org.tomitribe.crest.help.DocumentParser;
import org.tomitribe.crest.help.Element;
import org.tomitribe.crest.help.Paragraph;
import org.tomitribe.crest.javadoc.Javadoc;
import org.tomitribe.crest.javadoc.JavadocParser;
import org.tomitribe.util.PrintString;
import org.tomitribe.util.reflect.Classes;

import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Help {

    private final Map<String, Cmd> commands;
    private final String version;
    private final String name;

    public Help(final Map<String, Cmd> commands, final String version, final String name) {
        this.commands = commands;
        this.version = version;
        this.name = name;
    }

    public static void optionHelp(final Method method, final String commandName,
                                  final Collection<OptionParam> optionParams, final PrintStream out) {
        if (optionParams.isEmpty()) {
            return;
        }

        final List<Item> items = getItems(method, commandName, optionParams);

        int width = 20;
        for (final Item item : items) {
            width = Math.max(width, item.getFlag().length());
        }

        final String format = "  %-" + width + "s     %s%n";

        out.println("Options: ");

        for (final Item item : items) {
            final List<String> lines = new ArrayList<>();

            if (item.getDescription() != null) {
                lines.add(item.getDescription());
            }

            lines.addAll(item.getNote());
            if (lines.isEmpty()) {
                lines.add("");
            }

            out.printf(format, item.getFlag(), lines.remove(0));
            for (final String line : lines) {
                out.printf(format, "", String.format("(%s)", line));
            }

//            out.println();
        }

        final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        final String name = environment.getName();
        final String version = environment.getVersion();

        if (name == null && version == null) {
            return;
        }

        if (name == null) {
            out.printf("%nVersion %s%n", version);
            return;
        }

        out.printf("%n%s %s%n", name, version);
    }

    public static List<Item> getItems(final Method method, final String commandName, final Collection<OptionParam> optionParams) {
        final CommandJavadoc commandJavadoc = CommandJavadoc.getCommandJavadocs(method, commandName);

        final List<Item> items = getItems(method, commandName, optionParams, commandJavadoc);

        return items.stream()
                .map(Help::trimDescriptions)
                .collect(Collectors.toList());
    }

    private static Item trimDescriptions(final Item item) {
        if (item.getDescription() == null) return item;

        final Document document = DocumentParser.parser(item.getDescription());

        if (document.getElements().size() == 0) return item;

        final Element element = document.getElements().get(0);

        if (!(element instanceof Paragraph)) return item;

        final String content = element.getContent();
        final String[] sentences = content.split("\\. ");

        if (sentences.length == 0) return item;

        return new Item(item.getFlag(), sentences[0], item.getParam(), item.getNote());
    }

    public static List<Item> getItems(final Method method, final String commandName, final Collection<OptionParam> optionParams, final CommandJavadoc commandJavadoc) {
        final Class<?> clazz = method.getDeclaringClass();
        final List<Item> items = getItems(clazz, commandName, optionParams);

        if (commandJavadoc == null || commandJavadoc.getJavadoc() == null) return items;

        final Javadoc javadoc = JavadocParser.parse(commandJavadoc.getJavadoc());

        final Map<String, Javadoc.Param> params = javadoc.getParametersByName();

        final ListIterator<Item> iterator = items.listIterator();
        while (iterator.hasNext()) {
            final Item item = iterator.next();

            if (item.getDescription() != null) continue;

            final String optionName = item.getParam().getName();
            final String javadocParameterName = commandJavadoc.getProperties().getProperty(optionName);

            if (javadocParameterName == null) continue;

            final Javadoc.Param param = params.get(javadocParameterName);

            if (param == null || param.getDescription() == null) continue;

            final Item updated = new Item(item.getFlag(), param.getDescription(), item.getParam(), item.getNote());

            iterator.set(updated);
        }

        return items;
    }

    public static List<Item> getItems(final Class<?> clazz, final String commandName, final Collection<OptionParam> optionParams) {
        ResourceBundle general = null; // lazily loaded cause breaks the annotation driven API so not considered as default

        final List<Item> items = new ArrayList<>(optionParams.size());

        for (final OptionParam optionParam : optionParams) {
            String description = optionParam.getDescription();
            if (description == null || description.isEmpty()) {
                if (general == null) {
                    general = getResourceBundle(clazz);
                }
                description = getDescription(general, commandName, optionParam.getName());
            }
            final Item item = new Item(optionParam, description);
            items.add(item);
        }
        return items;
    }

    public static ResourceBundle getResourceBundle(final Class<?> clazz) {
        try {
            return ResourceBundle.getBundle(Classes.packageName(clazz) + ".OptionDescriptions");
        } catch (final java.util.MissingResourceException ok) {
            return null;
        }
    }

    public static String getDescription(final ResourceBundle general, final String commandName, final String name) {
        if (general == null) {
            return null;
        }

        try {

            return general.getString(commandName + "." + name);

        } catch (final MissingResourceException e) {

            try {

                return general.getString(name);

            } catch (final MissingResourceException e1) {

                return null;
            }
        }
    }


    @Command
    public String help() {
        final PrintString string = new PrintString();
        string.println("Commands: ");
        string.printf("   %-20s", "");
        string.println();

        final SortedSet<String> strings = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(final String s1, final String s2) {
                assert null != s1;
                assert null != s2;
                return s1.compareTo(s2);
            }
        });

        strings.addAll(commands.keySet());

        for (final String command : strings) {
            string.printf("   %-20s%n", command);
        }

        return string.toString();
    }

    @Command
    public String help(final String name) {
        final Cmd cmd = commands.get(name);

        if (cmd == null) {
            return String.format("No such command: %s%n", name);
        }

        final PrintString out = new PrintString();
        cmd.manual(out);
        return out.toString();
    }

    @Command
    public String help(final String name, final String subCommand) {
        final Cmd cmd = commands.get(name);

        if (cmd == null) {
            return String.format("No such command: %s%n", name);
        }

        final PrintString out = new PrintString();

        if (cmd instanceof CmdGroup) {
            CmdGroup cmdGroup = (CmdGroup) cmd;
            cmdGroup.manual(subCommand, out);
        } else {
            cmd.manual(out);
        }

        return out.toString();
    }

}
