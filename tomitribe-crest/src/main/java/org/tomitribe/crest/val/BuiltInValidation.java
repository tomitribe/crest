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

import org.tomitribe.crest.api.validation.Validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class BuiltInValidation implements BeanValidationImpl {
    private final BeanValidationImpl sibling;
    private final Map<Executable, Consumer<Object[]>> validatorPerExecutable = new ConcurrentHashMap<>();
    private final Function<Class<?>, Object> lookup;

    public BuiltInValidation(final BeanValidationImpl sibling, final Function<Class<?>, Object> lookup) {
        this.sibling = sibling;
        this.lookup = lookup;
    }

    @Override
    public void validateParameters(final Object instanceOrClass, final Method method, final Object[] parameters) {
        doValidateParameters(method, parameters);
        if (sibling != null) {
            sibling.validateParameters(instanceOrClass, method, parameters);
        }
    }

    @Override
    public void validateParameters(final Constructor constructor, final Object[] parameters) {
        doValidateParameters(constructor, parameters);
        if (sibling != null) {
            sibling.validateParameters(constructor, parameters);
        }
    }

    @Override
    public Optional<List<String>> messages(final Throwable exception) {
        return Optional.of(exception)
                .filter(ValidationMessages.class::isInstance)
                .map(ValidationMessages.class::cast)
                .map(e -> e.messages);
    }

    private Object createInstance(final Class<?> value) {
        return ofNullable(lookup.apply(value)).orElseGet(() -> {
            try {
                return value.getConstructor().newInstance();
            } catch (final InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            } catch (final InvocationTargetException e) {
                throw new IllegalStateException(e.getTargetException());
            }
        });
    }

    private void doValidateParameters(final Executable executable, final Object[] parameters) {
        validatorPerExecutable.computeIfAbsent(executable, e -> {
            if (executable.getParameterCount() == 0) {
                return a -> {
                };
            }
            final Consumer[] validators = Stream.of(executable.getParameters())
                    .map(this::toValidator)
                    .toArray(Consumer[]::new);
            return args -> executeValidations(validators.length, i -> validators[i].accept(args[i]));
        }).accept(parameters);
    }

    private Consumer<Object> toValidator(final Parameter parameter) {
        final Consumer[] validations = Stream.of(parameter.getAnnotations())
                .filter(it -> it.annotationType().isAnnotationPresent(Validation.class))
                .map(it -> {
                    final Class<?> value = it.annotationType().getAnnotation(Validation.class).value();
                    final Object validationInstance = createInstance(value);
                    if (Consumer.class.isInstance(validationInstance)) {
                        return Consumer.class.cast(validationInstance);
                    }
                    if (BiConsumer.class.isInstance(validationInstance)) {
                        final BiConsumer<Annotation, Object> biConsumer = BiConsumer.class.cast(validationInstance);
                        return (Consumer<Object>) a -> biConsumer.accept(it, a);
                    }
                    throw new IllegalArgumentException("Invalid validation, expected Consumer or BiConsumer but got " + value);
                })
                .toArray(Consumer[]::new);
        if (validations.length == 0) {
            return a -> {
            };
        }
        return a -> executeValidations(validations.length, i -> validations[i].accept(a));
    }

    private <T> void executeValidations(final int max, final IntConsumer validate) {
        ValidationMessages exception = null;
        for (int i = 0; i < max; i++) {
            try {
                validate.accept(i);
            } catch (final RuntimeException re) {
                if (exception == null) {
                    exception = new ValidationMessages(new ArrayList<>());
                }
                if (ValidationMessages.class.isInstance(re)) {
                    exception.messages.addAll(ValidationMessages.class.cast(re).messages);
                } else {
                    exception.messages.add(re.getMessage());
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    private static class ValidationMessages extends RuntimeException {
        private final List<String> messages;

        private ValidationMessages(final List<String> messages) {
            super(String.join(", ", messages));
            this.messages = messages;
        }
    }
}
