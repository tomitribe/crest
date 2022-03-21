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
import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.crest.interceptor.UnresolvedInterceptorAnnotationException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public abstract class InternalInterceptorInvocationContext {
    private final Map<Class<?>, InternalInterceptor> interceptors;
    private final CrestContext context;
    private final Class<?>[] interceptorKeys;

    private List<Object> parameters;
    private int index = 0;

    public InternalInterceptorInvocationContext(final Map<Class<?>, InternalInterceptor> interceptors,
                                                final Class<?>[] interceptorKeys,
                                                final String name,
                                                final List<ParameterMetadata> parameterMetadatas,
                                                final Method method,
                                                final List<Object> parameters) {
        this.interceptorKeys = interceptorKeys;
        this.interceptors = interceptors;
        this.parameters = parameters;
        this.context = new CrestContext() {
            @Override
            public Object proceed() {
                return InternalInterceptorInvocationContext.this.proceed();
            }

            @Override
            public Method getMethod() {
                return method;
            }

            @Override
            public List<Object> getParameters() { // mutable
                return parameters;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public List<ParameterMetadata> getParameterMetadata() {
                return parameterMetadatas;
            }
        };
    }

    public Object proceed() {
        if (index < interceptorKeys.length) {
            final Class<?> interceptorClass = interceptorKeys[index];
            InternalInterceptor internalInterceptor = interceptors.get(interceptorClass);

            if (internalInterceptor == null) {

                if (interceptorClass.isAnnotation()) {
                    throw new UnresolvedInterceptorAnnotationException(interceptorClass);
                }

                internalInterceptor = InternalInterceptor.from(interceptorClass);
            }
            index++;
            return internalInterceptor.intercept(context);
        }
        return doInvoke(parameters);
    }

    protected abstract Object doInvoke(List<Object> parameters);
}