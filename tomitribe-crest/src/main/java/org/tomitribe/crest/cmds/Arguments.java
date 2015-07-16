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
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.targets.Substitution;
import org.tomitribe.util.Join;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Arguments {
    private static final Join.NameCallback<String> STRING_NAME_CALLBACK = new Join.NameCallback<String>() {
        @Override
        public String getName(final String object) {
            if (object.startsWith("-")) {
                return object;
            }
            if (object.length() > 1) {
                return "--" + object;
            }
            return "-" + object;
        }
    };

    public static class Needed {
        private int count;

        public Needed(int count) {
            this.count = count;
        }

        public void decr() {
            count--;
        }

        public int getCount() {
            return count;
        }
    }

    private final List<String> list = new ArrayList<String>();
    private final Map<String, String> options = new HashMap<String, String>();
    private final CmdMethod method;

    Arguments(final String[] rawArgs, final CmdMethod method) {
        this.method = method;

        final Map<String, String> defaults = method.getDefaults();
        final Map<String, String> supplied = new HashMap<String, String>();

        final List<String> invalid = new ArrayList<String>();
        final Set<String> repeated = new HashSet<String>();

        // Read in and apply the options specified on the command line
        for (final String arg : rawArgs) {
            if (arg.startsWith("--")) {
                getCommand("--", arg, defaults, supplied, invalid, repeated);
            } else if (arg.startsWith("-")) {
                getCommand("-", arg, defaults, supplied, invalid, repeated);
            } else {
                this.list.add(arg);
            }
        }

        checkInvalid(invalid);
        checkRequired(supplied);
        checkRepeated(repeated);

        interpret(defaults);

        this.options.putAll(defaults);
        this.options.putAll(supplied);

    }

    private void getCommand(final String defaultPrefix,
                            final String arg,
                            final Map<String, String> defaults,
                            final Map<String, String> supplied,
                            final List<String> invalid,
                            final Set<String> repeated)
    {
        String name;
        String value;
        String prefix = defaultPrefix;

        if (arg.indexOf("=") > 0) {
            name = arg.substring(arg.indexOf(prefix) + prefix.length(), arg.indexOf("="));
            if (!defaults.containsKey(name) && !method.getSpec().getAliases().containsKey(name)) {
                name = arg.substring(0, arg.indexOf("="));
                prefix = "";
            }
            value = arg.substring(arg.indexOf("=") + 1);
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
            if (arg.contains("=") && name.length() > 1) {
                invalid.add(prefix + name);
                return;
            }

            final Set<String> opts = new HashSet<String>();
            Collections.addAll(opts, name.split("(?!^)"));

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
                               final Set<String> repeated)
    {

        String name = optName;
        String value = optValue;

        if (!defaults.containsKey(name) && method.getSpec().getAliases().containsKey(name)) {
            // check the options to find see if name is an alias for an option
            // if it is, get the actual optionparam name
            name = method.getSpec().getAliases().get(name).getName();
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
            final String value = Substitution.format(method.getTarget(), method.getMethod(), entry.getValue(), method.getDefaultsFinder());
            map.put(entry.getKey(), value);
        }
    }

    private void checkInvalid(final List<String> invalid) {
        if (invalid.size() > 0) {
            throw new IllegalArgumentException("Unknown options: " + Join.join(", ", STRING_NAME_CALLBACK, invalid));
        }
    }

    private void checkRequired(final Map<String, String> supplied) {
        final List<String> required = new ArrayList<String>();
        for (final Param parameter : method.getSpec().getOptions().values()) {
            if (!parameter.isAnnotationPresent(Required.class)) {
                continue;
            }

            final Option option = parameter.getAnnotation(Option.class);
            for (final String optionValue : option.value()) {
                if (!supplied.containsKey(optionValue)) {
                    required.add(optionValue);
                }
            }
        }

        if (required.size() > 0) {
            throw new IllegalArgumentException("Required: " + Join.join(", ", STRING_NAME_CALLBACK, required));
        }
    }

    private void checkRepeated(final Set<String> repeated) {
        if (repeated.size() > 0) {
            throw new IllegalArgumentException("Cannot be specified more than once: " + Join.join(", ", repeated));
        }
    }

    public List<String> getList() {
        return list;
    }

    public Map<String, String> getOptions() {
        return options;
    }

    public void checkOptions() {
        if (!options.isEmpty()) {
            throw new IllegalArgumentException("Unknown arguments: " + Join.join(", ", STRING_NAME_CALLBACK, options.keySet()));
        }
    }
}
