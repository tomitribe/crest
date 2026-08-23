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


import com.google.auto.service.AutoService;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.GlobalOptions;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
public class HelpProcessor extends AbstractProcessor {

    /**
     * The bean resource files already written this compilation, so a bean
     * declared by many commands is stored once
     */
    private final Set<String> storedBeans = new HashSet<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotations = new LinkedHashSet<String>();
        annotations.add(Command.class.getCanonicalName());
        annotations.add(CrestInterceptor.class.getCanonicalName());
        return annotations;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        roundEnv.getElementsAnnotatedWith(Command.class).stream()
                .filter(annotatedElement -> annotatedElement.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .map(this::processCommand)
                .forEach(javadoc -> storeProperties(javadoc.getResourceFileName(), javadoc.getProperties()));

        /*
         * Interceptor methods document the options they contribute to the
         * commands they are bound to.  Their javadoc is stored under the
         * method's own name — interceptors have no @Command name — so the
         * runtime can reconstruct the resource file from the Method alone.
         *
         * A @Command method carrying @CrestInterceptor merely names its
         * interceptor class; its javadoc is already stored above.
         */
        roundEnv.getElementsAnnotatedWith(CrestInterceptor.class).stream()
                .filter(annotatedElement -> annotatedElement.getKind() == ElementKind.METHOD)
                .filter(annotatedElement -> annotatedElement.getAnnotation(Command.class) == null)
                .map(ExecutableElement.class::cast)
                .map(this::processInterceptor)
                .forEach(javadoc -> storeProperties(javadoc.getResourceFileName(), javadoc.getProperties()));

        return true;
    }


    private CommandJavadoc processCommand(final ExecutableElement executableElement) {
        return processMethod(executableElement, getCommandName(executableElement));
    }

    private CommandJavadoc processInterceptor(final ExecutableElement executableElement) {
        return processMethod(executableElement, executableElement.getSimpleName().toString());
    }

    private CommandJavadoc processMethod(final ExecutableElement executableElement, final String commandName) {
        final String className = executableElement.getEnclosingElement().toString();
        final Types types = processingEnv.getTypeUtils();

        final List<String> paramTypes = executableElement.getParameters().stream()
                .map(VariableElement::asType)
                .map(types::erasure)
                .map(Object::toString)
                .collect(Collectors.toList());

        final String hash = CommandJavadoc.signatureHash(
                className,
                executableElement.getSimpleName().toString(),
                paramTypes);

        final CommandJavadoc commandJavadoc = new CommandJavadoc(className, commandName, hash);

        { // write method javadoc
            final String javadoc = processingEnv.getElementUtils().getDocComment(executableElement);
            if (javadoc != null) {
                commandJavadoc.setJavadoc(javadoc);
            }
        }

        // Add the parameter data
        for (final VariableElement parameter : executableElement.getParameters()) {

            processBean(parameter.asType());

            final Option option = parameter.getAnnotation(Option.class);
            if (option == null) continue;
            for (final String optionName : option.value()) {
                commandJavadoc.getProperties().put(optionName, parameter.getSimpleName() + "");
            }
        }

        return commandJavadoc;
    }

    /**
     * Captures the javadoc of an @Options or @GlobalOptions bean reachable
     * as a method parameter.  The options a bean contributes are documented
     * as @param entries on its constructor, so the resource file is keyed
     * by the bean's class name alone and the runtime resolves it from the
     * parameter type.  Nested beans — a bean whose constructor takes
     * another bean — are captured recursively.
     *
     * When a bean has several documented constructors the first documented
     * one supplies the javadoc; option-to-parameter mappings merge from all
     * constructors.
     */
    private void processBean(final TypeMirror typeMirror) {
        final Element element = processingEnv.getTypeUtils().asElement(typeMirror);
        if (!(element instanceof TypeElement)) return;

        final TypeElement typeElement = (TypeElement) element;
        if (typeElement.getAnnotation(Options.class) == null && typeElement.getAnnotation(GlobalOptions.class) == null) {
            return;
        }

        final String className = typeElement.getQualifiedName().toString();
        final String resourceFileName = CommandJavadoc.getBeanResourceFileName(className);

        // Beans are shared: many commands may declare the same bean.  Store
        // its file once per compilation; this also breaks bean cycles.
        if (!storedBeans.add(resourceFileName)) return;

        final CommandJavadoc beanJavadoc = new CommandJavadoc(className, "bean", CommandJavadoc.beanHash(className));

        { // the bean class javadoc carries its narrative
            final String classJavadoc = processingEnv.getElementUtils().getDocComment(typeElement);
            if (classJavadoc != null) {
                beanJavadoc.setClassJavadoc(classJavadoc);
            }
        }

        for (final ExecutableElement constructor : ElementFilter.constructorsIn(typeElement.getEnclosedElements())) {

            if (beanJavadoc.getJavadoc() == null) {
                final String javadoc = processingEnv.getElementUtils().getDocComment(constructor);
                if (javadoc != null) {
                    beanJavadoc.setJavadoc(javadoc);
                }
            }

            for (final VariableElement parameter : constructor.getParameters()) {

                processBean(parameter.asType());

                final Option option = parameter.getAnnotation(Option.class);
                if (option == null) continue;
                for (final String optionName : option.value()) {
                    beanJavadoc.getProperties().put(optionName, parameter.getSimpleName() + "");
                }
            }
        }

        storeProperties(resourceFileName, beanJavadoc.getProperties());
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

        final String fullName = Stream.of(command.value(), executableElement.getSimpleName() + "")
                .filter(Objects::nonNull)
                .filter(s -> s.length() > 0)
                .findFirst()
                .orElseThrow(() -> new IllegalElementException("Illegal command with no name", executableElement));

        // Use only the leaf name for resource file generation.
        // Multi-word @Command values like "setting add" represent a path;
        // the command name is the last token.
        final int lastSpace = fullName.lastIndexOf(' ');
        return lastSpace < 0 ? fullName : fullName.substring(lastSpace + 1);
    }

}
