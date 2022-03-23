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
package org.tomitribe.crest.val;

import java.util.function.Function;

/**
 * A simple interceptor to validate parameters and returned value using
 * bean validation spec. It doesn't use group for now.
 */
public class BeanValidation {
    private BeanValidation() {
        // no-op
    }

    public static BeanValidationImpl create(final Function<Class<?>, Object> validatorLookup) {
        BeanValidationImpl impl = null;
        final ClassLoader loader = BeanValidation.class.getClassLoader();
        try {
            Class.forName("javax.validation.executable.ExecutableValidator", false, loader);
            impl = new BeanValidation11();
        } catch (final ClassNotFoundException e) {
            try {
                Class.forName("org.apache.bval.jsr303.extensions.MethodValidator", false, loader);
                impl = new BVal05();
            } catch (final ClassNotFoundException cnfe) {
                // no-op
            }
        }
        return new BuiltInValidation(impl, validatorLookup);
    }
}

