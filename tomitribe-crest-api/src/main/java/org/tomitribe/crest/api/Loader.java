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
package org.tomitribe.crest.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
 */
public interface Loader extends Iterable<Class<?>> {

    static Iterator<Class<?>> of(final Class<?> commandClass, final Class<?>... commandClasses) {
        final List<Class<?>> list = new ArrayList<>();
        list.add(commandClass);
        list.addAll(Arrays.asList(commandClasses));
        return list.iterator();
    }
}
