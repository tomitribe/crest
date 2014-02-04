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
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.OptionBean;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.util.Converter;
import org.tomitribe.crest.val.BeanValidation;
import org.tomitribe.util.Join;
import org.tomitribe.util.reflect.Parameter;
import org.tomitribe.util.reflect.Reflection;

import java.io.PrintStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
    private final Map<String, OptionParam> optionParameters;
    private final List<Param> argumentParameters;
    private final List<Param> parameters;
    private final DefaultsContext defaultsFinder;

    public CmdMethod(Method method, DefaultsContext defaultsFinder) {
        this(method, new SimpleBean(null), defaultsFinder);
    }

    public CmdMethod(Method method, Target target, DefaultsContext defaultsFinder) {
        this.target = target;
        this.method = method;
        this.defaultsFinder = defaultsFinder;
        this.name = name(method);

        final Map<String, OptionParam> options = new TreeMap<String, OptionParam>();
        final List<Param> arguments = new ArrayList<Param>();
        final List<Param> parameters = new ArrayList<Param>();
        for (final Parameter parameter : Reflection.params(method)) {

            addParameter(options, arguments, parameters, parameter);
        }

        this.optionParameters = Collections.unmodifiableMap(options);
        this.argumentParameters = Collections.unmodifiableList(arguments);
        this.parameters = Collections.unmodifiableList(parameters);

        validate();
    }

    private void addParameter(final Map<String, OptionParam> options,
                              final List<Param> arguments,
                              final List<Param> parameters,
                              final Parameter parameter) {
        if (parameter.isAnnotationPresent(Option.class)) {

            newOptionParam(new OptionParam(parameter), options, parameters);
        } else if (parameter.getType().isAnnotationPresent(OptionBean.class)) {

            final Object instance;
            try {
                instance = parameter.getType().newInstance();
            } catch (final InstantiationException e) {
                throw toRuntimeException(e);
            } catch (final IllegalAccessException e) {
                throw toRuntimeException(e);
            }

            for (final Field field : parameter.getType().getDeclaredFields()) {
                final Parameter fieldParameter = new Parameter(field.getDeclaredAnnotations(), field.getType(), field.getGenericType());
                if (fieldParameter.isAnnotationPresent(Option.class)) {
                    newOptionParam(new BeanFieldOptionParam(fieldParameter, field, instance), options, parameters);
                }
            }
        } else {

            final Param e = new Param(parameter);
            arguments.add(e);
            parameters.add(e);
        }
    }

    private void newOptionParam(final OptionParam optionParam, final Map<String, OptionParam> options,
                                final List<Param> parameters) {
        final OptionParam existing = options.put(optionParam.getName(), optionParam);

        if (existing != null) throw new IllegalArgumentException("Duplicate option: " + optionParam.getName());

        parameters.add(optionParam);
    }

    public CmdMethod(final Method method, final Target target) {
        this(method, target, new SystemPropertiesDefaultsContext());

    }

    public Method getMethod() {
        return method;
    }

    public List<Param> getArgumentParameters() {
        return argumentParameters;
    }

    private void validate() {
        for (Param param : argumentParameters) {
            if (param.isAnnotationPresent(Default.class)) {
                throw new IllegalArgumentException("@Default only usable with @Option parameters.");
            }
            if (!param.isListable() && param.isAnnotationPresent(Required.class)) {
                throw new IllegalArgumentException("@Required only usable with @Option parameters and lists.");
            }
        }
    }

    private static String name(Method method) {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) return method.getName();
        return value(command.value(), method.getName());
    }

    /**
     * Returns a single line description of the command
     * @return
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

        for (Param parameter : argumentParameters) {
            args.add(parameter.getDisplayType().replace("[]", "..."));
        }

        return String.format("%s %s %s", name, args.size() == method.getParameterTypes().length ? "" : "[options]", Join.join(" ", args)).trim();
    }

    private String usage() {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) return null;
        if ("".equals(command.usage())) return null;
        return command.usage();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object exec(String... rawArgs) {
        final List<Object> list;
        try {
            list = parse(rawArgs);
        } catch (Exception e) {
            reportWithHelp(e);
            throw toRuntimeException(e);
        }

        return exec(list);
    }

    public Object exec(List<Object> list) {
        final Object[] args;
        try {
            args = list.toArray();
            BeanValidation.validateParameters(method.getDeclaringClass(), method, args);
        } catch (Exception e) {
            reportWithHelp(e);
            throw toRuntimeException(e);
        }

        try {
            return target.invoke(method, args);
        } catch (InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                reportWithHelp(e);
            }
            throw new CommandFailedException(cause);
        } catch (Throwable e) {
            throw toRuntimeException(e);
        }
    }

    private void reportWithHelp(Exception e) {
        final PrintStream err = Environment.local.get().getError();

        if (BeanValidation.isActive()) {
            for (final String message : BeanValidation.messages(e)) {
                err.println(message);
            }
        } else {
            err.println(e.getMessage());
        }
        help(err);
    }

    public static RuntimeException toRuntimeException(Throwable e) {
        if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new IllegalArgumentException(e);
    }

    public Map<String, OptionParam> getOptionParameters() {
        return optionParameters;
    }

    @Override
    public void help(PrintStream out) {
        out.println();
        out.print("Usage: ");
        out.println(getUsage());
        out.println();

        Help.optionHelp(method.getDeclaringClass(), getName(), optionParameters.values(), out);
    }

    public List<Object> parse(String... rawArgs) {
        return convert(new Arguments(rawArgs));
    }

    private <T> List<Object> convert(Arguments args) {

        final Map<String, String> options = args.options;
        final List<String> available = args.list;

        final List<Object> converted = new ArrayList<Object>();
        int needed = argumentParameters.size();

        /**
         * Here we iterate over the method's parameters and convert
         * strings into their equivalent Option or Arg value.
         *
         * The result is a List of objects that matches perfectly
         * the available of arguments required to pass into the
         * java.lang.reflect.Method.invoke() method.
         *
         * Thus, iteration order is very significant in this loop.
         */
        Object lastBoundValue = null;
        for (final Param parameter : parameters) {
            if (parameter.getAnnotation(OptionBean.class) != null) {
                final Option option = parameter.getAnnotation(Option.class);
                parameter.bind(value(parameter, option, options.remove(option.value())));
                if (lastBoundValue == null || parameter.getBoundValue() != lastBoundValue) {
                    lastBoundValue = parameter.getBoundValue();
                    converted.add(lastBoundValue);
                }
                continue;
            }

            final Option option = parameter.getAnnotation(Option.class);

            if (option != null) {
                final String value = options.remove(option.value());
                parameter.bind(value(parameter, option, value));
                converted.add(parameter.getBoundValue());
            } else if (available.size() > 0) {
                needed--;

                if (parameter.isListable()) {
                    final List<String> glob = new ArrayList<String>(available.size());

                    for (int i = available.size(); i > needed; i--) {
                        glob.add(available.remove(0));
                    }

                    parameter.bind(convert(parameter, glob, null));
                    converted.add(parameter.getBoundValue());
                } else {

                    final String value = available.remove(0);
                    parameter.bind(Converter.convert(value, parameter.getType(), parameter.getDisplayType().replace("[]", "...")));
                    converted.add(parameter.getBoundValue());
                }

            } else {

                throw new IllegalArgumentException("Missing argument: " + parameter.getDisplayType().replace("[]", "...") + "");
            }
            lastBoundValue = null; // this value is only relevant for @OptionBeans
        }

        if (available.size() > 0) {
            throw new IllegalArgumentException("Excess arguments: " + Join.join(", ", available));
        }

        if (options.size() > 0) {
            throw new IllegalArgumentException("Unknown arguments: " + Join.join(", ", new Join.NameCallback<Object>() {
                @Override
                public String getName(Object object) {
                    return "--" + object;
                }
            }, options.keySet()));
        }

        return converted;
    }

    private Object value(final Param parameter, final Option option, final String value) {
        if (parameter.isListable()) {
            return convert(parameter, OptionParam.getSeparatedValues(value), option.value());
        }
        return Converter.convert(value, parameter.getType(), option.value());
    }

    private static Object convert(final Param parameter, final List<String> values, final String name) {
        final Class<?> type = parameter.getListableType();

        if (parameter.isAnnotationPresent(Required.class) && values.size() == 0) {
            if (parameter instanceof OptionParam) {
                final OptionParam optionParam = (OptionParam) parameter;
                throw new IllegalArgumentException(String.format("--%s must be specified at least once", optionParam.getName()));
            } else {
                throw new IllegalArgumentException(String.format("Argument for %s requires at least one value", parameter.getDisplayType().replace("[]", "...")));
            }
        }

        final String description = name == null ? "[" + type.getSimpleName() + "]" : name;

        if (Enum.class.isAssignableFrom(type) && isBoolean(values)) {
            final boolean all = "true".equals(values.get(0));

            values.clear();

            if (all) {
                final Class<? extends Enum> elementType = (Class<? extends Enum>) type;
                final EnumSet<? extends Enum> enums = EnumSet.allOf(elementType);
                for (Enum e : enums) {
                    values.add(e.name());
                }
            }
        }

        if (parameter.getType().isArray()) {

            final Object array = Array.newInstance(type, values.size());
            int i = 0;
            for (String string : values) {
                Array.set(array, i++, Converter.convert(string, type, description));
            }

            return array;

        } else {

            final Collection<Object> collection = instantiate((Class<? extends Collection>) parameter.getType());

            for (String string : values) {

                collection.add(Converter.convert(string, type, description));

            }

            return collection;
        }
    }

    private static boolean isBoolean(List<String> values) {
        if (values.size() != 1) return false;
        if ("true".equals(values.get(0))) return true;
        if ("false".equals(values.get(0))) return true;
        return false;
    }

    public static Collection<Object> instantiate(Class<? extends Collection> aClass) {
        if (aClass.isInterface()) {
            // Sub iterfaces listed first

            // Sets
            if (NavigableSet.class.isAssignableFrom(aClass)) return new TreeSet<Object>();
            if (SortedSet.class.isAssignableFrom(aClass)) return new TreeSet<Object>();
            if (Set.class.isAssignableFrom(aClass)) return new LinkedHashSet<Object>();

            // Queues
            if (Deque.class.isAssignableFrom(aClass)) return new LinkedList<Object>();
            if (Queue.class.isAssignableFrom(aClass)) return new LinkedList<Object>();

            // Lists
            if (List.class.isAssignableFrom(aClass)) return new ArrayList<Object>();

            // Collection
            if (Collection.class.isAssignableFrom(aClass)) return new LinkedList<Object>();

            // Iterable
            if (Iterable.class.isAssignableFrom(aClass)) return new LinkedList<Object>();

            throw new IllegalStateException("Unsupported Collection type: " + aClass.getName());
        }

        if (Modifier.isAbstract(aClass.getModifiers())) {

            throw new IllegalStateException("Unsupported Collection type: " + aClass.getName() + " - Type is Abstract");
        }

        try {

            final Constructor<? extends Collection> constructor = aClass.getConstructor();

            return constructor.newInstance();

        } catch (NoSuchMethodException e) {

            throw new IllegalStateException("Unsupported Collection type: " + aClass.getName() + " - No default constructor");

        } catch (Exception e) {

            throw new IllegalStateException("Cannot construct java.util.Collection type: " + aClass.getName(), e);
        }
    }

    public Map<String, String> getDefaults() {
        final Map<String, String> options = new HashMap<String, String>();

        for (OptionParam parameter : optionParameters.values()) {
            options.put(parameter.getName(), parameter.getDefaultValue());
        }

        return options;
    }

    public static String value(String value, String defaultValue) {
        return value == null || value.length() == 0 ? defaultValue : value;
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
            for (String arg : rawArgs) {
                if (arg.startsWith("--")) {

                    final String name;
                    String value;

                    if (arg.indexOf("=") > 0) {
                        name = arg.substring(arg.indexOf("--") + 2, arg.indexOf("="));
                        value = arg.substring(arg.indexOf("=") + 1);
                    } else {
                        if (arg.startsWith("--no-")) {
                            name = arg.substring(5);
                            value = "false";
                        } else {
                            name = arg.substring(2);
                            value = "true";
                        }
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
                        invalid.add(name);
                    }
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

        private void interpret(final Map<String, String> map) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getValue() == null) continue;
                final String value = Substitution.format(target, method, entry.getValue(), defaultsFinder);
                map.put(entry.getKey(), value);
            }
        }

        private void checkInvalid(List<String> invalid) {
            if (invalid.size() > 0) {
                throw new IllegalArgumentException("Unknown options: " + Join.join(", ", new Join.NameCallback() {
                    @Override
                    public String getName(Object object) {
                        return "--" + object;
                    }
                }, invalid));
            }
        }

        private void checkRequired(Map<String, String> supplied) {
            final List<String> required = new ArrayList<String>();
            for (Param parameter : optionParameters.values()) {
                if (!parameter.isAnnotationPresent(Required.class)) continue;

                final Option option = parameter.getAnnotation(Option.class);

                if (!supplied.containsKey(option.value())) {
                    required.add(option.value());
                }
            }

            if (required.size() > 0) {
                throw new IllegalArgumentException("Required: " + Join.join(", ", new Join.NameCallback() {
                    @Override
                    public String getName(Object object) {
                        return "--" + object;
                    }
                }, required));
            }
        }

        private void checkRepeated(Set<String> repeated) {
            if (repeated.size() > 0) {
                throw new IllegalArgumentException("Cannot be specified more than once: " + Join.join(", ", repeated));
            }
        }
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                '}';
    }
}
