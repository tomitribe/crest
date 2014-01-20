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
package org.tomitribe.crest;

import org.tomitribe.crest.api.Command;
import org.tomitribe.util.collect.FilteredIterable;
import org.tomitribe.util.collect.FilteredIterator;
import org.tomitribe.util.reflect.Reflection;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Commands {

    public static Iterable<Method> commands(Class<?> clazz) {
        return new FilteredIterable<Method>(Reflection.methods(clazz),
                new FilteredIterator.Filter<Method>() {
                    @Override
                    public boolean accept(Method method) {
                        return method.isAnnotationPresent(Command.class);
                    }
                }
        );
    }

    public static Map<String, Cmd> get(Object bean) {
        return get(bean.getClass(), new SimpleBean(bean));
    }

    public static Map<String, Cmd> get(Class<?> clazz) {
        return get(clazz, new SimpleBean(null));
    }

    public static Map<String, Cmd> get(Class<?> clazz, final Target target) {
        if (target == null) throw new IllegalArgumentException("Target cannot be null");

        final Map<String, Cmd> map = new HashMap<String, Cmd>();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                final CmdMethod cmd = new CmdMethod(method, target);

                final Cmd existing = map.get(cmd.getName());
                if (existing == null) {
                    map.put(cmd.getName(), cmd);
                } else if (existing instanceof CmdGroup) {
                    final CmdGroup group = (CmdGroup) existing;
                    group.add(cmd);
                } else {
                    final CmdGroup group = new CmdGroup(cmd.getName());
                    group.add((CmdMethod) existing);
                    group.add(cmd);
                    map.put(group.getName(), group);
                }
            }
        }
        return map;
    }
}
