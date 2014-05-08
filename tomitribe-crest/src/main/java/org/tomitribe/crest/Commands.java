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
package org.tomitribe.crest;

import org.tomitribe.crest.api.Command;
import org.tomitribe.util.Strings;
import org.tomitribe.util.collect.FilteredIterable;
import org.tomitribe.util.collect.FilteredIterator;
import org.tomitribe.util.reflect.Reflection;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.ServiceLoader;

public class Commands {

    private Commands() {
        // no-op
    }

    public static Iterable<Method> commands(final Class<?> clazz) {
        return new FilteredIterable<Method>(Reflection.methods(clazz),
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

        final Map<String, Cmd> map = new HashMap<String, Cmd>();

        for (final Method method : commands(clazz)) {

            final CmdMethod cmd = new CmdMethod(method, target, dc);

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

            final HashMap<String, Cmd> group = new HashMap<String, Cmd>();
            group.put(cmdGroup.getName(), cmdGroup);

            return group;

        } else {

            return map;
        }
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
        return value == null || value.length() == 0 ? defaultValue : value;
    }

    /**
     * Interface whose only purpose is to be used in conjunction
     * with the java.util.ServiceLoader API as one potential
     * way to load the list of classes that have commands.
     * <p/>
     * This interface intentionally has zero methods and never will
     * so that the simplest implementation is a plain java.util.ArrayList
     * (or pick your favorite collection)
     * <p/>
     * This interface is intentionally not used in any method or constructor of crest.
     */
    public static interface Loader extends Iterable<Class<?>> {
    }

    /**
     * A
     *
     * @return
     */
    public static Iterable<Class<?>> load() {

        final Iterator<Loader> all = ServiceLoader.load(Loader.class).iterator();

        // Let them tell is the list of classes to use
        final LinkedHashSet<Class<?>> classes = new LinkedHashSet<Class<?>>();

        while (all.hasNext()) {
            final Iterable<Class<?>> c = all.next();
            for (final Class<?> clazz : c) {
                classes.add(clazz);
            }
        }

        return classes;
    }
}
