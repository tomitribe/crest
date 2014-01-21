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

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.util.Join;
import org.tomitribe.util.PrintString;
import org.tomitribe.util.reflect.Classes;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Help {

    private final Map<String, Cmd> commands;

    public Help(final Map<String, Cmd> commands1) {
        commands = commands1;
    }

    public static void optionHelp(final Class<?> clazz, final String commandName, final Collection<OptionParam> optionParams, PrintStream out) {
        if (optionParams.size() == 0) return;

        final ResourceBundle general = getResourceBundle(clazz);

        final List<Item> items = new ArrayList<Item>(optionParams.size());

        int width = 20;
        for (OptionParam optionParam : optionParams) {
            final String description = getDescription(general, commandName, optionParam.getName());
            final Item item = new Item(optionParam, description);
            items.add(item);

            width = Math.max(width, item.flag.length());
        }

        final String format = "  %-" + width + "s     %s%n";

        out.println("Options: ");

        for (Item item : items) {
            final List<String> lines = new ArrayList<String>();

            if (item.description != null) {
                lines.add(item.description);
            }

            lines.addAll(item.note);
            if (lines.size() == 0) lines.add("");

            out.printf(format, item.flag, lines.remove(0));
            for (String line : lines) {
                out.printf(format, "", String.format("(%s)", line));
            }

//            out.println();
        }
    }

    public static ResourceBundle getResourceBundle(Class<?> clazz) {
        try {
            return ResourceBundle.getBundle(Classes.packageName(clazz) + ".OptionDescriptions");
        } catch (java.util.MissingResourceException ok) {
            return null;
        }
    }

    public static String getDescription(ResourceBundle general, String commandName, String name) {
        if (general == null) return null;

        try {

            return general.getString(commandName + "." + name);

        } catch (MissingResourceException e) {

            try {

                return general.getString(name);

            } catch (MissingResourceException e1) {

                return null;
            }
        }
    }


    private static class Item {

        private final String flag;
        private final List<String> note = new LinkedList<String>();
        private final String description;

        private Item(OptionParam p, String description) {
            this.description = description;

            final Class<?> type = p.getType();

            String defaultValue = p.getDefaultValue();

            if (boolean.class.equals(type) || (Boolean.class.equals(type) && defaultValue != null)) {

                if ("true".equals(defaultValue)) {
                    this.flag = "--no-" + p.getName();
                } else {
                    this.flag = "--" + p.getName();
                }

                defaultValue = null;

            } else {
                this.flag = String.format("--%s=<%s>", p.getName(), p.getDisplayType());
            }

            if (defaultValue != null) {

                if (p.isListable()) {
                    final List<String> defaultValues = p.getDefaultValues();

                    if (defaultValues.size() > 0) {
                        this.note.add(String.format("default: %s", Join.join(", ", defaultValues)));
                    }

                } else {

                    this.note.add(String.format("default: %s", p.getDefaultValue()));

                }
            }

            if (Enum.class.isAssignableFrom(type)) {
                final Class<? extends Enum> enumType = (Class<? extends Enum>) type;
                final EnumSet<? extends Enum> enums = EnumSet.allOf(enumType);
                final String join = Join.join(", ", enums);
                this.note.add(String.format("enum: %s", join));
            }

        }
    }

    @Command
    public String help() {
        final PrintString string = new PrintString();
        string.println("Commands: ");
        string.printf("   %-20s", "");
        string.println();

        for (String command : commands.keySet()) {
            string.printf("   %-20s%n", command);
        }

        return string.toString();
    }

    @Command
    public String help(String name) {
        final Cmd cmd = commands.get(name);

        if (cmd == null) {
            return String.format("No such command: %s%n", name);
        }

        final PrintString out = new PrintString();
        cmd.help(out);
        return out.toString();
    }


}
