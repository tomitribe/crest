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

import org.tomitribe.crest.EditorLoader;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CmdGroup;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.OverloadedCmdMethod;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.contexts.DefaultsContext;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.val.BeanValidationImpl;
import org.tomitribe.util.Strings;
import org.tomitribe.util.collect.FilteredIterable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public class Commands {

    private Commands() {
        // no-op
    }

    public static Iterable<Method> commands(final Class<?> clazz) {
        return new FilteredIterable<>(
                new FilteredIterable<>(
                        methods(clazz).collect(toList()),
                        method -> method.isAnnotationPresent(Command.class)),
                method -> {
                    if (!method.isAccessible()) {
                        method.setAccessible(true);
                    }
                    return true;
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

        final Map<String, Cmd> map = new HashMap<>();

        for (final Method method : commands(clazz)) {

            final CmdMethod cmd = new CmdMethod(
                    method, target, dc,
                    ofNullable(Environment.ENVIRONMENT_THREAD_LOCAL.get())
                            .map(e -> e.findService(BeanValidationImpl.class))
                            .orElse(null));

            final Cmd existing = map.get(cmd.getName());

            if (existing == null) {

                map.put(cmd.getName(), cmd);

            } else if (existing instanceof OverloadedCmdMethod) {

                final OverloadedCmdMethod overloaded = (OverloadedCmdMethod) existing;
                overloaded.add(cmd);

            } else {

                final OverloadedCmdMethod overloaded = new OverloadedCmdMethod(cmd.getName());
                overloaded.add((CmdMethod) existing);
                overloaded.add(cmd);
                map.put(overloaded.getName(), overloaded);
            }
        }

        if (clazz.isAnnotationPresent(Command.class)) {

            final CmdGroup cmdGroup = new CmdGroup(clazz, map);

            final HashMap<String, Cmd> group = new HashMap<>();
            group.put(cmdGroup.getName(), cmdGroup);

            return group;

        }
        return map;
    }

    public static String name(final Method method) {
        final Command command = method.getAnnotation(Command.class);
        if (command == null) {
            return method.getName();
        }
        return value(command.value(), method.getName());
    }

    public static String name(final Class<?> clazz) {
        final Command command = clazz.getAnnotation(Command.class);
        final String defaultName = Strings.lcfirst(clazz.getSimpleName());
        if (command == null) {
            return defaultName;
        }
        return value(command.value(), defaultName);
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

        // Let them tell us the list of classes to use
        final LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();

        addAll(classes, ServiceLoader.load(Loader.class, loader).iterator());
        addAll(classes, ServiceLoader.load(org.tomitribe.crest.api.Loader.class, loader).iterator());

        // if maven plugin has been used just let add the found classes
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
                                onClass(classes, loader.loadClass(line));
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
                onClass(classes, clazz);
            }
        }
    }

    // enable to handle auto-loading of some features specifically, in particular editor implicit auto-loading
    private static void onClass(final Collection<Class<?>> classes, final Class<?> clazz) {
        if (EditorLoader.class == clazz) { // was loaded but we don't need anything else but triggering the init
            EditorLoader.Lazy.lightInit(Thread.currentThread().getContextClassLoader());
        } else {
            classes.add(clazz);
        }
    }

    // not only public since we want to ease code encapsulation and script like style
    private static Stream<Method> methods(final Class<?>clazz) {
        if (clazz == null || clazz == Object.class) {
            return Stream.empty();
        }
        return Stream.concat(Stream.of(clazz.getDeclaredMethods()), methods(clazz.getSuperclass()));
    }
}
