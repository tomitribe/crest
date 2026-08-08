/*
 * Copyright 2026 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.interceptor;

import java.lang.reflect.Method;

public class InterceptorOptionConflictException extends IllegalArgumentException {
    public InterceptorOptionConflictException(final String option, final Method commandMethod,
                                              final Class<?> interceptorClass, final String detail) {
        super(String.format("Option \"%s\" declared by interceptor %s conflicts with the option declared" +
                        " by command method %s.%s: %s.  The same option may be shared when the declarations are" +
                        " identical.  Rename one of the options or make the types and defaults match.",
                option, interceptorClass.getName(),
                commandMethod.getDeclaringClass().getSimpleName(), commandMethod.getName(), detail));
    }
}
