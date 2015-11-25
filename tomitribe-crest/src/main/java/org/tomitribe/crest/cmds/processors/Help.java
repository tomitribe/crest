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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.util.Join;
import org.tomitribe.util.PrintString;
import org.tomitribe.util.reflect.Classes;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedList;
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
                                  final Collection<OptionParam> optionParams, final PrintStream out)
    {
        if (optionParams.size() == 0) {
            return;
        }

        ResourceBundle general = null; // lazily loaded cause breaks the annotation driven API so not considered as default

        final List<Item> items = new ArrayList<Item>(optionParams.size());

        int width = 20;
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

            width = Math.max(width, item.flag.length());
        }

        final String format = "  %-" + width + "s     %s%n";

        out.println("Options: ");

        for (final Item item : items) {
            final List<String> lines = new ArrayList<String>();

            if (item.description != null) {
                lines.add(item.description);
            }

            lines.addAll(item.note);
            if (lines.size() == 0) {
                lines.add("");
            }

            out.printf(format, item.flag, lines.remove(0));
            for (final String line : lines) {
                out.printf(format, "", String.format("(%s)", line));
            }

//            out.println();
        }
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


    private static class Item {

        private final String flag;
        private final List<String> note = new LinkedList<String>();
        private final String description;

        private Item(final OptionParam p, final String description) {
            this.description = description;
            final String prefix = p.getName().length() > 1 ? "--" : "-";
            
            final List<String> alias = new ArrayList<String>();

            Option option = p.getAnnotation(Option.class);
            for (int i = 1; i < option.value().length; i++) {
                final String aliasName = option.value()[i];
                alias.add(aliasName);
            }

            final boolean hasAlias = alias.size() > 0;
            final Class<?> type = p.getType();

            String defaultValue = p.getDefaultValue();

            final String name = p.getName();
            if (boolean.class.equals(type) || (Boolean.class.equals(type) && defaultValue != null)) {

                if ("true".equals(defaultValue)) {
                    this.flag = hasAlias ? Join.join(", ", "--no-" + name, getAlias(alias, false, true)) : "--no-" + name;
                } else {
                    final String optName = name.startsWith("-")? name : prefix + name;
                    this.flag = hasAlias ? Join.join(", ", optName, getAlias(alias, true, false)) : optName;
                }

                defaultValue = null;

            } else {
                final String optName = name.startsWith("-")? name : prefix + name;
                this.flag = hasAlias ? String.format("%s, %s=<%s>", optName, getAlias(alias, true, false), p.getDisplayType())
                            : String.format("%s=<%s>", optName, p.getDisplayType());
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

        private String getAlias(List<String> aliasList, boolean withDemiliter, boolean isBooleanValue) {
           StringBuilder sb = new StringBuilder();
           for (String alias : aliasList) {
               if (isBooleanValue) {
                   sb.append(", --no-" + alias);
               } else {
                   if (alias.length() > 1) {
                       sb.append(withDemiliter ? ", --" + alias : alias);
                   } else {
                       sb.append(withDemiliter ? ", -" + alias : alias);
                   }
               }
           }
           return sb.length() > 0 ? sb.toString().replaceFirst(", ","") : "";
        }
    }

    @Command
    public String help() {
        final PrintString string = new PrintString();
        string.println("Commands: ");
        string.printf("   %-20s", "");
        string.println();

        final SortedSet<String> strings = new TreeSet<String>(new Comparator<String>() {
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
        cmd.help(out);
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
            cmdGroup.help(subCommand, out);
        } else {
            cmd.help(out);
        }
        
        return out.toString();
    }

}
