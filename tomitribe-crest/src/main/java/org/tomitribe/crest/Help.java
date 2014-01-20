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
import org.tomitribe.util.PrintString;
import org.tomitribe.util.reflect.Classes;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class Help {

    private final Map<String, Cmd> commands;

    public Help(final Map<String, Cmd> commands1) {
        commands = commands1;
    }

    public static void optionHelp(final Class<Help> clazz, final String commandName, final Collection<CmdMethod.OptionParameter> optionParameters, PrintStream out) {

        final ResourceBundle general = ResourceBundle.getBundle(Classes.packageName(clazz) + ".OptionDescriptions");

        final List<Row> rows = new ArrayList<Row>(optionParameters.size());

        for (CmdMethod.OptionParameter optionParameter : optionParameters) {
            final String string = getDescription(general, commandName, optionParameter.getName());
            final Row row = new Row(optionParameter, string);
            rows.add(row);
        }

        final String format = "  %-" + widest(rows, 0, 20) + "s  %-" + widest(rows, 1, 1) + "s  %s%n";

        out.println("Options: ");
        out.printf(format, "", "(default)", "(description)");

        for (Row row : rows) {
            out.printf(format, row.columns.toArray());
        }
    }

    public static int widest(List<Row> rows, final int column, final int min) {
        int c1 = min;

        for (Row row : rows) {
            final String s = row.columns.get(column);
            final int length = (s == null) ? 0 : s.length();
            c1 = Math.max(c1, length);
        }

        return c1;
    }

    public static String getDescription(ResourceBundle general, String commandName, String name) {
        try {

            return general.getString(commandName + "." + name);

        } catch (MissingResourceException e) {

            try {

                return general.getString(name);

            } catch (MissingResourceException e1) {

                return "";
            }
        }
    }

    private static class Row {
        final List<String> columns = new ArrayList<String>(3);

        private Row(CmdMethod.OptionParameter parameter, String description) {
            if (boolean.class.equals(parameter.getType())) {
                columns.add("--[no-]" + parameter.getName());
                columns.add("true".equals(parameter.getDefaultValue()) ? "enabled" : "disabled");
            } else {
                columns.add(String.format("--%s=<%s>", parameter.getName(), parameter.getType().getSimpleName()));
                columns.add(parameter.getDefaultValue());
            }
            columns.add(description);
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
