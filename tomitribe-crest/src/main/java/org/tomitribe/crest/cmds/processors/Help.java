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
import org.tomitribe.util.PrintString;
import org.tomitribe.util.reflect.Classes;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.SortedSet;
import java.util.TreeSet;

public class Help {

    private final Map<String, Cmd> commands;

    public Help(final Map<String, Cmd> commands1) {
        commands = commands1;
    }

    public static void optionHelp(final Class<?> clazz, final String commandName,
                                  final Collection<OptionParam> optionParams, final PrintStream out) {
        if (optionParams.isEmpty()) {
            return;
        }

        final List<Item> items = getItems(clazz, commandName, optionParams);

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
