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
package org.tomitribe.crest.cli.api.interceptor.interactive;

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;
import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.crest.cli.api.CliEnvironment;
import org.tomitribe.crest.cli.api.interceptor.base.ParameterVisitor;
import org.tomitribe.crest.environments.Environment;

import java.lang.reflect.AnnotatedElement;

public class AskMissingParameters {
    private AskMissingParameters() {
        // no-op
    }

    @CrestInterceptor
    public static Object intercept(final CrestContext context) {
        ParameterVisitor.visit(context, new ParameterVisitor.DefaultOptionVisitor() {
            @Override
            public Object doOnOption(int index, ParameterMetadata meta, AnnotatedElement annotations) {
                return read(meta.getName(), annotations.getAnnotation(Interactivable.class));
            }
        });
        return context.proceed();
    }

    private static String read(final String name, final Interactivable interactivable) {
        if (interactivable == null) {
            return null;
        }
        final CliEnvironment e = CliEnvironment.class.cast(Environment.ENVIRONMENT_THREAD_LOCAL.get());
        final String prompt = "Enter " + name + ": ";
        final String val = interactivable.password() ? e.readPassword(prompt) : e.readInput(prompt);
        return "null".equals(val) ? null : val;
    }
}

