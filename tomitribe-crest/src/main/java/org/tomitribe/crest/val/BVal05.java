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
import javax.validation.Validation;
import javax.validation.bootstrap.ProviderSpecificBootstrap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class BVal05 implements BeanValidation.BeanValidationImpl {
    private static final Class<?>[] NO_GROUP = new Class<?>[0];

    private final Class<?> unwrapClass;
    private final Class<?> providerClass;
    private final Method validateMethodParameters;
    private final Method validateConstructorParamters;
    private final Method byProviderMethod;

    public BVal05() { // not static for laziness
        try {
            final ClassLoader classLoader = BVal05.class.getClassLoader();
            unwrapClass = classLoader.loadClass("org.apache.bval.jsr303.extensions.MethodValidator");
            providerClass = classLoader.loadClass("org.apache.bval.jsr303.ApacheValidationProvider");
            byProviderMethod = Validation.class.getMethod("byProvider", Class.class);
            validateMethodParameters = unwrapClass.getMethod("validateParameters", Class.class, Method.class, Object[].class, Class[].class);
            validateConstructorParamters = unwrapClass.getMethod("validateParameters", Class.class, Constructor.class, Object[].class, Class[].class);
        } catch (final Exception e) {
            throw new IllegalStateException("BVal 0.5 is not available", e);
        }
    }

    @Override
    public void validateParameters(final Object instanceOrClass, final Method method, final Object[] parameters) {
        if (instanceOrClass == null) { // static command
            return;
        }
        try {
            final Set<ConstraintViolation<?>> violations = Set.class.cast(
                validateMethodParameters.invoke(getValidatorObject(), instanceOrClass.getClass(), method, parameters, NO_GROUP));
            if (violations.size() > 0) {
                throw new ConstraintViolationException(violations);
            }
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void validateParameters(final Constructor constructor, final Object[] parameters) {
        try {
            final Set<ConstraintViolation<?>> violations = Set.class.cast(
                validateConstructorParamters.invoke(getValidatorObject(), constructor.getDeclaringClass(), constructor, parameters, NO_GROUP));
            if (violations.size() > 0) {
                throw new ConstraintViolationException(violations);
            }
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }

    private Object getValidatorObject() {
        try {
            final ProviderSpecificBootstrap<?> provider = (ProviderSpecificBootstrap<?>) byProviderMethod.invoke(null, providerClass);
            return provider.configure().buildValidatorFactory().getValidator().unwrap(unwrapClass);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
