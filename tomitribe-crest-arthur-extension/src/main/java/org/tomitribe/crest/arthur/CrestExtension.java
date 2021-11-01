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
        final Collection<Method> commands = context.findAnnotatedMethods(Command.class);

        final List<String> keptClasses = toClasses(context, commands)
                .filter(extensionFilter::test)
                .collect(toList());

        registerReflection(context, keptClasses);
        registerCommandsLoader(context, keptClasses);
    }

    private void registerCommandsLoader(final Context context, final List<String> keptClasses) {
        context.addNativeImageOption("-H:TomitribeCrestCommands=" + dump(
                Paths.get(requireNonNull(context.getProperty("workingDirectory"), "workingDirectory property")),
                "crest-commands.txt",
                String.join("\n", keptClasses)));
    }

    private void registerReflection(Context context, List<String> keptClasses) {
        keptClasses.stream()
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

    private Stream<String> toClasses(final Context context, final Collection<Method> commands) {
        return commands.stream()
                .flatMap(m -> Stream.concat(
                        Stream.of(m.getAnnotation(Command.class).interceptedBy()),
                        Stream.of(m.getDeclaringClass()).flatMap(context::findHierarchy)))
                .distinct()
                .map(Class::getName)
                .sorted();
    }

    private ClassReflectionModel toModel(final String name) {
        final ClassReflectionModel model = new ClassReflectionModel();
        model.setName(name);
        model.setAllPublicMethods(true);
        model.setAllPublicConstructors(true);
        return model;
    }
}
