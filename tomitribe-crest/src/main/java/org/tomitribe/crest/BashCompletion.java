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

import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.OverloadedCmdMethod;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.util.Join;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BashCompletion {

    final PrintString out = new PrintString();
    private final String mainCommand;
    private final Main main;

    public BashCompletion(final Main main, final String... args) {
        this.main = main;

        this.mainCommand = getMainCommandName(args);


        out.println("#!/bin/bash\n");

        utilities();

        group(2, "_" + mainCommand, this.main.commands.values());

        out.println("\ncomplete -F _" + mainCommand + " " + mainCommand);

    }

    private String getMainCommandName(String... args) {
        // Specifying it explicitly wins
        if (args.length == 1) return asFilename(args[0]);

        { // Next look for a system property 'cmd'
            final String name = System.getProperty("cmd");
            if (name != null) return asFilename(name);
        }

        { // Next look for a system property 'cmd'
            final String name = System.getenv("CMD");
            if (name != null) return asFilename(name);
        }

        final Environment env = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        final PrintStream error = env.getError();

        error.println("Specify the bash executable name.  Acceptable methods in order of priority:\n" +
                " 1. passed as an argument to this command (e.g. _completion wombat)\n" +
                " 2. set via a -Dcmd system property (e.g. java -Dcmd=wombat, e.g. java -Dcmd=/some/path/wombat)\n" +
                " 3. set as a $CMD environment variable (e.g. export CMD=$0, e.g. export CMD=wombat, e.g. export CMD=/some/path/wombat)");

        throw new IllegalStateException("The bash executable name was not found");
    }

    private String asFilename(String name) {
        final File file = new File(name);
        return file.getName();
    }

    public static String generate(final Main main, final String... args) {
        return new BashCompletion(main, args).get();
    }

    private void cmd(final int depth, final String group, Cmd cmd) {
        if (cmd instanceof CmdGroup) {

            cmdGroup((CmdGroup) cmd, group, depth);

        } else if (cmd instanceof OverloadedCmdMethod) {

            overloadedCmdMethod((OverloadedCmdMethod) cmd, group);

        } else if (cmd instanceof CmdMethod) {

            cmdMethod((CmdMethod) cmd, group);

        } else {

            throw new IllegalStateException("Unknown cmd type: " + cmd.getClass().getName());
        }
    }

    private void cmdGroup(final CmdGroup cmdGroup, final String group, final int depth) {
        final String functionName = getFunctionName(group, cmdGroup);

        group(depth, functionName, cmdGroup.getCommands());
    }

    private void overloadedCmdMethod(final OverloadedCmdMethod overloadedCmdMethod, final String group) {
        final CmdMethod cmdMethod = overloadedCmdMethod.getMethods().iterator().next();
        cmdMethod(cmdMethod, group);
    }

    private void cmdMethod(final CmdMethod cmdMethod, final String group) {
        final String functionName = getFunctionName(group, cmdMethod);

        out.println("\nfunction " + functionName + "() {");

        final CmdMethod.Spec spec = cmdMethod.getSpec();

        if (hasFlags(spec)) {
            proposeFlags(spec);
        } else {
            out.println("  _propose_files");
        }

        out.println("}");
    }

    private void proposeFlags(final CmdMethod.Spec spec) {

        out.println("" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "\n" +
                "  case \"$cur\" in");

        final Collection<OptionParam> options = spec.getOptions().values();
        for (final OptionParam param : options) {

            final List<String> values = guessValues(param);

            if (values.size() > 0) {
                final List<String> strings = values.stream()
                        .map(this::quote)
                        .collect(Collectors.toList());
                out.printf("  --%s=*) _propose_flag_values %s ;;\n", param.getName(), Join.join(" ", strings));
            } else {
                out.printf("  --%s=*) _propose_flag_file_values ;;\n", param.getName());
            }
        }

        {
            final List<String> flags = options.stream()
                    .map(OptionParam::getName)
                    .map(this::flag)
                    .map(this::quote)
                    .collect(Collectors.toList());

            out.printf("  -*) _propose_flags %s;;\n", Join.join(" ", flags));
        }

        out.println("" +
                "  *) _propose_files ;;\n" +
                "  esac\n");

    }

    private static boolean hasFlags(final CmdMethod.Spec spec) {
        return spec != null && spec.getOptions() != null && spec.getOptions().size() != 0;
    }

    private List<String> guessValues(final OptionParam param) {
        final ArrayList<String> values = new ArrayList<>();
        values.add(param.getDefaultValue());
        values.addAll(param.getDefaultValues());

        final Class<?> type = param.getType();
        if (Boolean.class.isAssignableFrom(type)) {
            values.add("true");
            values.add("false");
        } else if (Enum.class.isAssignableFrom(type)) {
            final Class<Enum<?>> enumClass = (Class<Enum<?>>) type;
            final Enum<?>[] constants = enumClass.getEnumConstants();
            for (final Enum<?> constant : constants) {
                values.add(constant.name());
            }
        } else if (Pattern.class.isAssignableFrom(type) && values.size() == 0) {
            values.add("<regex>");
        } else if (isNonFile(type) && values.size() == 0) {
            values.add(String.format("<%s>", type.getSimpleName()));
        }
        return values;
    }

    /**
     * By default we will list files as the suggestion for parameter or option
     *
     * There a few scenarios this is pointless.  In these situations we will
     * simply suggest the type itself so the user has some indication of a good value.
     */
    private boolean isNonFile(final Class<?> type) {
        final Class<?>[] types = {
                URI.class,
                URL.class,
                Byte.class,
                Character.class,
                Short.class,
                Integer.class,
                Long.class,
                Float.class,
                Double.class,
                Integer.class
        };

        for (final Class<?> aClass : types) {
            if (aClass.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    private String flag(String s) {
        return String.format("--%s=", s);
    }


    private <T> String quote(T t) {
        return "\"" + t + "\"";
    }

    /**
     * This should really be recursive as we have command groups
     */
    private String getFunctionName(final String group, Cmd cmd) {
        final String safeCharacters = cmd.getName().replaceAll("[^a-zA-Z0-9]+", "");
        return group + "_" + safeCharacters;
    }

    private void proposeFiles() {
        out.println("\n" +
                "function _propose_files() {\n" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "  COMPREPLY=($(compgen -f \"$cur\"))\n" +
                "}\n"
        );
    }

    private void proposeFlags() {
        out.println("\n" +
                "function _propose_flags() {\n" +
                "  local FLAGS=\"$@\"\n" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "\n" +
                "  # minus flags we've used\n" +
                "  for ((i = 0; i < ${#COMP_WORDS[*]} - 1; i++)); do\n" +
                "    n=\"${COMP_WORDS[$i]}\"\n" +
                "    [[ \"$n\" == -* ]] && {\n" +
                "      n=\"${n/=*/=}\"\n" +
                "      FLAGS=(\"${FLAGS[@]/$n/}\")\n" +
                "    }\n" +
                "  done\n" +
                "\n" +
                "  COMPREPLY=($(compgen -W \"${FLAGS[*]}\" -- \"$cur\"))\n" +
                "}\n"
        );
    }

    private void proposeFlagValues() {
        out.println("\n" +
                "function _propose_flag_values() {\n" +
                "  local VALUES=\"$@\"\n" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "\n" +
                "  cur=\"$(echo \"$cur\" | perl -pe 's/[^=]+=//')\"\n" +
                "  COMPREPLY=($(compgen -W \"${VALUES[*]}\" \"$cur\"))\n" +
                "}\n"
        );
    }

    private void proposeFlagValuesFiles() {
        out.println("\n" +
                "function _propose_flag_file_values() {\n" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "\n" +
                "  cur=\"$(echo \"$cur\" | perl -pe 's/[^=]+=//')\"\n" +
                "  COMPREPLY=($(compgen -f \"$cur\"))\n" +
                "}\n"
        );
    }

    private void utilities() {
        proposeFiles();
        proposeFlags();
        proposeFlagValues();
        proposeFlagValuesFiles();
    }

    private void group(final int depth, final String functionName, final Collection<Cmd> commands) {
        final int depthPlusOne = depth + 1;
        final int depthMinusOne = depth - 1;

        final List<String> names = commands.stream()
                .map(Cmd::getName)
                .map(s -> "    " + s)
                .collect(Collectors.toList());

        out.println("function " + functionName + "() {\n" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "  local args_length=${#COMP_WORDS[@]}\n" +
                "\n" +
                "  local COMMANDS=(\n" +
                Join.join("\n", names) +
                "\n  )\n" +
                "\n" +
                "  # List the commands\n" +
                "  [ $args_length -lt " + depthPlusOne + " ] && {\n" +
                "    COMPREPLY=($(compgen -W \"${COMMANDS[*]}\" \"$cur\"))\n" +
                "    return\n" +
                "  }\n" +
                "\n" +
                "  # Command chosen.  Delegate to its completion function\n" +
                "\n" +
                "  # Verify the command is one we know and execute the\n" +
                "  # function that performs its completion\n" +
                "  local CMD=${COMP_WORDS[" + depthMinusOne + "]}\n" +
                "  for n in \"${COMMANDS[@]}\"; do\n" +
                "    [ \"$CMD\" = \"$n\" ] && {\n" +
                "      CMD=\"$(echo \"$CMD\" | perl -pe 's,[^a-zA-Z0-9],,g')\"\n" +
                "      " + functionName + "_$CMD\n" +
                "      return\n" +
                "    }\n" +
                "  done\n" +
                "\n" +
                "  COMPREPLY=()\n" +
                "}\n");

        for (final Cmd cmd : commands) {
            cmd(depthPlusOne, functionName, cmd);
        }
    }

    public String get() {
        return out.toString();
    }
}
