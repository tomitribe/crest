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
import org.tomitribe.crest.api.CrestAnnotation;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.cmds.builder.ParameterBuilder;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.cmds.utils.CommandLine;
import org.tomitribe.crest.cmds.validator.IterableMessagesException;
import org.tomitribe.crest.cmds.validator.ParameterValidator;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.util.Join;
import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.reflect.Parameter;
import org.tomitribe.util.reflect.Reflection;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
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

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableMap;
import static org.tomitribe.crest.exception.Exceptions.toRuntimeException;

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
    private final Map<Class<?>, ParameterBuilder> injectors;
    private final List<ParameterValidator> validators;

    public class Spec {
        private final Map<String, OptionParam> options = new TreeMap<String, OptionParam>();
        private final Map<String, OptionParam> aliases = new TreeMap<String, OptionParam>();
        private final List<Param> arguments = new LinkedList<Param>();

        public Map<String, OptionParam> getOptions() {
            return unmodifiableMap(options);
        }

        public Map<String, OptionParam> getAliases() {
            return unmodifiableMap(aliases);
        }
    }

    public CmdMethod(final Method method, final Target target, final DefaultsContext defaultsFinder,
                     final Map<Class<?>, ParameterBuilder> injectors, final List<ParameterValidator> validators) {
        this.target = target;
        this.method = method;
        this.defaultsFinder = defaultsFinder;
        this.name = Commands.name(method);
        this.injectors = injectors;
        this.validators = validators;
        this.parameters = Collections.unmodifiableList(buildParams(Reflection.params(method)));

        validate();
    }

    public List<Param> buildParams(final Iterable<Parameter> params) {
        final List<Param> parameters = new ArrayList<Param>();
        for (final Parameter parameter : params) {
            boolean matched = false;

            for (final Annotation annotation : parameter.getAnnotations()) {
                final ParameterBuilder injector = injectors.get(annotation.annotationType());
                if (injector != null) {
                    matched = addParameter(parameters, parameter, injector);
                    break; // injectors are sorted so first matching one is the right one
                }
            }
            if (matched) {
                continue;
            }

            for (final Annotation annotation : parameter.getType().getAnnotations()) {
                final ParameterBuilder injector = injectors.get(annotation.annotationType());
                if (injector != null) {
                    matched = addParameter(parameters, parameter, injector);
                    break;
                }
            }
            if (matched) {
                continue;
            }

            final Param e = new Param(parameter);
            spec.arguments.add(e);
            parameters.add(e);
        }
        return parameters;
    }

    private boolean addParameter(final List<Param> parameters, final Parameter parameter, final ParameterBuilder injector) {
        final ParameterBuilder.ParamMeta paramMeta = injector.buildParameter(this, parameter);
        if (paramMeta == null) {
            return false;
        }

        if (OptionParam.class.isInstance(paramMeta.getParam())) {
            final OptionParam optionParam = OptionParam.class.cast(paramMeta.getParam());
            final OptionParam existing = spec.options.put(paramMeta.getMainOption(), optionParam);
            if (existing != null) {
                throw new IllegalArgumentException("Duplicate option: " + paramMeta.getMainOption());
            }
            for (final String alias : paramMeta.getAliases()) {
                spec.aliases.put(alias, optionParam);
            }
        }

        parameters.add(paramMeta.getParam());
        return true;
    }

    public Method getMethod() {
        return method;
    }

    public Target getTarget() {
        return target;
    }

    public DefaultsContext getDefaultsFinder() {
        return defaultsFinder;
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
        String commandName = name;
        
        Class<?> declaringClass = method.getDeclaringClass();
        Map<String, Cmd> commands = Commands.get(declaringClass, injectors, validators);
        if (commands.size() == 1 && commands.values().iterator().next() instanceof CmdGroup) {
            final CmdGroup cmdGroup = (CmdGroup) commands.values().iterator().next();
            commandName = cmdGroup.getName() + " " + name;
        }
        
        
        final String usage = usage();

        if (usage != null) {
            if (!usage.startsWith(commandName)) {
                return commandName + " " + usage;
            } else {
                return usage;
            }
        }

        final List<Object> args = new ArrayList<Object>();

        for (final Param parameter : spec.arguments) {
            boolean skip = parameter.getType() == Environment.class;
            for (final Annotation a : parameter.getAnnotations()) {
                final CrestAnnotation crestAnnotation = a.annotationType().getAnnotation(CrestAnnotation.class);
                if (crestAnnotation != null) {
                    skip = crestAnnotation.skipUsage();
                    break;
                }
            }
            if (skip) {
                continue;
            }
            args.add(parameter.getDisplayType().replace("[]", "..."));
        }

        return String.format("%s %s %s", commandName, args.size() == method.getParameterTypes().length ? "" : "[options]",
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
            if (!validators.isEmpty()) {
                if (validators.size() == 1) {
                    validators.get(0).validate(method.getDeclaringClass(), method, args);
                } else {
                    for (final ParameterValidator validator : validators) {
                        validator.validate(method.getDeclaringClass(), method, args);
                    }
                }
            }
        } catch (final Exception e) {
            reportWithHelp(e);
            throw toRuntimeException(IterableMessagesException.class.isInstance(e) ? IterableMessagesException.class.cast(e).getCause() : e);
        }

        try {
            return target.invoke(method, args);
        } catch (final InvocationTargetException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IllegalArgumentException) {
                reportWithHelp(e);
            }
            throw new CommandFailedException(cause, getName());
        } catch (final Throwable e) {
            throw toRuntimeException(e);
        }
    }

    private void reportWithHelp(final Exception e) {
        final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();

        if (IterableMessagesException.class.isInstance(e)) {
            for (final String message : IterableMessagesException.class.cast(e).getMessages()) {
                err.println(message);
            }
        } else {
            err.println(e.getMessage());
        }
        help(err);
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
        return convert(new Arguments(rawArgs, this));
    }

    public Spec getSpec() {
        return spec;
    }

    public List<ParameterValidator> getValidators() {
        return validators;
    }

    private List<Object> convert(final Arguments args) {
        final Arguments.Needed needed = new Arguments.Needed(spec.arguments.size());

        final List<Object> converted = convert(args, needed, parameters);

        if (!args.getList().isEmpty()) {
            throw new IllegalArgumentException("Excess arguments: " + Join.join(", ", args.getList()));
        }
        args.checkOptions();

        return converted;
    }

    public List<Object> convert(Arguments args, Arguments.Needed needed, List<Param> params) { // TODO: use injectors/builders
        /**
         * Here we iterate over the method's parameters and convert strings into their equivalent Option or Arg value.
         *
         * The result is a List of objects that matches perfectly the available of arguments required to pass into the
         * java.lang.reflect.Method.invoke() method.
         *
         * Thus, iteration order is very significant in this loop.
         */
        final List<Object> converted = new ArrayList<Object>();

        for (final Param parameter : params) {
            boolean matched = false;

            for (final Annotation annotation : parameter.getAnnotations()) {
                final ParameterBuilder injector = injectors.get(annotation.annotationType());
                if (injector != null) {
                    converted.add(injector.create(this, parameter, args, needed));
                    matched = true;
                    break; // injectors are sorted so first matching one is the right one
                }
            }
            if (matched) {
                continue;
            }

            for (final Annotation annotation : parameter.getType().getAnnotations()) {
                final ParameterBuilder injector = injectors.get(annotation.annotationType());
                if (injector != null) {
                    converted.add(injector.create(this, parameter, args, needed));
                    matched = true;
                    break;
                }
            }
            if (matched) {
                continue;
            }

            if (!args.getList().isEmpty()) {
                needed.decr();

                if (parameter.isListable()) {
                    final List<String> glob = new ArrayList<String>(args.getList().size());

                    for (int i = args.getList().size(); i > needed.getCount(); i--) {
                        glob.add(args.getList().remove(0));
                    }

                    converted.add(convert(parameter, glob, null));
                } else {

                    final String value = args.getList().remove(0);
                    converted.add(Converter.convert(value, parameter.getType(),
                        parameter.getDisplayType().replace("[]", "...")));
                }

            } else if (Environment.class == parameter.getType()) {
                converted.add(Environment.ENVIRONMENT_THREAD_LOCAL.get());

            } else {

                throw new IllegalArgumentException("Missing argument: "
                        + parameter.getDisplayType().replace("[]", "...") + "");
            }
        }
        return converted;
    }

    public static Object convert(final Param parameter, final List<String> values, final String name) {
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
        return values.size() == 1 && asList("true", "false").contains(values.get(0));
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

    private Collection<String> findMatcingParametersOptions(String prefix, boolean isIncludeAliasChar) {
        final List<String> result = new ArrayList<String>();
        for (Param param : parameters) {
            if (param instanceof OptionParam) {
                final OptionParam optionParam = (OptionParam) param;

                final String optionParamName = optionParam.getName();
                if (optionParamName.startsWith(prefix)) {
                    if (optionParamName.startsWith("-")) {
                        result.add(optionParamName);
                        continue;
                    }
                    if (optionParamName.length() > 1) {
                        result.add("--" + optionParamName);
                        continue;
                    }
                    if (isIncludeAliasChar) {
                        result.add("-" + optionParamName);
                    }
                }
            }
        }
        return result;
    }

    private Collection<String> findMatchingAliasOptions(String prefix, boolean isIncludeAliasChar) {
        final List<String> result = new ArrayList<String>();
        for (String alias : spec.aliases.keySet()) {
            if (alias.startsWith(prefix)) {
                if (alias.startsWith("-")) {
                    result.add(alias);
                } else if (alias.length() > 1) {
                    result.add("--" + alias);
                }
                if (isIncludeAliasChar && alias.length() == 1) {
                    result.add("-" + alias);
                }
            }
        }
        return result;
    }

    private Collection<String> findMatchingOptions(String prefix, boolean isIncludeAliasChar) {
        List<String> results = new ArrayList<String>();
        results.addAll(findMatcingParametersOptions(prefix, isIncludeAliasChar));
        results.addAll(findMatchingAliasOptions(prefix, isIncludeAliasChar));
        return results;
    }
}
