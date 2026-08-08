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
package org.tomitribe.crest.interceptor.internal;

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.Priority;
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.interceptor.InterceptorAnnotationNotFoundException;
import org.tomitribe.crest.interceptor.InvalidInterceptorPriorityException;
import org.tomitribe.crest.interceptor.UnresolvedInterceptorAnnotationException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InternalInterceptor {

    public static final double DEFAULT_PRIORITY = 5d;

    private final Target target;
    private final Method method;
    private final double priority;

    public InternalInterceptor(final Target target, final Method method, final Class<?> clazz) {
        this.target = target;
        this.method = method;
        this.priority = readPriority(clazz);
    }

    /**
     * The valid range is greater than 0 and less than 11.  The endpoints are
     * deliberately excluded so no interceptor can take the last spot: there
     * is always room to slot in before or after any priority.  The negated
     * comparison also rejects NaN, which would otherwise scramble the sort.
     */
    private static double readPriority(final Class<?> clazz) {
        final Priority priority = clazz.getAnnotation(Priority.class);

        if (priority == null) {
            return DEFAULT_PRIORITY;
        }

        final double value = priority.value();

        if (!(value > 0 && value < 11)) {
            throw new InvalidInterceptorPriorityException(clazz, value);
        }

        return value;
    }

    public Object intercept(final CrestContext crestContext) {
        try {
            return target.invoke(method, crestContext);
        } catch (final InvocationTargetException e) {
            return throwRuntime(e.getCause());
        } catch (final IllegalAccessException e) {
            return throwRuntime(e);
        }
    }

    private static Object throwRuntime(final Throwable cause) { // try to propagate if possible
        throw RuntimeException.class.isInstance(cause) ? RuntimeException.class.cast(cause) : new IllegalStateException(cause);
    }

    public double getPriority() {
        return priority;
    }

    /**
     * Resolves the interceptor keys bound to a command method into the
     * chain that will actually run.  Keys may be interceptor classes or
     * custom interceptor annotations registered in the supplied registry.
     *
     * The same interceptor may be reachable through several keys, e.g. an
     * interceptor class carrying two custom annotations both used on the
     * method.  Such duplicates are removed so an interceptor runs once.
     * The surviving interceptors are sorted by {@link Priority}, ties
     * keeping the order the keys were declared on the method.
     */
    public static List<InternalInterceptor> resolve(final Map<Class<?>, InternalInterceptor> registry, final Class<?>[] keys) {
        final List<InternalInterceptor> chain = new ArrayList<>();
        final Set<InternalInterceptor> seen = Collections.newSetFromMap(new IdentityHashMap<>());

        for (final Class<?> key : keys) {
            InternalInterceptor interceptor = registry.get(key);

            if (interceptor == null) {

                if (key.isAnnotation()) {
                    throw new UnresolvedInterceptorAnnotationException(key);
                }

                interceptor = from(key);
            }

            if (seen.add(interceptor)) {
                chain.add(interceptor);
            }
        }

        chain.sort(Comparator.comparingDouble(InternalInterceptor::getPriority));

        return chain;
    }

    public static InternalInterceptor from(final Class<?> clazz){
        for (final Method method : clazz.getMethods()) {
            if (Object.class == method.getDeclaringClass()) {
                continue;
            }

            final CrestInterceptor interceptor = method.getAnnotation(CrestInterceptor.class);
            if (interceptor != null) {
                return new InternalInterceptor(new SimpleBean(null), method, clazz);
            }
        }

        throw new InterceptorAnnotationNotFoundException(clazz);
    }
}
