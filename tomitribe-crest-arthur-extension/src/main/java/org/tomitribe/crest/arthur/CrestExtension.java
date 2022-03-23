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
package org.tomitribe.crest.arthur;

import org.apache.geronimo.arthur.spi.ArthurExtension;
import org.apache.geronimo.arthur.spi.model.ClassReflectionModel;
import org.apache.geronimo.arthur.spi.model.DynamicProxyModel;
import org.apache.geronimo.arthur.spi.model.ResourceBundleModel;
import org.apache.geronimo.arthur.spi.model.ResourceModel;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Editor;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.table.Border;
import org.tomitribe.crest.table.TableInterceptor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class CrestExtension implements ArthurExtension {
    @Override
    public void execute(final Context context) {
        final boolean alreadyScanned = Boolean.parseBoolean(context.getProperty("tomitribe.crest.useInPlaceRegistrations"));
        if (alreadyScanned) {
            doRegisters(context, doLoadCrestTxt("crest-commands.txt"), doLoadCrestTxt("crest-editors.txt"));
            return;
        }

        final Predicate<String> extensionFilter = context.createIncludesExcludes("tomitribe.crest.command.", PredicateType.STARTS_WITH);
        final Predicate<String> editorsFilter = context.createIncludesExcludes("tomitribe.crest.editors.", PredicateType.STARTS_WITH);
        final Collection<Method> commands = context.findAnnotatedMethods(Command.class);

        final List<String> commandsAndInterceptors = toClasses(context, commands)
                .filter(extensionFilter)
                .collect(toList());
        if (!commandsAndInterceptors.contains(TableInterceptor.class.getName())) {
            commandsAndInterceptors.add(TableInterceptor.class.getName()); // forced in Main
        }
        if (!commandsAndInterceptors.contains(Help.class.getName())) {
            commandsAndInterceptors.add(Help.class.getName()); // forced in Main
        }
        registerReflection(context, singletonList(Border.class.getName())); // TableInterceptor does reflection on it

        // table can extract methods/fields from the returned type so register it
        registerReflection(context, context.findAnnotatedMethods(Table.class).stream()
                .flatMap(it -> extractTableType(it.getGenericReturnType()))
                .flatMap(context::findHierarchy)
                .distinct()
                .map(Class::getName)
                .collect(toList()));

        final List<String> editors = findEditors(context)
                .filter(editorsFilter)
                .collect(toList());

        doRegisters(context, commandsAndInterceptors, editors);
        optionalJavaxBeanValidationRegistration(context);
    }

    private Stream<Class<?>> extractTableType(final Type genericReturnType) {
        if (Class.class.isInstance(genericReturnType)) {
            final Class<?> type = Class.class.cast(genericReturnType);
            if (type.isPrimitive() || type == String.class) {
                return Stream.empty();
            }
            return Stream.of(type);
        }
        if (ParameterizedType.class.isInstance(genericReturnType)) {
            final ParameterizedType pt = ParameterizedType.class.cast(genericReturnType);
            if (Stream.class == pt.getRawType() ||
                    Collection.class == pt.getRawType() ||
                    Map.class == pt.getRawType() ||
                    Set.class == pt.getRawType() ||
                    List.class == pt.getRawType()) {
                return extractTableType(pt.getActualTypeArguments()[pt.getActualTypeArguments().length - 1]);
            }
        }
        return Stream.empty();
    }

    private void optionalJavaxBeanValidationRegistration(final Context context) {
        try {
            context.loadClass("javax.validation.Validation");

            final ResourceBundleModel validationMessagesBundle = new ResourceBundleModel();
            validationMessagesBundle.setName("org.apache.bval.jsr.ValidationMessages");
            context.register(validationMessagesBundle);

            registerConstructorReflection(context, singletonList("org.apache.bval.jsr.ApacheValidatorFactory"));

            final Class<? extends Annotation> constraint = context.loadClass("javax.validation.Constraint").asSubclass(Annotation.class);
            final Collection<Class<?>> constraintAnnotations = context.findAnnotatedClasses(constraint);
            final List<String> constraints = constraintAnnotations.stream()
                    .flatMap(it -> Stream.concat(Stream.of(it), extractConstraintValidatedBy(it, constraint)))
                    .map(Class::getName)
                    .collect(toList());
            registerReflection(context, constraints);
            registerAnnotationProxies(context, constraintAnnotations.stream().map(Class::getName).distinct().collect(toList()));

            {
                final ClassReflectionModel typesFields = new ClassReflectionModel();
                typesFields.setName("org.apache.bval.jsr.ConstraintAnnotationAttributes$Types");
                typesFields.setAllDeclaredFields(true);
                context.register(typesFields);
            }

            try (final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/apache/bval/jsr/DefaultConstraints.properties")) {
                if (stream != null) {
                    final Properties properties = new Properties();
                    properties.load(stream);

                    final ResourceModel defaultConstraintsRegistry = new ResourceModel();
                    defaultConstraintsRegistry.setPattern("org/apache/bval/jsr/DefaultConstraints.properties");
                    context.register(defaultConstraintsRegistry);

                    final List<String> defaultConstraints = properties.stringPropertyNames().stream()
                            .map(properties::getProperty)
                            .flatMap(it -> Stream.of(it.split(",")))
                            .map(String::trim)
                            .filter(it -> !it.isEmpty())
                            .map(context::loadClass)
                            .map(Class::getName)
                            .collect(toList());
                    registerReflection(context, defaultConstraints);
                    registerAnnotationProxies(context, properties.stringPropertyNames());
                }
            }
        } catch (final Error | Exception e) {
            // no-op, skip
        }
    }

    private void registerAnnotationProxies(final Context context, Collection<String> names) {
        names.forEach(name -> {
            final DynamicProxyModel dynamicProxyModel = new DynamicProxyModel();
            dynamicProxyModel.setClasses(singletonList(name));
            context.register(dynamicProxyModel);
        });
    }

    private Stream<Class<?>> extractConstraintValidatedBy(final Class<?> it, final Class<? extends Annotation> constraint) {
        try {
            final Annotation value = it.getAnnotation(constraint);
            return Stream.of((Class<?>[]) value.annotationType().getMethod("validatedBy").invoke(value));
        } catch (final Exception e) {
            return Stream.empty();
        }
    }

    private void doRegisters(final Context context, final List<String> commandsAndInterceptors, final List<String> editors) {
        registerReflection(context, commandsAndInterceptors);
        registerConstructorReflection(context, editors);

        if (!editors.isEmpty()) { // uses a static block so init at runtime
            registerEditorsLoader(context, editors);
            registerCommandsLoader(context,
                    // ensure editors are loaded early add EditorLoader in this SPI registration
                    Stream.concat(
                                    Stream.of("org.tomitribe.crest.EditorLoader"),
                                    commandsAndInterceptors.stream())
                            .collect(toList()));

            context.register(toClassReflection("org.tomitribe.crest.EditorLoader"));
        } else {
            registerCommandsLoader(context, commandsAndInterceptors);
        }
    }

    private boolean isExplicitClass(final String name) {
        return !TableInterceptor.class.getName().equals(name) && !Help.class.getName().equals(name);
    }

    private void registerEditorsLoader(final Context context, final List<String> keptClasses) {
        context.addNativeImageOption("-H:TomitribeCrestEditors=" + dump(
                Paths.get(requireNonNull(context.getProperty("workingDirectory"), "workingDirectory property")),
                "crest-editors.txt",
                String.join("\n", keptClasses)));
    }

    private void registerCommandsLoader(final Context context, final List<String> keptClasses) {
        context.addNativeImageOption("-H:TomitribeCrestCommands=" + dump(
                Paths.get(requireNonNull(context.getProperty("workingDirectory"), "workingDirectory property")),
                "crest-commands.txt",
                keptClasses.stream()
                        // manually registered but we want the reflection registration so filtering there to avoid conflicts
                        .filter(this::isExplicitClass)
                        .collect(joining("\n"))));
    }

    private void registerConstructorReflection(final Context context, final List<String> classes) {
        classes.stream()
                .map(this::toConstructorModel)
                .forEach(context::register);
    }

    private void registerReflection(final Context context, final List<String> classes) {
        classes.stream()
                .map(this::toModel)
                .forEach(context::register);
    }

    private String dump(final Path workDir, final String name, final String value) {
        if (!java.nio.file.Files.isDirectory(workDir)) {
            try {
                java.nio.file.Files.createDirectories(workDir);
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        }
        final Path out = workDir.resolve(name);
        try {
            Files.write(
                    out, value.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return out.toAbsolutePath().toString();
    }

    private Stream<String> findEditors(final Context context) {
        // normally we would need context.findImplementations(Editor.class).stream() but since it is
        // a standard SPI, graal can do it alone so don't do it twice
        return context.findAnnotatedClasses(Editor.class).stream()
                .distinct()
                .map(Class::getName)
                .sorted();
    }

    private Stream<String> toClasses(final Context context, final Collection<Method> commands) {
        return commands.stream()
                .flatMap(m -> Stream.concat(
                        findInterceptors(context, m),
                        Stream.of(m.getDeclaringClass())))
                .distinct()
                .flatMap(context::findHierarchy)
                .distinct() // hierarchy can overlap but doing 2 distincts we have less classes to handle overall
                .map(Class::getName)
                .sorted();
    }

    private Stream<Class<?>> findInterceptors(final Context context, final Method method) {
        return Stream.concat(
                findBindingInterceptors(context, method),
                Stream.of(method.getAnnotation(Command.class).interceptedBy()));
    }

    private Stream<Class<?>> findBindingInterceptors(final Context context, final Method method) {
        return Stream.of(method.getAnnotations())
                .map(Annotation::annotationType)
                .filter(it -> it.isAnnotationPresent(CrestInterceptor.class))
                .flatMap(marker -> context.findAnnotatedClasses(marker).stream()
                        .filter(it -> Stream.of(it.getMethods()).anyMatch(m -> m.isAnnotationPresent(CrestInterceptor.class))));
    }

    private ClassReflectionModel toClassReflection(final String name) {
        final ClassReflectionModel model = new ClassReflectionModel();
        model.setName(name);
        return model;
    }

    private ClassReflectionModel toConstructorModel(final String name) {
        final ClassReflectionModel model = new ClassReflectionModel();
        model.setName(name);
        model.setAllPublicConstructors(true);
        return model;
    }

    private ClassReflectionModel toModel(final String name) {
        final ClassReflectionModel model = new ClassReflectionModel();
        model.setName(name);
        model.setAllPublicMethods(true);
        model.setAllPublicConstructors(true);
        return model;
    }


    private List<String> doLoadCrestTxt(final String name) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            return list(loader.getResources(name)).stream()
                    .flatMap(url -> {
                        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                            return reader.lines()
                                    .filter(it -> !it.isEmpty() && !it.startsWith("#"))
                                    .map(it -> {
                                        try {
                                            return loader.loadClass(it).getName();
                                        } catch (final Error | Exception e) {
                                            return null;
                                        }
                                    })
                                    .filter(Objects::nonNull)
                                    .collect(toList()) // materialize before we close the stream
                                    .stream();
                        } catch (final IOException ioe) {
                            return Stream.empty();
                        }
                    })
                    .collect(toList());
        } catch (final IOException e) {
            return emptyList();
        }
    }
}
