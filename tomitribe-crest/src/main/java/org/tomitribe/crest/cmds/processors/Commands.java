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
package org.tomitribe.crest.cmds.processors;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.val.BeanValidationImpl;
import org.tomitribe.util.Strings;
import org.tomitribe.util.collect.FilteredIterable;
import org.tomitribe.util.collect.FilteredIterator;
import org.tomitribe.util.reflect.Reflection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;

public class Commands {

    private Commands() {
        // no-op
    }

    public static Iterable<Method> commands(final Class<?> clazz) {
        return new FilteredIterable<>(Reflection.methods(clazz),
                new FilteredIterator.Filter<Method>() {
                    @Override
                    public boolean accept(final Method method) {
                        return method.isAnnotationPresent(Command.class);
                    }
                }
        );
    }

    public static Map<String, Cmd> get(final Object bean) {
        return get(bean.getClass(), new SimpleBean(bean), new SystemPropertiesDefaultsContext());
    }

    public static Map<String, Cmd> get(final Object bean, final DefaultsContext dc) {
        return get(bean.getClass(), new SimpleBean(bean), dc);
    }

    public static Map<String, Cmd> get(final Class<?> clazz) {
        return get(clazz, new SimpleBean(null), new SystemPropertiesDefaultsContext());
    }

    public static Map<String, Cmd> get(final Class<?> clazz, final DefaultsContext dc) {
        return get(clazz, new SimpleBean(null), dc);
    }

    public static Map<String, Cmd> get(final Class<?> clazz, final Target target, final DefaultsContext dc) {
        if (target == null) {
            throw new IllegalArgumentException("Target cannot be null");
        }

        final CmdGroup root = new CmdGroup("", Collections.emptyMap());

        for (final Method method : commands(clazz)) {

            final CmdMethod cmd = new CmdMethod(
                    clazz, method, target, dc,
                    ofNullable(Environment.ENVIRONMENT_THREAD_LOCAL.get())
                            .map(e -> e.findService(BeanValidationImpl.class))
                            .orElse(null));

            root.merge(cmd.getRoot());
        }

        return root.getCommandMap();
    }

    public static String name(final Method method) {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) {
            return method.getName();
        }
        return leafName(value(command.value(), method.getName()));
    }

    public static String name(final Class<?> clazz) {
        final Command command = clazz.getAnnotation(Command.class);
        final String defaultName = Strings.lcfirst(clazz.getSimpleName());
        if (command == null) {
            return defaultName;
        }
        return leafName(value(command.value(), defaultName));
    }

    /**
     * Returns the full path tokens for a method's @Command value.
     * Single-word values return a single-element list.
     */
    public static List<String> path(final Method method) {
        final Command command = method.getAnnotation(Command.class);
        if (command == null || command.value().isEmpty()) {
            return Collections.singletonList(method.getName());
        }
        return Arrays.asList(command.value().split("\\s+"));
    }

    /**
     * Returns the full path tokens for a class's @Command value.
     * Returns an empty list if the class has no @Command annotation.
     */
    public static List<String> path(final Class<?> clazz) {
        final Command command = clazz.getAnnotation(Command.class);
        if (command == null) {
            return Collections.emptyList();
        }
        if (command.value().isEmpty()) {
            return Collections.singletonList(Strings.lcfirst(clazz.getSimpleName()));
        }
        return Arrays.asList(command.value().split("\\s+"));
    }

    private static String leafName(final String name) {
        final int lastSpace = name.lastIndexOf(' ');
        return lastSpace < 0 ? name : name.substring(lastSpace + 1);
    }

    public static String value(final String value, final String defaultValue) {
        return value == null || value.isEmpty() ? defaultValue : value;
    }

    /**
     * Interface whose only purpose is to be used in conjunction
     * with the java.util.ServiceLoader API as one potential
     * way to load the list of classes that have commands.
     *
     * This interface intentionally has zero methods and never will
     * so that the simplest implementation is a plain java.util.ArrayList
     * (or pick your favorite collection)
     *
     * This interface is intentionally not used in any method or constructor of crest.
     * @deprecated use org.tomitribe.crest.api.Loader
     */
    @Deprecated
    public static interface Loader extends Iterable<Class<?>> {
    }

    public static Iterable<Class<?>> load() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassLoader.getSystemClassLoader();
        }

        final LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();

        // api.Loader is the primary mechanism — if any are found, they
        // are the authoritative source and we skip everything else.
        addAll(classes, ServiceLoader.load(org.tomitribe.crest.api.Loader.class, loader).iterator());

        if (!classes.isEmpty()) {
            return classes;
        }

        // Backward compat: check the deprecated Commands$Loader (used by xbean)
        addAll(classes, ServiceLoader.load(Loader.class, loader).iterator());

        if (!classes.isEmpty()) {
            return classes;
        }

        // Last resort: read crest-commands.txt directly for builds that
        // haven't updated to generate the Loader service file yet
        for (final String prefix : asList("", "/")) {
            try {
                final Enumeration<URL> urls = loader.getResources(prefix + "crest-commands.txt");
                final boolean done = urls.hasMoreElements();
                while (urls.hasMoreElements()) {
                    final URL url = urls.nextElement();
                    try (InputStream stream = url.openStream();
                         BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = normalize(line);
                            try {
                                classes.add(loader.loadClass(line));
                            } catch (final ClassNotFoundException e) {
                                // no-op: we can log it but don't fail cause one command didn't load
                            }
                        }
                    } catch (final IOException ioe) {
                        // no-op
                    }
                }
                if (done) {
                    break;
                }
            } catch (final IOException e) {
                // no-op
            }
        }

        return classes;
    }

    /**
     * Remove any whitespace to improve ability to understand what the user meant
     * Remove 'class ' at the start in case the user added a class via its toString() vs getName()
     */
    private static String normalize(String line) {
        line = line.trim();
        if (line.startsWith("class ")) {
            line = line.substring(5).trim();
        }
        return line;
    }

    private static void addAll(final LinkedHashSet<Class<?>> classes, final Iterator<? extends Iterable<Class<?>>> all) {
        while (all.hasNext()) {
            for (final Class<?> clazz : all.next()) {
                classes.add(clazz);
            }
        }
    }
}
