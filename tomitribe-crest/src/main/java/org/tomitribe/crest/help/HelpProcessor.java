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
package org.tomitribe.crest.help;


import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HelpProcessor extends AbstractProcessor {
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(Command.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        final Map<String, List<CommandJavadoc>> all = roundEnv.getElementsAnnotatedWith(Command.class).stream()
                .filter(annotatedElement -> annotatedElement.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .map(this::processCommand)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(CommandJavadoc::getName));

        all.values().forEach(this::writeAll);

        return true;
    }


    private void writeAll(final List<CommandJavadoc> list) {
        for (int i = 0; i < list.size(); i++) {
            final CommandJavadoc commandJavadoc = list.get(i);
            storeProperties(commandJavadoc.getResourceFileName(i), commandJavadoc.getProperties());
        }
    }

    private CommandJavadoc processCommand(final ExecutableElement executableElement) {
        final String commandName = getCommandName(executableElement);
        final String className = executableElement.getEnclosingElement().toString();

        final CommandJavadoc commandJavadoc = new CommandJavadoc(className, commandName);

        { // write method javadoc
            final String javadoc = processingEnv.getElementUtils().getDocComment(executableElement);
            if (javadoc != null) {
                commandJavadoc.setJavadoc(javadoc);
            }
        }

        // Add the parameter data
        for (final VariableElement parameter : executableElement.getParameters()) {
            final Option option = parameter.getAnnotation(Option.class);
            if (option == null) continue;
            for (final String optionName : option.value()) {
                commandJavadoc.getProperties().put(optionName, parameter.getSimpleName() + "");
            }
        }

        { // Record the arg names
            final List<String> argNames = executableElement.getParameters().stream()
                    .map(Element::getSimpleName)
                    .map(Objects::toString)
                    .collect(Collectors.toList());

            commandJavadoc.setArgNames(argNames);
        }

        { // Record the arg types
            final List<String> argTypes = executableElement.getParameters().stream()
                    .map(VariableElement.class::cast)
                    .map(Element::asType)
                    .map(TypeMirror::toString)
                    .collect(Collectors.toList());

            commandJavadoc.setArgTypes(argTypes);
        }

        return commandJavadoc;
    }

    private void storeProperties(final String resourceFile, final Properties properties) {
        try {
            final Filer filer = this.processingEnv.getFiler();
            final FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceFile);
            properties.store(file.openOutputStream(), null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String getCommandName(final ExecutableElement executableElement) {
        final Command command = executableElement.getAnnotation(Command.class);

        return Stream.of(command.value(), executableElement.getSimpleName() + "")
                .filter(Objects::nonNull)
                .filter(s -> s.length() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalElementException("Illegal command with no name", executableElement));
    }

}
