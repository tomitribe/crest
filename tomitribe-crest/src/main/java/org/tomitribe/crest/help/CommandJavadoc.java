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

import org.tomitribe.util.IO;
import org.tomitribe.util.Join;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandJavadoc {
    private final String clazzName;
    private final String commandName;
    private final Properties properties = new Properties();

    public CommandJavadoc(final String clazzName, final String commandName) {
        this.clazzName = clazzName;
        this.commandName = commandName;
    }


    public void setJavadoc(final String javadoc) {
        this.getProperties().put("@javadoc", javadoc);
    }

    public String getJavadoc() {
        return this.getProperties().getProperty("@javadoc");
    }

    public void setArgNames(final List<String> argNames) {
        this.getProperties().put("@arg.names", Join.join(", ", argNames));
    }

    public List<String> getArgNames() {
        final String property = getProperties().getProperty("@arg.names");
        return Arrays.asList(property.split(", "));
    }

    public void setArgTypes(final List<String> argTypes) {

        final List<String> normalized = argTypes.stream()
                .map(s -> s.replaceAll("<[^<>]+>", ""))
                .collect(Collectors.toList());
        this.getProperties().put("@arg.types", Join.join(", ", normalized));
    }

    public boolean matches(final Class<?>[] types) {
        final List<String> list = Stream.of(types)
                .map(this::classSignature)
                .collect(Collectors.toList());
        final String expected = normalize(Join.join(", ", list));
        final String argtypes = normalize(getProperties().getProperty("@arg.types"));
        return expected.equals(argtypes);
    }

    /**
     * The javax.lang.model.element.Element.getSimpleName() method is used to
     * get the class name of each argument type.  This method will use a dot (.)
     * before inner class names.  The string it provides will
     * be in the format of {@code org.example.Outter.Inner}.  This is the way
     * these classes are referred to in source, so it is correct from that
     * perspective.
     *
     * However, java.lang.Class.getName() will use the dollar sign ($) before
     * inner class names.  The string it provides will
     * be in the format of {@code org.example.Outter$Inner}.  This is the way
     * these classes are referred to by classloaders, so it is correct from that
     * perspective.
     *
     * This method just says to heck with it and replaces all $ signs with dots.
     *
     * Though you can legally use a dollar sign in a class name, you will get a
     * duplicate class error by the compiler if you do so in a way that could
     * class with an inner class of the same name.  So this approach should be
     * fairly safe.
     */
    private String normalize(final String signature) {
        return signature.replace("$", ".");
    }

    public String classSignature(final Class<?> aClass) {
        if (aClass.isArray()) {
            return String.format("%s[]", aClass.getComponentType().getName());
        } else {
            return aClass.getName();
        }
    }

    public String getClazzName() {
        return clazzName;
    }

    public String getCommandName() {
        return commandName;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getName() {
        return String.format("%s/%s", clazzName, commandName);
    }

    public String getResourceFileName(final int i) {
        return getResourceFileName(clazzName, commandName, i);
    }

    public static String getResourceFileName(final String clazzName, final String commandName, final int i) {
        return String.format("META-INF/crest/%s/%s.%s.properties", clazzName, commandName, i);
    }

    public static List<CommandJavadoc> loadJavadoc(final Class clazz, final String commandName) {
        return loadJavadoc(clazz.getName().replace('$', '.'), commandName);
    }

    public static List<CommandJavadoc> loadJavadoc(final String clazzName, final String commandName) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final List<CommandJavadoc> javadocs = new ArrayList<>();

        for (int i = 0; true; i++) {
            final String resourceFileName = getResourceFileName(clazzName, commandName, i);
            final URL resource = loader.getResource(resourceFileName);
            if (resource == null) break;

            final CommandJavadoc javadoc = new CommandJavadoc(clazzName, commandName);
            try (final InputStream in = IO.read(resource)) {
                javadoc.getProperties().load(in);
                javadocs.add(javadoc);
            } catch (IOException e) {
                throw new InvalidJavadocFileException(resourceFileName, e);
            }
        }

        return javadocs;
    }

    public static CommandJavadoc getCommandJavadocs(final Method method, final String name) {
        final List<CommandJavadoc> javadocs = loadJavadoc(method.getDeclaringClass(), name);

        return javadocs.stream()
                .filter(commandJavadoc -> commandJavadoc.matches(method.getParameterTypes()))
                .findFirst().orElse(null);
    }
}
