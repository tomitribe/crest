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
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Editor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class CrestExtension implements ArthurExtension {
    @Override
    public void execute(final Context context) {
        final Predicate<String> extensionFilter = context.createIncludesExcludes("tomitribe.crest.command.", PredicateType.STARTS_WITH);
        final Predicate<String> editorsFilter = context.createIncludesExcludes("tomitribe.crest.editors.", PredicateType.STARTS_WITH);
        final Collection<Method> commands = context.findAnnotatedMethods(Command.class);

        final List<String> commandsAndInterceptors = toClasses(context, commands)
                .filter(extensionFilter)
                .collect(toList());

        final List<String> editors = findEditors(context)
                .filter(editorsFilter)
                .collect(toList());

        registerReflection(context, commandsAndInterceptors);
        registerConstructorReflection(context, editors);
        registerEditorsLoader(context, editors);
        registerCommandsLoader(context, editors.isEmpty() ?
                commandsAndInterceptors :
                // ensure editors are loaded early add EditorLoader in this SPI registration
                Stream.concat(Stream.of("org.tomitribe.crest.EditorLoader"), commandsAndInterceptors.stream())
                        .collect(toList()));

        if (!editors.isEmpty()) { // uses a static block so init at runtime
            context.register(toClassReflection("org.tomitribe.crest.EditorLoader"));
            context.initializeAtRunTime("org.tomitribe.crest.EditorLoader");
        }
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
                String.join("\n", keptClasses)));
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
                        findInterceptors(m),
                        Stream.of(m.getDeclaringClass())))
                .distinct()
                .flatMap(context::findHierarchy)
                .distinct() // hierarchy can overlap but doing 2 distincts we have less classes to handle overall
                .map(Class::getName)
                .sorted();
    }

    private Stream<Class<?>> findInterceptors(final Method method) {
        return Stream.of(method.getAnnotation(Command.class).interceptedBy());
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
}
