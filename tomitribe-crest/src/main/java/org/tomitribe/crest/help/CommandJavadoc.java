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
import org.tomitribe.util.hash.XxHash64;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandJavadoc {
    private final String clazzName;
    private final String commandName;
    private final String hash;
    private final Properties properties = new Properties();

    public CommandJavadoc(final String clazzName, final String commandName, final String hash) {
        this.clazzName = clazzName;
        this.commandName = commandName;
        this.hash = hash;
    }


    public void setJavadoc(final String javadoc) {
        this.getProperties().put("@javadoc", javadoc);
    }

    public String getJavadoc() {
        return this.getProperties().getProperty("@javadoc");
    }

    public String getClazzName() {
        return clazzName;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getHash() {
        return hash;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getResourceFileName() {
        return getResourceFileName(clazzName, commandName, hash);
    }

    public static String getResourceFileName(final String clazzName, final String commandName, final String hash) {
        return String.format("META-INF/crest/%s/%s.%s.properties", clazzName, commandName, hash);
    }

    /**
     * Build the canonical signature string for a method. This must produce
     * bit-identical output for both the annotation-processor view of a method
     * (via javax.lang.model) and the runtime reflection view (java.lang.reflect.Method).
     *
     * Form: {@code <declaringClass>.<methodName>(<paramType1>,<paramType2>,...)}
     *
     * Inner-class dollar signs are normalized to dots so reflection's
     * {@code Outer$Inner} matches APT's {@code Outer.Inner}.
     */
    public static String canonicalSignature(final String declaringClassName, final String methodName, final List<String> paramTypeNames) {
        final String params = paramTypeNames.stream()
                .map(s -> s.replace('$', '.'))
                .collect(Collectors.joining(","));
        return declaringClassName.replace('$', '.') + "." + methodName + "(" + params + ")";
    }

    public static String signatureHash(final String declaringClassName, final String methodName, final List<String> paramTypeNames) {
        final String canonical = canonicalSignature(declaringClassName, methodName, paramTypeNames);
        return String.format("%016x", XxHash64.hash(canonical));
    }

    /**
     * Render a reflection {@link Class} in a form that matches the APT
     * {@code Types.erasure(...).toString()} output. Specifically:
     * arrays render as {@code <component>[]} (recursively, so {@code String[][]}
     * becomes {@code java.lang.String[][]}, not the JVM-internal
     * {@code [[Ljava.lang.String;}).
     */
    public static String classSignature(final Class<?> aClass) {
        if (aClass.isArray()) {
            return classSignature(aClass.getComponentType()) + "[]";
        }
        return aClass.getName();
    }

    public static CommandJavadoc loadJavadoc(final String clazzName, final String commandName, final String hash) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        final String resourceFileName = getResourceFileName(clazzName, commandName, hash);
        final URL resource = loader.getResource(resourceFileName);
        if (resource == null) return null;

        final CommandJavadoc javadoc = new CommandJavadoc(clazzName, commandName, hash);
        try (final InputStream in = IO.read(resource)) {
            javadoc.getProperties().load(in);
        } catch (IOException e) {
            throw new InvalidJavadocFileException(resourceFileName, e);
        }
        return javadoc;
    }

    public static CommandJavadoc getCommandJavadocs(final Method method, final String name) {
        final List<String> paramTypes = Stream.of(method.getParameterTypes())
                .map(CommandJavadoc::classSignature)
                .collect(Collectors.toList());
        final String hash = signatureHash(
                method.getDeclaringClass().getName(),
                method.getName(),
                paramTypes);
        final String clazzName = method.getDeclaringClass().getName().replace('$', '.');
        return loadJavadoc(clazzName, name, hash);
    }
}
