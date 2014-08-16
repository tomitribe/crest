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

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Substitution;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.cmds.utils.CommandLine;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.val.BeanValidation;
import org.tomitribe.util.Join;
import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.reflect.Parameter;
import org.tomitribe.util.reflect.Reflection;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @version $Revision$ $Date$
 */
public class CmdMethod implements Cmd {

    private final Target target;
    private final Method method;
    private final String name;
    private final List<Param> parameters;
    private final DefaultsContext defaultsFinder;
    private final Spec spec = new Spec();

    public class Spec {
        private final Map<String, OptionParam> options = new TreeMap<String, OptionParam>();
        private final Map<String, OptionParam> aliases = new TreeMap<String, OptionParam>();
        private final List<Param> arguments = new LinkedList<Param>();
    }

    public CmdMethod(final Method method, final DefaultsContext defaultsFinder) {
        this(method, new SimpleBean(null), defaultsFinder);
    }

    public CmdMethod(final Method method, final Target target, final DefaultsContext defaultsFinder) {
        this.target = target;
        this.method = method;
        this.defaultsFinder = defaultsFinder;
        this.name = Commands.name(method);

        final List<Param> parameters = buildParams(Reflection.params(method));

        this.parameters = Collections.unmodifiableList(parameters);

        validate();
    }

    private List<Param> buildParams(final Iterable<Parameter> params) {
        final List<Param> parameters = new ArrayList<Param>();
        for (final Parameter parameter : params) {

            if (parameter.isAnnotationPresent(Option.class)) {

                final Option option = parameter.getAnnotation(Option.class);
                final String name = option.value()[0];
                final OptionParam optionParam = new OptionParam(parameter, name);

                final OptionParam existing = spec.options.put(optionParam.getName(), optionParam);

                if (existing != null) {
                    throw new IllegalArgumentException("Duplicate option: " + optionParam.getName());
                }
                
                for (int i = 1; i < option.value().length; i++) {
                    final String alias = option.value()[i];
                    final OptionParam existingAlias = spec.aliases.put(alias, optionParam);
                    
                    if (existingAlias != null) {
                        throw new IllegalArgumentException("Duplicate alias: " + alias);
                    }
                }

                parameters.add(optionParam);
            } else if (parameter.getType().isAnnotationPresent(Options.class)) {

                final ComplexParam complexParam = new ComplexParam(parameter);

                parameters.add(complexParam);

            } else {

                final Param e = new Param(parameter);
                spec.arguments.add(e);
                parameters.add(e);
            }
        }
        return parameters;
    }

    private class ComplexParam extends Param {

        private final List<Param> parameters;
        private final Constructor<?> constructor;

        private ComplexParam(Parameter parent) {
            super(parent);

            this.constructor = parent.getType().getConstructors()[0];

            this.parameters = Collections.unmodifiableList(buildParams(Reflection.params(constructor)));
        }

        public Object convert(final Arguments arguments, final Needed needed) {

            final List<Object> converted = CmdMethod.this.convert(arguments, needed, parameters);

            try {
                final Object[] args = converted.toArray();

                BeanValidation.validateParameters(constructor.getDeclaringClass(), constructor, args);

                return constructor.newInstance(args);

            } catch (InvocationTargetException e) {

                throw toRuntimeException(e.getCause());

            } catch (Exception e) {

                throw toRuntimeException(e);
            }
        }
    }

    public CmdMethod(final Method method, final Target target) {
        this(method, target, new SystemPropertiesDefaultsContext());
    }

    public Method getMethod() {
        return method;
    }

    public List<Param> getArgumentParameters() {
        return Collections.unmodifiableList(spec.arguments);
    }

    private void validate() {
        for (final Param param : spec.arguments) {
            if (param.isAnnotationPresent(Default.class)) {
                throw new IllegalArgumentException("@Default only usable with @Option parameters.");
            }
            if (!param.isListable() && param.isAnnotationPresent(Required.class)) {
                throw new IllegalArgumentException("@Required only usable with @Option parameters and lists.");
            }
        }
    }

    /**
     * Returns a single line description of the command
     */
    @Override
    public String getUsage() {
        final String usage = usage();

        if (usage != null) {
            if (!usage.startsWith(name)) {
                return name + " " + usage;
            } else {
                return usage;
            }
        }

        final List<Object> args = new ArrayList<Object>();

        for (final Param parameter : spec.arguments) {
            args.add(parameter.getDisplayType().replace("[]", "..."));
        }

        return String.format("%s %s %s", name, args.size() == method.getParameterTypes().length ? "" : "[options]",
                Join.join(" ", args)).trim();
    }

    private String usage() {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) {
            return null;
        }
        if ("".equals(command.usage())) {
            return null;
        }
        return command.usage();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object exec(final String... rawArgs) {
        final List<Object> list;
        try {
            list = parse(rawArgs);
        } catch (final Exception e) {
            reportWithHelp(e);
            throw toRuntimeException(e);
        }

        return exec(list);
    }

    public Object exec(final List<Object> list) {
        final Object[] args;
        try {
            args = list.toArray();
            BeanValidation.validateParameters(method.getDeclaringClass(), method, args);
        } catch (final Exception e) {
            reportWithHelp(e);
            throw toRuntimeException(e);
        }

        try {
            return target.invoke(method, args);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                reportWithHelp(e);
            }
            throw new CommandFailedException(cause);
        } catch (final Throwable e) {
            throw toRuntimeException(e);
        }
    }

    private void reportWithHelp(final Exception e) {
        final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();

        if (BeanValidation.isActive()) {
            for (final String message : BeanValidation.messages(e)) {
                err.println(message);
            }
        } else {
            err.println(e.getMessage());
        }
        help(err);
    }

    public static RuntimeException toRuntimeException(final Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new IllegalArgumentException(e);
    }

    public Map<String, OptionParam> getOptionParameters() {
        return Collections.unmodifiableMap(spec.options);
    }

    @Override
    public void help(final PrintStream out) {
        out.println();
        out.print("Usage: ");
        out.println(getUsage());
        out.println();

        Help.optionHelp(method.getDeclaringClass(), getName(), spec.options.values(), out);
    }

    public List<Object> parse(final String... rawArgs) {
        return convert(new Arguments(rawArgs));
    }

    public class Needed {
        private int count;

        public Needed(int count) {
            this.count = count;
        }
    }

    private <T> List<Object> convert(final Arguments args) {

        final Needed needed = new Needed(spec.arguments.size());

        final List<Object> converted = convert(args, needed, parameters);

        if (args.list.size() > 0) {
            throw new IllegalArgumentException("Excess arguments: " + Join.join(", ", args.list));
        }

        if (args.options.size() > 0) {
            throw new IllegalArgumentException("Unknown arguments: " + Join.join(", ", new Join.NameCallback<String>() {
                @Override
                public String getName(final String object) {
                    if (object.length() > 1) {
                        return "--" + object;
                    }
                    return "-" + object;
                }
            }, args.options.keySet()));
        }

        return converted;
    }

    private List<Object> convert(Arguments args, Needed needed, List<Param> parameters1) {
        /**
         * Here we iterate over the method's parameters and convert strings into their equivalent Option or Arg value.
         *
         * The result is a List of objects that matches perfectly the available of arguments required to pass into the
         * java.lang.reflect.Method.invoke() method.
         *
         * Thus, iteration order is very significant in this loop.
         */
        final List<Object> converted = new ArrayList<Object>();

        for (final Param parameter : parameters1) {
            final Option option = parameter.getAnnotation(Option.class);

            if (option != null) {
                
                final String optionValue = option.value()[0];
                final String value = args.options.remove(optionValue);

                if (parameter.isListable()) {
                    converted.add(convert(parameter, OptionParam.getSeparatedValues(value), optionValue));
                } else { 
                    converted.add(Converter.convert(value, parameter.getType(), optionValue));
                }
            } else if (parameter instanceof ComplexParam) {

                final ComplexParam complexParam = (ComplexParam) parameter;

                converted.add(complexParam.convert(args, needed));

            } else if (args.list.size() > 0) {
                needed.count--;

                if (parameter.isListable()) {
                    final List<String> glob = new ArrayList<String>(args.list.size());

                    for (int i = args.list.size(); i > needed.count; i--) {
                        glob.add(args.list.remove(0));
                    }

                    converted.add(convert(parameter, glob, null));
                } else {

                    final String value = args.list.remove(0);
                    converted.add(Converter.convert(value, parameter.getType(),
                            parameter.getDisplayType().replace("[]", "...")));
                }

            } else {

                throw new IllegalArgumentException("Missing argument: "
                        + parameter.getDisplayType().replace("[]", "...") + "");
            }
        }
        return converted;
    }

    private static Object convert(final Param parameter, final List<String> values, final String name) {
        final Class<?> type = parameter.getListableType();

        if (parameter.isAnnotationPresent(Required.class) && values.size() == 0) {
            if (parameter instanceof OptionParam) {
                final OptionParam optionParam = (OptionParam) parameter;
                throw new IllegalArgumentException(String.format("--%s must be specified at least once",
                        optionParam.getName()));
            } else {
                throw new IllegalArgumentException(String.format("Argument for %s requires at least one value",
                        parameter.getDisplayType().replace("[]", "...")));
            }
        }

        final String description = name == null ? "[" + type.getSimpleName() + "]" : name;

        if (Enum.class.isAssignableFrom(type) && isBoolean(values)) {
            final boolean all = "true".equals(values.get(0));

            values.clear();

            if (all) {
                final Class<? extends Enum> elementType = (Class<? extends Enum>) type;
                final EnumSet<? extends Enum> enums = EnumSet.allOf(elementType);
                for (final Enum e : enums) {
                    values.add(e.name());
                }
            }
        }

        if (parameter.getType().isArray()) {

            final Object array = Array.newInstance(type, values.size());
            int i = 0;
            for (final String string : values) {
                Array.set(array, i++, Converter.convert(string, type, description));
            }

            return array;

        } else {

            final Collection<Object> collection = instantiate((Class<? extends Collection>) parameter.getType());

            for (final String string : values) {

                collection.add(Converter.convert(string, type, description));

            }

            return collection;
        }
    }

    private static boolean isBoolean(final List<String> values) {
        if (values.size() != 1) {
            return false;
        }
        if ("true".equals(values.get(0))) {
            return true;
        }
        if ("false".equals(values.get(0))) {
            return true;
        }
        return false;
    }

    public static Collection<Object> instantiate(final Class<? extends Collection> aClass) {
        if (aClass.isInterface()) {
            // Sub iterfaces listed first

            // Sets
            if (NavigableSet.class.isAssignableFrom(aClass)) {
                return new TreeSet<Object>();
            }
            if (SortedSet.class.isAssignableFrom(aClass)) {
                return new TreeSet<Object>();
            }
            if (Set.class.isAssignableFrom(aClass)) {
                return new LinkedHashSet<Object>();
            }

            // Queues
            if (Deque.class.isAssignableFrom(aClass)) {
                return new LinkedList<Object>();
            }
            if (Queue.class.isAssignableFrom(aClass)) {
                return new LinkedList<Object>();
            }

            // Lists
            if (List.class.isAssignableFrom(aClass)) {
                return new ArrayList<Object>();
            }

            // Collection
            if (Collection.class.isAssignableFrom(aClass)) {
                return new LinkedList<Object>();
            }

            // Iterable
            if (Iterable.class.isAssignableFrom(aClass)) {
                return new LinkedList<Object>();
            }

            throw new IllegalStateException("Unsupported Collection type: " + aClass.getName());
        }

        if (Modifier.isAbstract(aClass.getModifiers())) {

            throw new IllegalStateException("Unsupported Collection type: " + aClass.getName() + " - Type is Abstract");
        }

        try {

            final Constructor<? extends Collection> constructor = aClass.getConstructor();

            return constructor.newInstance();

        } catch (final NoSuchMethodException e) {

            throw new IllegalStateException("Unsupported Collection type: " + aClass.getName() + " - No default "
                    + "constructor");

        } catch (final Exception e) {

            throw new IllegalStateException("Cannot construct java.util.Collection type: " + aClass.getName(), e);
        }
    }

    public Map<String, String> getDefaults() {
        final Map<String, String> options = new HashMap<String, String>();

        for (final OptionParam parameter : spec.options.values()) {
            options.put(parameter.getName(), parameter.getDefaultValue());
        }

        return options;
    }

    private class Arguments {
        private final List<String> list = new ArrayList<String>();
        private final Map<String, String> options = new HashMap<String, String>();

        private Arguments(final String[] rawArgs) {

            final Map<String, String> defaults = getDefaults();
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

        private void getCommand(final String prefix,
                           final String arg,
                           final Map<String, String> defaults,
                           final Map<String, String> supplied,
                           final List<String> invalid,
                           final Set<String> repeated)
        {
            String name;
            String value;

            if (arg.indexOf("=") > 0) {
                name = arg.substring(arg.indexOf(prefix) + prefix.length(), arg.indexOf("="));
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

            if (!defaults.containsKey(name) && spec.aliases.containsKey(name)) {
                // check the options to find see if name is an alias for an option
                // if it is, get the actual optionparam name
                name = spec.aliases.get(name).getName();
            }

            if (defaults.containsKey(name)) {
                final boolean isList = defaults.get(name) != null
                        && defaults.get(name).startsWith(OptionParam.LIST_TYPE);
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
                invalid.add(name);
            }
        }

        private void interpret(final Map<String, String> map) {
            for (final Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                final String value = Substitution.format(target, method, entry.getValue(), defaultsFinder);
                map.put(entry.getKey(), value);
            }
        }

        private void checkInvalid(final List<String> invalid) {
            if (invalid.size() > 0) {
                throw new IllegalArgumentException("Unknown options: " + Join.join(", ", new Join.NameCallback<String>() {
                    @Override
                    public String getName(final String object) {
                        if (object.length() > 1) {
                            return "--" + object;
                        } else {
                            return "-" + object;
                        }
                    }
                }, invalid));
            }
        }

        private void checkRequired(final Map<String, String> supplied) {
            final List<String> required = new ArrayList<String>();
            for (final Param parameter : spec.options.values()) {
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

            if (required.size() > 0) {
                throw new IllegalArgumentException("Required: " + Join.join(", ", new Join.NameCallback<String>() {
                    @Override
                    public String getName(final String object) {
                        if (object.length() > 1) {
                            return "--" + object;
                        } else {
                            return "-" + object;
                        }
                    }
                }, required));
            }
        }

        private void checkRepeated(final Set<String> repeated) {
            if (repeated.size() > 0) {
                throw new IllegalArgumentException("Cannot be specified more than once: " + Join.join(", ", repeated));
            }
        }
    }

    @Override
    public String toString() {
        return "Command{" + "name='" + name + '\'' + '}';
    }

    @Override
    public Collection<String> complete(final String buffer, final int cursorPosition) {
        final List<String> result = new ArrayList<String>(); 
        
        
        final String commandLine = buffer.substring(0, cursorPosition);
        final String[] args = CommandLine.translateCommandline(commandLine);

        if (args != null && args.length > 0) {
            final String lastArg = args[args.length - 1];
            if (lastArg.startsWith("--")) {
                result.addAll(findMatchingOptions(lastArg.substring(2), false));
            } else if (lastArg.startsWith("-")) {
                result.addAll(findMatchingOptions(lastArg.substring(1), true));
            }
        }
        
        return result;
    }

    private Collection<String> findMatchingOptions(String prefix, boolean isIncludeAliasChar) {
        final List<String> result = new ArrayList<String>();
        
        for (Param param : parameters) {
            if (param instanceof OptionParam) {
                final OptionParam optionParam = (OptionParam) param;
                
                if (optionParam.getName().startsWith(prefix)) {
                    result.add("--" + optionParam.getName());
                }
            }
        }

        for (String alias : spec.aliases.keySet()) {
            if (alias.startsWith(prefix)) {
                if (alias.length() > 1) {
                    result.add("--" + alias);
                }

                if (isIncludeAliasChar && alias.length() == 1) {
                    result.add("-" + alias);
                }
            }
        }
        return result;
    }
}
