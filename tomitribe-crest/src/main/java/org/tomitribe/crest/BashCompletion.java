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
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.GlobalSpec;
import org.tomitribe.crest.cmds.OverloadedCmdMethod;
import org.tomitribe.crest.cmds.Spec;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.util.IO;
import org.tomitribe.util.Join;
import org.tomitribe.util.PrintString;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BashCompletion {

    private static final String COMPLETER = "_completer";
    final PrintString out = new PrintString();
    private final Main main;
    private String mainCommand;

    public BashCompletion(final Main main) {
        this.main = main;
    }

    @Command(COMPLETER)
    public String _completer(@Option("f") boolean toFile) {
        return _completer(toFile, getName());
    }

    @Command(COMPLETER)
    public String _completer(@Option("f") boolean toFile, final String name) {
        this.mainCommand = name;

        out.println("#!/bin/bash\n");

        utilities();

        root("_" + mainCommand, this.main.commands.values());

        out.println("\ncomplete -F _" + mainCommand + " " + mainCommand);

        if (toFile) {
            return asFile();
        } else {
            return out.toString();
        }
    }

    private String asFile() {
        try {
            final File file = File.createTempFile(String.format(".%s-completion-", mainCommand), ".sh");
            IO.copy(out.toByteArray(), file);
            return file.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate completion script for " + mainCommand, e);
        }
    }

    private String getName() {

        final String name = main.getName();
        if (name != null) {
            return name;
        }

        final Environment env = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        final PrintStream error = env.getError();

        error.println("Specify the bash executable name.  Acceptable methods in order of priority:\n" +
                " 1. passed as an argument to this command (e.g. _completion wombat)\n" +
                " 2. set via a -Dcmd system property (e.g. java -Dcmd=wombat, e.g. java -Dcmd=/some/path/wombat)\n" +
                " 3. set as a $CMD environment variable (e.g. export CMD=$0, e.g. export CMD=wombat, e.g. export CMD=/some/path/wombat)");

        throw new IllegalStateException("The bash executable name was not found");
    }

    public static String generate(final Main main, final String... args) {
        final BashCompletion bashCompletion = new BashCompletion(main);
        final Map<String, Cmd> cmds = Commands.get(bashCompletion);
        return (String) cmds.get(COMPLETER).exec(null, args);
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

        final Spec spec = cmdMethod.getSpec();

        if (hasFlags(spec)) {
            proposeFlags(spec, true);
        } else {
            out.println("  _propose_files");
        }

        out.println("}");
    }

    private void globalFlags(final String functionName, final GlobalSpec globalSpec) {
        out.println("\nfunction " + functionName + "__global_flags() {");

        final Spec spec = globalSpec.getSpec();

        if (hasFlags(spec)) {
            proposeFlags(spec, false);
        }

        out.println("}");
    }

    private void proposeFlags(final Spec spec, final boolean proposeFiles) {

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
                out.printf("  %s*) _propose_flag_values %s ;;\n", flag(param.getName()), Join.join(" ", strings));
            } else {
                out.printf("  %s*) _propose_flag_file_values ;;\n", flag(param.getName()));
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

        if (proposeFiles) {
            out.println("  *) _propose_files ;;" );
        }

        out.println("  esac\n");

    }

    private static boolean hasFlags(final Spec spec) {
        return spec != null && spec.getOptions() != null && spec.getOptions().size() != 0;
    }

    private List<String> guessValues(final OptionParam param) {
        final List<String> values = getDefaults(param);

        final Class<?> type = param.getType();

        if (type.isPrimitive()) {
            values.remove(0);
        }
        if (Boolean.class.isAssignableFrom(type) || Boolean.TYPE.isAssignableFrom(type)) {

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

        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private static List<String> getDefaults(final OptionParam param) {
        final ArrayList<String> values = new ArrayList<>();
        values.add(param.getDefaultValue());
        values.addAll(param.getDefaultValues());
        return values.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
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
                Byte.TYPE,
                Character.class,
                Character.TYPE,
                Short.class,
                Short.TYPE,
                Integer.class,
                Integer.TYPE,
                Long.class,
                Long.TYPE,
                Float.class,
                Float.TYPE,
                Double.class,
                Double.TYPE
        };

        for (final Class<?> aClass : types) {
            if (aClass.isAssignableFrom(type)) {
                return true;
            }
        }
        return false;
    }

    private String flag(String s) {
        if (s.length() == 1) {
            return String.format("-%s=", s);
        } else {
            return String.format("--%s=", s);
        }
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

    private void root(final String functionName, final Collection<Cmd> commands) {
        final int depth = 2;
        final int depthPlusOne = depth + 1;
        final int depthMinusOne = depth - 1;

        final List<String> names = commands.stream()
                .map(Cmd::getName)
                .map(s -> "    " + s)
                .collect(Collectors.toList());

        out.println("function " + functionName + "() {\n" +
                "\n" +
                "  local cur=${COMP_WORDS[COMP_CWORD]}\n" +
                "\n" +
                "  # Find the index of the last global flag\n" +
                "  local LAST_GLOBAL_FLAG_INDEX=0\n" +
                "\n" +
                "  for ((i = 1; i < ${#COMP_WORDS[@]}; i++)); do\n" +
                "    [[ \"${COMP_WORDS[i]}\" != -* ]] && break\n" +
                "    ((LAST_GLOBAL_FLAG_INDEX++))\n" +
                "  done\n" +
                "\n" +
                "  # If the current completion is a flag and that is before any subsequent\n" +
                "  # commands, we do global flag completion.\n" +
                "  if [[ \"$cur\" == -* ]] && (( COMP_CWORD <= LAST_GLOBAL_FLAG_INDEX )); then\n" +
                "\n" +
                "    # Remove any command arguments so their flags do not influence\n" +
                "    # logic in _propose_flags that tries not to repeat flags\n" +
                "    COMP_WORDS=(\"${COMP_WORDS[@]:0:LAST_GLOBAL_FLAG_INDEX+1}\")\n" +
                "\n" +
                "    " + functionName + "__global_flags\n" +
                "    return\n" +
                "  fi\n" +
                "\n" +
                "  # If there are global flags, trim them out adjust the COMP_CWORD index\n" +
                "  if (( LAST_GLOBAL_FLAG_INDEX > 0 )); then\n" +
                "    COMP_WORDS=(\"${COMP_WORDS[0]}\" \"${COMP_WORDS[@]:LAST_GLOBAL_FLAG_INDEX+1}\")\n" +
                "    COMP_CWORD=$(( COMP_CWORD - LAST_GLOBAL_FLAG_INDEX  ))\n" +
                "  fi\n" +
                "\n" +
                "  local args_length=${#COMP_WORDS[@]}\n" +
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

        globalFlags(functionName, this.main.getGlobalSpec());

        for (final Cmd cmd : commands) {
            cmd(depthPlusOne, functionName, cmd);
        }
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

}
