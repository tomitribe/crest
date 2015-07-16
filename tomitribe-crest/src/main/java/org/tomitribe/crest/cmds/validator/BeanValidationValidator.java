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
package org.tomitribe.crest.cmds.validator;

import org.apache.bval.jsr303.ApacheValidationProvider;
import org.apache.bval.jsr303.ApacheValidatorConfiguration;
import org.apache.bval.jsr303.extensions.MethodValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public class BeanValidationValidator implements ParameterValidator {
    private final boolean active;

    public BeanValidationValidator() {
        active = isActive();
    }

    @Override
    public void validate(final Class clazz, final Method method, final Object[] parameters) {
        if (!active) {
            return;
        }
        Helper.validateParameters(clazz, method, parameters);
    }

    @Override
    public void validate(final Class<?> clazz, final Constructor constructor, final Object[] parameters) {
        if (!active) {
            return;
        }
        Helper.validateParameters(clazz, constructor, parameters);
    }

    public static boolean isActive() {
        try {
            Class.forName("javax.validation.Validator", false, BeanValidationValidator.class.getClassLoader());
            return true;
        } catch (final ClassNotFoundException e) {
            return false;
        }
    }

    // Note: using bval 1.1 will just make it portable
    private static class Helper { // use for laziness of loading
        private static final AtomicReference<MethodValidator> VALIDATOR = new AtomicReference<MethodValidator>();

        public static void validateParameters(final Class clazz, final Method method, final Object[] parameters) {
            throwIfNeeded(getValidatorObject().validateParameters(clazz, method, parameters));
        }

        public static void validateParameters(final Class clazz, final Constructor constructor, final Object[] parameters) {
            throwIfNeeded(getValidatorObject().validateParameters(clazz, constructor, parameters));
        }

        private static MethodValidator getValidatorObject() {
            MethodValidator existing = VALIDATOR.get();
            if (existing == null) { // don't create it N times!
                final ApacheValidatorConfiguration configure = Validation.byProvider(ApacheValidationProvider.class).configure();
                final ValidatorFactory validatorFactory = configure.buildValidatorFactory();
                existing = validatorFactory.getValidator().unwrap(org.apache.bval.jsr303.extensions.MethodValidator.class);
                if (!VALIDATOR.compareAndSet(null, existing)) {
                    existing = VALIDATOR.get();
                }
            }
            return existing;
        }

        private static void throwIfNeeded(final Set<ConstraintViolation<?>> violations) {
            if (violations.isEmpty()) {
                return;
            }
            final ConstraintViolationException constraintViolationException = new ConstraintViolationException(violations);
            final List<String> messages = new ArrayList<String>(constraintViolationException.getConstraintViolations().size());
            for (final ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
                messages.add(violation.getMessage());
            }
            throw new IterableMessagesException(messages, constraintViolationException);
        }
    }
}
