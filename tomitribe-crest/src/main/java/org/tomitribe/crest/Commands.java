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

    public static Map<String, Executable> get(Class<?> clazz) {
        final Map<String, Executable> map = new HashMap<String, Executable>();

        for (Method method : clazz.getMethods()) {
            if (method.isAnnotationPresent(Command.class)) {
                final CmdMethod cmd = new CmdMethod(method);
                map.put(cmd.getName(), cmd);
            }
        }
        return map;
    }
}
