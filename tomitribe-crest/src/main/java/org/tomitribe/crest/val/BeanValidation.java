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

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.bval.jsr303.ApacheValidatorConfiguration;
import org.apache.bval.jsr303.extensions.MethodValidator;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * A simple interceptor to validate parameters and returned value using
 * bean validation spec. It doesn't use group for now.
 */
public class BeanValidation {

    private BeanValidation() {
        // no-op
    }

    public static boolean isActive() {
        try {
            Class.forName("javax.validation.Validator", false, BeanValidation.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    public static void validateParameters(final Class clazz, final Method method, final Object[] parameters) throws Exception {
        if (!isActive()) {
            return;
        }

        Helper.validateParameters(clazz, method, parameters);
    }

    public static void validateParameters(final Class clazz, final Constructor constructor, final Object[] parameters) throws Exception {
        if (!isActive()) {
            return;
        }

        Helper.validateParameters(clazz, constructor, parameters);
    }

    public static Iterable<? extends String> messages(final Exception e) {
        if (!ConstraintViolationException.class.isInstance(e)) {
            return asList(e.getMessage());
        }
        final Collection<String> msg = new LinkedList<String>();
        final ConstraintViolationException cve = (ConstraintViolationException) e;
        for (final ConstraintViolation<?> violation : cve.getConstraintViolations()) {
            msg.add(violation.getMessage());
        }
        return msg;
    }

    // Note: using bval 1.1 will just make it portable
    private static class Helper { // use for laziness of loading

        public static void validateParameters(final Class clazz, final Method method, final Object[] parameters) {
            final MethodValidator validatorObject = getValidatorObject();
            final Set<ConstraintViolation<?>> violations = validatorObject.validateParameters(clazz, method, parameters);

            if (violations.size() > 0) {
                throw new ConstraintViolationException(violations);
            }
        }

        public static void validateParameters(final Class clazz, final Constructor constructor, final Object[] parameters) {
            final MethodValidator validatorObject = getValidatorObject();
            final Set<ConstraintViolation<?>> violations = validatorObject.validateParameters(clazz, constructor, parameters);

            if (violations.size() > 0) {
                throw new ConstraintViolationException(violations);
            }
        }

        private static MethodValidator getValidatorObject() {
            final ApacheValidatorConfiguration configure = Validation.byProvider(ApacheValidationProvider.class).configure();
            final ValidatorFactory validatorFactory = configure.buildValidatorFactory();
            return validatorFactory.getValidator().unwrap(org.apache.bval.jsr303.extensions.MethodValidator.class);
        }
    }
}

