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

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;

import static java.util.Collections.singletonList;

/**
 * A simple interceptor to validate parameters and returned value using
 * bean validation spec. It doesn't use group for now.
 */
public class BeanValidation {
    private static final BeanValidationImpl IMPL;
    static {
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
        IMPL = impl;
    }

    private BeanValidation() {
        // no-op
    }

    public static boolean isActive() {
        return IMPL != null;
    }

    public static void validateParameters(final Object instance, final Method method, final Object[] parameters) throws Exception {
        if (!isActive()) {
            return;
        }

        IMPL.validateParameters(instance, method, parameters);
    }

    public static void validateParameters(final Constructor constructor, final Object[] parameters) throws Exception {
        if (!isActive()) {
            return;
        }

        IMPL.validateParameters(constructor, parameters);
    }

    public static Iterable<? extends String> messages(final Exception e) {
        if (!ConstraintViolationException.class.isInstance(e)) {
            return singletonList(e.getMessage());
        }
        final Collection<String> msg = new LinkedList<>();
        final ConstraintViolationException cve = (ConstraintViolationException) e;
        for (final ConstraintViolation<?> violation : cve.getConstraintViolations()) {
            msg.add(violation.getMessage());
        }
        return msg;
    }

    public interface BeanValidationImpl {
        void validateParameters(Object instanceOrClass, Method method, Object[] parameters);
        void validateParameters(Constructor constructor, Object[] parameters);
    }
}

