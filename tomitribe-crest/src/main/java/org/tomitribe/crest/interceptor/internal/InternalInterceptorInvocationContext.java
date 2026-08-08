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

import java.lang.reflect.Method;
import java.util.List;

public abstract class InternalInterceptorInvocationContext {
    private static final Object[] NO_OPTIONS = new Object[0];

    private final List<InternalInterceptor> chain;
    private final List<Object[]> interceptorOptions;
    private final CrestContext context;

    private List<Object> parameters;
    private int index = 0;

    public InternalInterceptorInvocationContext(final List<InternalInterceptor> chain,
                                                final List<Object[]> interceptorOptions,
                                                final String name,
                                                final List<ParameterMetadata> parameterMetadatas,
                                                final Method method,
                                                final List<Object> parameters) {
        this.chain = chain;
        this.interceptorOptions = interceptorOptions;
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
        if (index < chain.size()) {
            final Object[] options = interceptorOptions == null ? NO_OPTIONS : interceptorOptions.get(index);
            return chain.get(index++).intercept(context, options);
        }
        return doInvoke(parameters);
    }

    protected abstract Object doInvoke(List<Object> parameters);
}