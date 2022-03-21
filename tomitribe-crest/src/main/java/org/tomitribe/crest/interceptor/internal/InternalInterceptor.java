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
import org.tomitribe.crest.cmds.targets.SimpleBean;
import org.tomitribe.crest.cmds.targets.Target;
import org.tomitribe.crest.interceptor.InterceptorAnnotationNotFoundException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class InternalInterceptor {
    private final Target target;
    private final Method method;

    public InternalInterceptor(final Target target, final Method method) {
        this.target = target;
        this.method = method;
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

    public static InternalInterceptor from(final Class<?> clazz){
        for (final Method method : clazz.getMethods()) {
            if (Object.class == method.getDeclaringClass()) {
                continue;
            }

            final CrestInterceptor interceptor = method.getAnnotation(CrestInterceptor.class);
            if (interceptor != null) {
                return new InternalInterceptor(new SimpleBean(null), method);
            }
        }

        throw new InterceptorAnnotationNotFoundException(clazz);
    }
}
