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
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableValidator;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Set;

class BeanValidation11 extends BeanValidationMessages {
    private final ValidatorFactory validatorFactory;

    public BeanValidation11() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @Override
    public void validateParameters(final Object instanceOrClass, final Method method, final Object[] parameters) {
        if (instanceOrClass == null) { // bval 11 doesnt support it
            return;
        }
        final ExecutableValidator executableValidator = validatorFactory.getValidator().forExecutables();
        final Set<ConstraintViolation<Object>> violations = executableValidator.validateParameters(instanceOrClass, method, parameters);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @Override
    public void validateParameters(final Constructor constructor, final Object[] parameters) {
        final ExecutableValidator executableValidator = validatorFactory.getValidator().forExecutables();
        final Set<ConstraintViolation<?>> violations = executableValidator.validateConstructorParameters(constructor, parameters);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
