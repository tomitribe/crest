/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.targets.Substitution;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Arguments {
    private final List<String> list = new ArrayList<>();
    private final Map<String, String> options = new HashMap<>();
    private final Spec spec;
    private final DefaultsContext defaultsFinder;

    public Arguments(final DefaultsContext defaultsFinder, final Spec spec, final String[] rawArgs) {
        this.defaultsFinder = defaultsFinder;
        this.spec = spec;
        final Map<String, String> defaults = spec.getDefaults();
        final Map<String, String> supplied = new HashMap<>();

        final List<String> invalid = new ArrayList<>();
        final Set<String> repeated = new HashSet<>();

        // Read in and apply the options specified on the command line
        for (final String arg : rawArgs) {
            if (arg.startsWith("--")) {
                getCommand("--", arg, defaults, supplied, invalid, repeated);
            } else if (arg.startsWith("-")) {
                getCommand("-", arg, defaults, supplied, invalid, repeated);
            } else {
                this.getList().add(arg);
            }
        }

        checkInvalid(invalid);
        checkRequired(supplied);
        checkRepeated(repeated);

        interpret(defaults);

        this.getOptions().putAll(defaults);
        this.getOptions().putAll(supplied);

    }

    public List<String> getList() {
        return list;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    private void getCommand(final String defaultPrefix,
                            final String arg,
                            final Map<String, String> defaults,
                            final Map<String, String> supplied,
                            final List<String> invalid,
                            final Set<String> repeated) {
        String name;
        String value;
        String prefix = defaultPrefix;

        if (arg.indexOf('=') > 0) {
            name = arg.substring(arg.indexOf(prefix) + prefix.length(), arg.indexOf('='));
            if (!defaults.containsKey(name) && !spec.getAliases().containsKey(name)) {
                name = arg.substring(0, arg.indexOf('='));
                prefix = "";
            }
            value = arg.substring(arg.indexOf('=') + 1);
        } else {
            if (arg.startsWith("--no-")) {
                name = arg.substring(5);
                value = "false";
            } else {
                name = arg.substring(prefix.length());
                value = "true";
            }
        }

        if ("-".equals(prefix)) {

            // reject -del=true
            if (arg.indexOf('=') > -1 && name.length() > 1) {
                invalid.add(prefix + name);
                return;
            }

            final Set<String> opts = new HashSet<>();
            for (final String opt : name.split("(?!^)")) {
                opts.add(opt);
            }

            for (final String opt : opts) {
                processOption(prefix, opt, value, defaults, supplied, invalid, repeated);
            }
        }

        // reject --d and --d=true
        if ("--".equals(prefix)) {
            if (name.length() == 1) {
                invalid.add(prefix + name);
                return;
            }

            processOption(prefix, name, value, defaults, supplied, invalid, repeated);
        }
        if (prefix.isEmpty()) {
            processOption(prefix, name, value, defaults, supplied, invalid, repeated);
        }
    }

    private void processOption(final String prefix,
                               final String optName,
                               final String optValue,
                               final Map<String, String> defaults,
                               final Map<String, String> supplied,
                               final List<String> invalid,
                               final Set<String> repeated) {

        String name = optName;
        String value = optValue;

        if (!defaults.containsKey(name) && spec.getAliases().containsKey(name)) {
            // check the options to find see if name is an alias for an option
            // if it is, get the actual optionparam name
            name = spec.getAliases().get(name).getName();
        }

        if (defaults.containsKey(name)) {
            final boolean isList = defaults.get(name) != null && defaults.get(name).startsWith(OptionParam.LIST_TYPE);
            final String existing = supplied.get(name);

            if (isList) {

                if (existing == null) {

                    value = OptionParam.LIST_TYPE + value;

                } else {

                    value = existing + OptionParam.LIST_SEPARATOR + value;

                }

            } else if (existing != null) {

                repeated.add(name);
            }

            supplied.put(name, value);
        } else {
            invalid.add(prefix + name);
        }
    }

    private void interpret(final Map<String, String> map) {
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            final String value = Substitution.format(entry.getValue(), defaultsFinder);
            map.put(entry.getKey(), value);
        }
    }

    private void checkInvalid(final List<String> invalid) {
        if (!invalid.isEmpty()) {
            throw new IllegalArgumentException("Unknown options: " + Join.join(", ", CmdMethod.STRING_NAME_CALLBACK, invalid));
        }
    }

    private void checkRequired(final Map<String, String> supplied) {
        final List<String> required = new ArrayList<>();
        for (final Param parameter : spec.getOptions().values()) {
            if (!parameter.isAnnotationPresent(Required.class)) {
                continue;
            }

            final Option option = parameter.getAnnotation(Option.class);

            for (String optionValue : option.value()) {
                if (!supplied.containsKey(optionValue)) {
                    required.add(optionValue);
                }
            }
        }

        if (!required.isEmpty()) {
            throw new IllegalArgumentException("Required: " + Join.join(", ", CmdMethod.STRING_NAME_CALLBACK, required));
        }
    }

    private void checkRepeated(final Set<String> repeated) {
        if (!repeated.isEmpty()) {
            throw new IllegalArgumentException("Cannot be specified more than once: " + Join.join(", ", repeated));
        }
    }

    public static class Split {
        /**
         * All flags leading up to the command
         */
        private String[] global;

        /**
         * The command name
         */
        private String command;

        /**
         * The command arguments
         */
        private String[] args;

        public Split(final String[] global, final String command, final String[] args) {
            this.global = global;
            this.command = command;
            this.args = args;
        }

        public String[] getGlobal() {
            return global;
        }

        public String getCommand() {
            return command;
        }

        public String[] getArgs() {
            return args;
        }

        /**
         * Splits a single array of command line arguments into three parts:
         * <ul>
         *   <li><b>Global arguments</b> – all leading arguments beginning with a dash
         *       (e.g. <code>-v</code>, <code>--debug</code>).</li>
         *   <li><b>Command name</b> – the first non-dash argument.</li>
         *   <li><b>Command arguments</b> – all remaining arguments after the command name.</li>
         * </ul>
         *
         * <p>Examples:
         * <pre>{@code
         * Split s1 = Split.split(new String[]{"--verbose", "deploy", "--force", "prod"});
         * // global: ["--verbose"]
         * // command: "deploy"
         * // args: ["--force", "prod"]
         *
         * Split s2 = Split.split(new String[]{"--help"});
         * // global: ["--help"]
         * // command: null
         * // args: []
         * }</pre>
         *
         * @param args the full argument array, possibly {@code null}
         * @return a {@link Split} object containing the parsed segments
         */
        public static Split split(final String[] args) {
            if (args == null || args.length == 0) {
                return new Split(new String[0], null, new String[0]);
            }

            int index = 0;
            for (; index < args.length; index++) {
                final String arg = args[index];
                // Treat anything starting with '-' as global
                if (!arg.startsWith("-")) {
                    break;
                }
            }

            final String[] global = Arrays.copyOfRange(args, 0, index);

            if (index >= args.length) {
                // all args are global, no command
                return new Split(global, null, new String[0]);
            }

            final String command = args[index];
            final String[] commandArgs = Arrays.copyOfRange(args, index + 1, args.length);
            return new Split(global, command, commandArgs);
        }
    }
}
