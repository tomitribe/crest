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
import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Err;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.NotAService;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.api.Required;
import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.cmds.processors.Item;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Substitution;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.cmds.utils.CommandLine;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.help.CommandJavadoc;
import org.tomitribe.crest.help.Document;
import org.tomitribe.crest.help.DocumentFormatter;
import org.tomitribe.crest.help.DocumentParser;
import org.tomitribe.crest.interceptor.internal.InternalInterceptor;
import org.tomitribe.crest.interceptor.internal.InternalInterceptorInvocationContext;
import org.tomitribe.crest.javadoc.Javadoc;
import org.tomitribe.crest.javadoc.JavadocParser;
import org.tomitribe.crest.val.BeanValidation;
import org.tomitribe.util.IO;
import org.tomitribe.util.Join;
import org.tomitribe.util.editor.Converter;
import org.tomitribe.util.reflect.Parameter;
import org.tomitribe.util.reflect.Reflection;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
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

import static java.util.Collections.unmodifiableList;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.BEAN_OPTION;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.INTERNAL;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.OPTION;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.SERVICE;
import static org.tomitribe.crest.help.DocumentParser.parseOptionDescription;

/**
 * @version $Revision$ $Date$
 */
public class CmdMethod implements Cmd {
    private static final String[] NO_PREFIX = {""};
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

    private final Target target;
    private final Method method;
    private final String name;
    private final List<Param> parameters;
    private final Class<?>[] interceptors;
    private final DefaultsContext defaultsFinder;
    private final Spec spec = new Spec();
    private volatile List<ParameterMetadata> parameterMetadatas;

    public class Spec {
        private final Map<String, OptionParam> options = new TreeMap<>();
        private final Map<String, OptionParam> aliases = new TreeMap<>();
        private final List<Param> arguments = new LinkedList<>();

        public Map<String, OptionParam> getOptions() {
            return Collections.unmodifiableMap(options);
        }

        public Map<String, OptionParam> getAliases() {
            return Collections.unmodifiableMap(aliases);
        }

        public List<Param> getArguments() {
            return Collections.unmodifiableList(arguments);
        }
    }

    public CmdMethod(final Method method, final DefaultsContext defaultsFinder) {
        this(method, new SimpleBean(null), defaultsFinder);
    }

    public CmdMethod(final Method method, final Target target, final DefaultsContext defaultsFinder) {
        this.target = target;
        this.method = method;
        this.defaultsFinder = defaultsFinder;
        this.name = Commands.name(method);

        final List<Param> parameters = buildParams(null, NO_PREFIX, null, Reflection.params(method));

        this.parameters = Collections.unmodifiableList(parameters);

        final Command cmdAnnotation = method.getAnnotation(Command.class);
        this.interceptors = cmdAnnotation == null ? null : cmdAnnotation.interceptedBy();

        validate();
    }

    private List<Param> buildParams(final String globalDescription, final String[] inPrefixes,
                                    final Defaults.DefaultMapping[] defaultsMapping, final Iterable<Parameter> params) {
        final String[] prefixes = inPrefixes == null ? NO_PREFIX : inPrefixes;
        final List<Param> parameters = new ArrayList<>();
        for (final Parameter parameter : params) {

            if (parameter.isAnnotationPresent(Option.class)) {

                final Option option = parameter.getAnnotation(Option.class);

                final Options options = parameter.getType().getAnnotation(Options.class);
                if (options != null) {

                    final Defaults defaultMappings = parameter.getAnnotation(Defaults.class);
                    final Defaults.DefaultMapping[] directMapping = parameter.getDeclaredAnnotationsByType(Defaults.DefaultMapping.class);
                    final ComplexParam complexParam = new ComplexParam(
                            option.value(), option.description(),
                            directMapping != null ? directMapping : defaultMappings.value(),
                            parameter, options.nillable());

                    parameters.add(complexParam);

                } else {
                    if (parameter.isAnnotationPresent(Defaults.class)) {
                        throw new IllegalArgumentException("Simple option doesnt support @Defaults, use @Default please");
                    }

                    final String shortName = option.value()[0];
                    final String mainOption = prefixes[0] + shortName;
                    String def = null;
                    String description = option.description();
                    if (defaultsMapping != null) {
                        for (final Defaults.DefaultMapping mapping : defaultsMapping) {
                            if (mapping.name().equals(shortName)) {
                                def = mapping.value();
                                if (!mapping.description().isEmpty()) {
                                    def = mapping.description();
                                }
                                break;
                            }
                        }
                    }
                    final OptionParam optionParam = new OptionParam(parameter, mainOption, def, (globalDescription != null ? globalDescription : "") + description);

                    final OptionParam existing = spec.options.put(mainOption, optionParam);
                    if (existing != null) {
                        throw new IllegalArgumentException("Duplicate option: " + mainOption);
                    }

                    for (int i = 1; i < prefixes.length; i++) {
                        final String key = prefixes[i] + optionParam.getName();
                        final OptionParam existingAlias = spec.aliases.put(key, optionParam);

                        if (existingAlias != null) {
                            throw new IllegalArgumentException("Duplicate alias: " + key);
                        }
                    }

                    for (int i = 1; i < option.value().length; i++) {
                        final String alias = option.value()[i];
                        for (final String prefix : prefixes) {
                            final String fullAlias = prefix + alias;
                            final OptionParam existingAlias = spec.aliases.put(fullAlias, optionParam);

                            if (existingAlias != null) {
                                throw new IllegalArgumentException("Duplicate alias: " + fullAlias);
                            }
                        }
                    }

                    parameters.add(optionParam);
                }
            } else if (parameter.getType().isAnnotationPresent(Options.class)) {

                final ComplexParam complexParam = new ComplexParam(null, null, null, parameter, parameter.getType().getAnnotation(Options.class).nillable());

                parameters.add(complexParam);

            } else {

                final Param e = new Param(parameter);
                spec.arguments.add(e);
                parameters.add(e);
            }
        }

        parameterMetadatas = buildApiParameterViews(parameters);

        return parameters;
    }

    private class ComplexParam extends Param {

        private final List<Param> parameters;
        private final Constructor<?> constructor;
        private final boolean nullable;

        private ComplexParam(final String[] prefixes, final String globalDescription,
                             final Defaults.DefaultMapping[] defaults, final Parameter parent, final boolean nullable) {
            super(parent);

            this.constructor = parent.getType().getConstructors()[0];
            this.parameters = Collections.unmodifiableList(buildParams(globalDescription, prefixes, defaults, Reflection.params(constructor)));
            this.nullable = nullable;
        }

        public Value convert(final Arguments arguments, final Needed needed) {
            final List<Value> converted = CmdMethod.this.convert(arguments, needed, parameters);
            if (nullable) {
                boolean allNull = true;
                for (final Value val : converted) {
                    if (val.isProvided()) {
                        allNull = false;
                        break;
                    }
                }
                if (allNull) {
                    return new Value(null, false);
                }
            }

            try {
                final Object[] args = toArgs(converted).toArray(new Object[converted.size()]);
                BeanValidation.validateParameters(constructor, args);
                return new Value(constructor.newInstance(args), true);

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
        String commandName = name;

        final Class<?> declaringClass = method.getDeclaringClass();
        final Map<String, Cmd> commands = Commands.get(declaringClass);
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

        final List<Object> args = new ArrayList<>();

        for (final Param parameter : spec.arguments) {
            boolean skip = Environment.class.isAssignableFrom(parameter.getType());
            for (final Annotation a : parameter.getAnnotations()) {
                final CrestAnnotation crestAnnotation = a.annotationType().getAnnotation(CrestAnnotation.class);
                if (crestAnnotation != null) {
                    skip = crestAnnotation.skipUsage();
                    break;
                }
            }
            if (!skip) {
                skip = parameter.getAnnotation(NotAService.class) == null &&
                        Environment.ENVIRONMENT_THREAD_LOCAL.get().findService(parameter.getType()) != null;
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
    public Object exec(final Map<Class<?>, InternalInterceptor> globalInterceptors, final String... rawArgs) {
        final List<Object> list;
        try {
            list = parse(rawArgs);
        } catch (final Exception e) {
            reportWithHelp(e);
            throw toRuntimeException(e);
        }

        return exec(globalInterceptors, list);
    }

    public Object exec(final Map<Class<?>, InternalInterceptor> globalInterceptors, final List<Object> list) {
        return interceptors == null || interceptors.length == 0 ?
                doInvoke(list) :
                new InternalInterceptorInvocationContext(globalInterceptors, interceptors, name, parameterMetadatas, method, list) {
                    @Override
                    protected Object doInvoke(final List<Object> parameters) {
                        return CmdMethod.this.doInvoke(parameters);
                    }
                }.proceed();
    }

    private List<ParameterMetadata> buildApiParameterViews(final List<Param> parameters) {
        final List<ParameterMetadata> parameterMetadatas = new ArrayList<>();
        for (final Param param : parameters) {
            // precompute all values to get a fast runtime immutable structure
            final ParameterMetadata.ParamType type = OptionParam.class.isInstance(param) ? OPTION :
                    (ComplexParam.class.isInstance(param) ? BEAN_OPTION :
                            (Environment.class.isAssignableFrom(param.getType()) || param.getAnnotation(In.class) != null
                                    || param.getAnnotation(Out.class) != null || param.getAnnotation(Err.class) != null ? INTERNAL :
                                    (Environment.ENVIRONMENT_THREAD_LOCAL.get().findService(param.getType()) != null ? SERVICE : ParameterMetadata.ParamType.PLAIN)));

            if (type == INTERNAL) { // some pre runtime checks
                if (param.isAnnotationPresent(In.class)) {
                    if (InputStream.class != param.getType()) {
                        throw new IllegalArgumentException("@In only supports InputStream injection");
                    }
                } else if (param.isAnnotationPresent(Out.class)) {
                    if (PrintStream.class != param.getType()) {
                        throw new IllegalArgumentException("@Out only supports PrintStream injection");
                    }
                } else if (param.isAnnotationPresent(Err.class)) {
                    if (PrintStream.class != param.getType()) {
                        throw new IllegalArgumentException("@Err only supports PrintStream injection");
                    }
                }
            }

            final String name = type == ParameterMetadata.ParamType.OPTION ? OptionParam.class.cast(param).getName() : null;
            final List<ParameterMetadata> nested = type == BEAN_OPTION ? buildApiParameterViews(ComplexParam.class.cast(param).parameters) : null;

            final ParameterMetadata parameterMetadata = new ParameterMetadata() {
                @Override
                public ParamType getType() {
                    return type;
                }

                @Override
                public String getName() {
                    return name;
                }

                @Override
                public List<ParameterMetadata> getNested() {
                    return nested;
                }

                @Override
                public Type getReflectType() {
                    return param.getGenericType();
                }

                @Override
                public boolean isListable() {
                    return param.isListable();
                }

                @Override
                public Class<?> getComponentType() {
                    return param.getListableType();
                }

                @Override
                public String toString() {
                    return getType() + ": " + getReflectType() + ", name=" + getName() + ", nested=" + getNested();
                }
            };
            param.setApiView(parameterMetadata);
            parameterMetadatas.add(parameterMetadata);
        }
        return unmodifiableList(parameterMetadatas);
    }

    protected Object doInvoke(final List<Object> list) {
        final Object[] args;
        try {
            args = list.toArray();
            BeanValidation.validateParameters(target.getInstance(method), method, args);
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
            throw new CommandFailedException(cause, getName());
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

        throw new HelpPrintedException(e);
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

    public Spec getSpec() {
        return spec;
    }

    @Override
    public void manual(final PrintStream out) {
        final CommandJavadoc commandJavadoc = CommandJavadoc.getCommandJavadocs(method, name);

        if (commandJavadoc == null) {
            help(out);
            return;
        }

        final Javadoc javadoc = JavadocParser.parse(commandJavadoc.getJavadoc());

        final Document.Builder manual = Document.builder()
                .heading("NAME")
                .paragraph(name)
                .heading("SYNOPSIS")
                .paragraph(getUsage());

        {
            final Document description = DocumentParser.parser(javadoc.getContent());
            if (description.getElements().size() > 0) {
                manual.heading("DESCRIPTION")
                        .inline(description);
            }
        }

        if (spec.getOptions().size() > 0) {
            manual.heading("OPTIONS");

            final List<Item> items = Help.getItems(method, name, spec.options.values(), commandJavadoc);

            for (final Item item : items) {

                final Document.Builder description = Document.builder();
                if (item.getDescription() != null) {
                    description.inline(parseOptionDescription(item.getDescription()));
                }

                if (has(item.getNote())) {
                    final String notes = Join.join(". ", item.getNote());
                    description.paragraph(notes);
                }

                manual.element(new org.tomitribe.crest.help.Option(item.getFlag(), description.build()));
            }
        }

        if (javadoc.getDeprecated() != null) {
            manual.heading("Deprecated");
            final Javadoc.Deprecated deprecated = javadoc.getDeprecated();
            if (deprecated.getContent() == null || deprecated.getContent().length() == 0) {
                manual.paragraph("Command has been marked deprecated.");
            } else {
                manual.paragraph(deprecated.getContent());
            }
        }

        if (has(javadoc.getSees())) {
            manual.heading("SEE ALSO");
            javadoc.getSees().forEach(see -> manual.paragraph(see.getContent()));
        }

        if (has(javadoc.getAuthors())) {
            manual.heading("AUTHORS");
            javadoc.getAuthors().forEach(author -> manual.paragraph(author.getContent()));
        }

        final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        final boolean color = !environment.getEnv().containsKey("NOCOLOR");
        final DocumentFormatter formatter = new DocumentFormatter(100, color);
        final String format = formatter.format(manual.build());

        final boolean less = !environment.getEnv().containsKey("NOLESS");
        if (!less) {
            out.print(format);
        } else {
            try {
                final File tempFile = File.createTempFile("help-", ".txt");
                tempFile.deleteOnExit();
                IO.copy(IO.read(format), tempFile);

                final Process process = new ProcessBuilder("less", "-r")
                        .inheritIO()
                        .redirectInput(tempFile)
                        .start();
                final int exit = process.waitFor();

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (InterruptedException e) {
                Thread.interrupted();
                throw new IllegalStateException(e);
            }
        }
    }

    public static boolean has(final List<?> list) {
        return list != null && list.size() > 0;
    }

    @Override
    public void help(final PrintStream out) {
        out.println();
        out.print("Usage: ");
        out.println(getUsage());
        out.println();

        Help.optionHelp(method, getName(), spec.options.values(), out);
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

        final List<Value> converted = convert(args, needed, parameters);

        if (!args.list.isEmpty()) {
            throw new IllegalArgumentException("Excess arguments: " + Join.join(", ", args.list));
        }

        if (!args.options.isEmpty()) {
            throw new IllegalArgumentException("Unknown arguments: " + Join.join(", ", STRING_NAME_CALLBACK, args.options.keySet()));
        }

        return toArgs(converted);
    }

    private List<Object> toArgs(final List<Value> converted) {
        final List<Object> objects = new ArrayList<>(converted.size());
        for (final Value v : converted) {
            objects.add(v.getValue());
        }
        return objects;
    }

    private List<Value> convert(Arguments args, Needed needed, List<Param> parameters1) {
        /**
         * Here we iterate over the method's parameters and convert strings into their equivalent Option or Arg value.
         *
         * The result is a List of objects that matches perfectly the available of arguments required to pass into the
         * java.lang.reflect.Method.invoke() method.
         *
         * Thus, iteration order is very significant in this loop.
         */
        final List<Value> converted = new ArrayList<>(args.options.size() /*approx but better than nothing*/);
        final Environment environment = Environment.ENVIRONMENT_THREAD_LOCAL.get();
        for (final Param parameter : parameters1) {
            final ParameterMetadata apiView = parameter.getApiView();
            switch (apiView.getType()) {
                case INTERNAL: {
                    if (parameter.isAnnotationPresent(In.class)) {
                        converted.add(new Value(environment.getInput(), false));
                        needed.count--;
                    } else if (parameter.isAnnotationPresent(Out.class)) {
                        converted.add(new Value(environment.getOutput(), false));
                        needed.count--;
                    } else if (parameter.isAnnotationPresent(Err.class)) {
                        converted.add(new Value(environment.getError(), false));
                        needed.count--;
                    } else if (Environment.class.isAssignableFrom(parameter.getType())) {
                        converted.add(new Value(environment, false));
                        needed.count--;
                    }
                    break;
                }
                case SERVICE:
                    converted.add(new Value(environment.findService(parameter.getType()), false));
                    break;
                case PLAIN:
                    if (!args.list.isEmpty()) {
                        needed.count--;
                        converted.add(fillPlainParameter(args, needed, parameter));
                    } else {
                        throw new MissingArgumentException(parameter.getDisplayType().replace("[]", "..."));
                    }
                    break;
                case BEAN_OPTION:
                    converted.add(ComplexParam.class.cast(parameter).convert(args, needed));
                    break;
                case OPTION:
                    converted.add(fillOptionParameter(args, parameter, apiView.getName()));
                    break;
                default:
                    throw new IllegalStateException("Unsupported ParamType: " + apiView.getType());
            }
        }
        return converted;
    }

    private Value fillOptionParameter(final Arguments args, final Param parameter, final String name) {
        final String value = args.options.remove(name);
        if (parameter.isListable()) {
            return convert(parameter, OptionParam.getSeparatedValues(value), name);
        }
        final Object convert = Converter.convert(value, parameter.getType(), name);
        return new Value(convert, value != null && !value.equals(OptionParam.class.cast(parameter).getDefaultValue()));
    }

    private Value fillPlainParameter(final Arguments args, final Needed needed, final Param parameter) {
        if (parameter.isListable()) {
            final List<String> glob = new ArrayList<>(args.list.size());
            for (int i = args.list.size(); i > needed.count; i--) {
                glob.add(args.list.remove(0));
            }
            return convert(parameter, glob, null);
        } else {
            final String value = args.list.remove(0);
            return new Value(Converter.convert(value, parameter.getType(), parameter.getDisplayType().replace("[]", "...")), value != null);
        }
    }

    private static Value convert(final Param parameter, final List<String> values, final String name) {
        final Class<?> type = parameter.getListableType();

        if (parameter.isAnnotationPresent(Required.class) && values.isEmpty()) {
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

            return new Value(array, !values.isEmpty());

        }

        final Collection<Object> collection = instantiate((Class<? extends Collection>) parameter.getType());
        for (final String string : values) {
            collection.add(Converter.convert(string, type, description));
        }
        return new Value(collection, !collection.isEmpty());
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
                return new TreeSet<>();
            }
            if (SortedSet.class.isAssignableFrom(aClass)) {
                return new TreeSet<>();
            }
            if (Set.class.isAssignableFrom(aClass)) {
                return new LinkedHashSet<>();
            }

            // Queues
            if (Deque.class.isAssignableFrom(aClass)) {
                return new LinkedList<>();
            }
            if (Queue.class.isAssignableFrom(aClass)) {
                return new LinkedList<>();
            }

            // Lists
            if (List.class.isAssignableFrom(aClass)) {
                return new ArrayList<>();
            }

            // Collection
            if (Collection.class.isAssignableFrom(aClass)) {
                return new LinkedList<>();
            }

            // Iterable
            if (Iterable.class.isAssignableFrom(aClass)) {
                return new LinkedList<>();
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
        final Map<String, String> options = new HashMap<>();

        for (final OptionParam parameter : spec.options.values()) {
            options.put(parameter.getName(), parameter.getDefaultValue());
        }

        return options;
    }

    private class Arguments {
        private final List<String> list = new ArrayList<>();
        private final Map<String, String> options = new HashMap<>();

        private Arguments(final String[] rawArgs) {

            final Map<String, String> defaults = getDefaults();
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
                                final Set<String> repeated) {
            String name;
            String value;
            String prefix = defaultPrefix;

            if (arg.indexOf('=') > 0) {
                name = arg.substring(arg.indexOf(prefix) + prefix.length(), arg.indexOf('='));
                if (!defaults.containsKey(name) && !spec.aliases.containsKey(name)) {
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

            if (!defaults.containsKey(name) && spec.aliases.containsKey(name)) {
                // check the options to find see if name is an alias for an option
                // if it is, get the actual optionparam name
                name = spec.aliases.get(name).getName();
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
                final String value = Substitution.format(target, method, entry.getValue(), defaultsFinder);
                map.put(entry.getKey(), value);
            }
        }

        private void checkInvalid(final List<String> invalid) {
            if (!invalid.isEmpty()) {
                throw new IllegalArgumentException("Unknown options: " + Join.join(", ", STRING_NAME_CALLBACK, invalid));
            }
        }

        private void checkRequired(final Map<String, String> supplied) {
            final List<String> required = new ArrayList<>();
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

            if (!required.isEmpty()) {
                throw new IllegalArgumentException("Required: " + Join.join(", ", STRING_NAME_CALLBACK, required));
            }
        }

        private void checkRepeated(final Set<String> repeated) {
            if (!repeated.isEmpty()) {
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
        final List<String> result = new ArrayList<>();
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
        final List<String> result = new ArrayList<>();
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
        final List<String> result = new ArrayList<>();
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
        List<String> results = new ArrayList<>();
        results.addAll(findMatcingParametersOptions(prefix, isIncludeAliasChar));
        results.addAll(findMatchingAliasOptions(prefix, isIncludeAliasChar));
        return results;
    }

    public static final class Value {
        private final Object value;
        private final boolean provided;

        protected Value(final Object value, final boolean provided) {
            this.value = value;
            this.provided = provided;
        }

        public Object getValue() {
            return value;
        }

        public boolean isProvided() {
            return provided;
        }
    }
}
