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
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.api.CrestAnnotation;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Defaults;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.crest.cmds.validator.IterableMessagesException;
import org.tomitribe.crest.cmds.validator.ParameterValidator;
import org.tomitribe.util.reflect.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.util.Arrays.asList;
import static org.tomitribe.crest.exception.Exceptions.toRuntimeException;

public class ComplexParam extends Param {
    private final List<Param> parameters;
    private final Constructor<?> constructor;
    private final CmdMethod method;
    private final List<ParameterValidator> validators;

    public ComplexParam(final CmdMethod method, final Parameter parent, final Class<?> type) {
        super(parent);

        this.constructor = type.getConstructors()[0];
        this.parameters = Collections.unmodifiableList(method.buildParams(params(parent, constructor)));
        this.method = method;
        this.validators = method.getValidators();
    }

    public Object convert(final Arguments arguments, final Arguments.Needed needed) {

        final List<Object> converted = method.convert(arguments, needed, parameters);

        try {
            final Object[] args = converted.toArray();

            if (!validators.isEmpty()) {
                if (validators.size() == 1) {
                    validators.get(0).validate(constructor.getDeclaringClass(), constructor, args);
                } else {
                    for (final ParameterValidator validator : validators) {
                        validator.validate(constructor.getDeclaringClass(), constructor, args);
                    }
                }
            }

            return constructor.newInstance(args);
        } catch (final InvocationTargetException e) {
            throw toException(e.getCause());
        } catch (final Exception e) {
            return toException(e);
        }
    }

    private RuntimeException toException(final Throwable e) {
        throw toRuntimeException(IterableMessagesException.class.isInstance(e) ? IterableMessagesException.class.cast(e).getCause() : e);
    }

    // translate method parameter annotations to constructor annotation when relevant
    // algo is this one:
    // - if method has @Option then merge with @Option in constructor param
    // - if method has @Defaults with a matching mapping then override @Default on constructor
    // - all annotation not belonging to the JVM or crest are propagated if not decorated with @CrestAnnotation
    //   (ie all not internal config is propagated)
    // TODO: recursive, ie support @Option on method param, @Option on constructor param, @option on constructor param...
    private static Iterable<Parameter> params(final Parameter parent, final Constructor constructor) {
        final Option option = parent.getAnnotation(Option.class);
        final Defaults defaults = parent.getAnnotation(Defaults.class);

        final Collection<Annotation> toAddAnnotations = new ArrayList<Annotation>();
        for (final Annotation a : parent.getAnnotations()) {
            final String name = a.annotationType().getName();
            if (name.startsWith("org.tomitribe.crest.api.") || name.startsWith("java")) {
                continue;
            }
            if (a.annotationType().getAnnotation(CrestAnnotation.class) == null) {
                continue;
            }
            toAddAnnotations.add(a);
        }

        return new Iterable<Parameter>() {
            @Override
            public Iterator<Parameter> iterator() {
                return new Iterator<Parameter>() {
                    private int index = 0;

                    @Override
                    public boolean hasNext() {
                        return index < constructor.getParameterTypes().length;
                    }

                    @Override
                    public Parameter next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }

                        Annotation[] annotations = constructor.getParameterAnnotations()[index];
                        Collection<Annotation> newOpts = null;

                        String name = null;
                        if (option != null) {
                            newOpts = new ArrayList<Annotation>(annotations.length);

                            Option constructorOption = null;
                            for (final Annotation a : annotations) {
                                if (a.annotationType() == Option.class) {
                                    constructorOption = Option.class.cast(a);
                                    break;
                                } else {
                                    newOpts.add(a);
                                }
                            }

                            if (constructorOption != null) {
                                name = constructorOption.value()[0];

                                final String[] values = new String[option.value().length * constructorOption.value().length];
                                int idx = 0;
                                for (final String pOpt : option.value()) {
                                    for (final String opt : constructorOption.value()) {
                                        values[idx++] = pOpt + opt;
                                    }
                                }
                                newOpts.add(new Option() {
                                    @Override
                                    public boolean equals(final Object obj) {
                                        return Option.class.isInstance(obj) && asList(Option.class.cast(obj).value()).equals(asList(values));
                                    }

                                    @Override
                                    public int hashCode() {
                                        return asList(values).hashCode();
                                    }

                                    @Override
                                    public String toString() {
                                        return "@Option(" + asList(values).toString().replace("[", "").replace("]", "") + ")";
                                    }

                                    @Override
                                    public Class<? extends Annotation> annotationType() {
                                        return Option.class;
                                    }

                                    @Override
                                    public String[] value() {
                                        return values;
                                    }
                                });
                            }
                        }
                        if (defaults != null) {
                            if (name == null) {
                                for (final Annotation a : annotations) {
                                    if (a.annotationType() == Option.class) {
                                        name = Option.class.cast(a).value()[0];
                                        break;
                                    }
                                }
                            }

                            String newVal = null;
                            if (name != null) {
                                for (final Defaults.DefaultMapping mapping : defaults.value()) {
                                    if (mapping.name().equals(name)) {
                                        newVal = mapping.value();
                                        break;
                                    }
                                }
                            }

                            if (newVal != null) {
                                if (newOpts == null) {
                                    newOpts = new ArrayList<Annotation>(annotations.length);
                                    for (final Annotation a : annotations) {
                                        if (a.annotationType() != Default.class) {
                                            newOpts.add(a);
                                        }
                                    }
                                }
                                final String annotVal = newVal;
                                newOpts.add(new Default() {
                                    @Override
                                    public boolean equals(final Object obj) {
                                        return Default.class.isInstance(obj) && Default.class.cast(obj).value().equals(annotVal);
                                    }

                                    @Override
                                    public int hashCode() {
                                        return annotVal.hashCode();
                                    }

                                    @Override
                                    public String toString() {
                                        return "@Default(" + annotVal + ")";
                                    }

                                    @Override
                                    public Class<? extends Annotation> annotationType() {
                                        return Default.class;
                                    }

                                    @Override
                                    public String value() {
                                        return annotVal;
                                    }
                                });
                            }
                        }

                        if (!toAddAnnotations.isEmpty()) {
                            if (newOpts == null) {
                                newOpts = new ArrayList<Annotation>(annotations.length);
                                Collections.addAll(newOpts, annotations);
                            }
                            newOpts.addAll(toAddAnnotations);
                        }

                        return new Parameter(
                                newOpts != null ? newOpts.toArray(new Annotation[newOpts.size()]) : annotations,
                                constructor.getParameterTypes()[index],
                                constructor.getGenericParameterTypes()[index++]);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
