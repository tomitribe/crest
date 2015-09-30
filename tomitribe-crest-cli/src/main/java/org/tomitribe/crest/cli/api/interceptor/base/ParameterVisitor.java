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
package org.tomitribe.crest.cli.api.interceptor.base;

import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.ParameterMetadata;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tomitribe.util.editor.Converter.convertString;

// helper class to browse all option more easily in custom interceptors
public final class ParameterVisitor {
    private ParameterVisitor() {
        // no-op
    }

    public static void visit(final CrestContext context, final OptionVisitor visitor) {
        final List<Object> parameters = context.getParameters();
        if (parameters != null) {
            fillParams(visitor, parameters, context.getParameterMetadata(), context.getMethod().getParameterAnnotations());
        }
    }

    private static void fillParams(final OptionVisitor visitor,
                                   final List<Object> parameters, final List<ParameterMetadata> metadata,
                                   final Annotation[][] paramAnnotations) {
        for (int i = 0; i < parameters.size(); i++) {
            final ParameterMetadata parameterMetadata = metadata.get(i);
            switch (parameterMetadata.getType()) {
                case OPTION: {
                    if (parameters.get(i) != null) {
                        continue; // already set
                    }

                    final Object val = visitor.onOption(i, parameterMetadata, new AnnotatedElementImpl(paramAnnotations[i]));
                    if (val != null) {
                        parameters.set(i, val);
                    }
                    break;
                }

                case BEAN_OPTION: {
                    final Object fillBean = fillBean(visitor, parameterMetadata.getReflectType(), parameters.get(i), parameterMetadata.getNested());
                    if (fillBean != null) {
                        parameters.set(i, fillBean);
                    }
                    break;
                }

                default:
                    // no-op
            }
        }
    }

    // here try to use javabeans convention to read/write values. If name is "x.y.z" we consider the name is "z" is the bean.
    // TODO: enhance constructor handling
    private static Object fillBean(final OptionVisitor visitor, final Type type, final Object bean, final List<ParameterMetadata> metadatas) {
        if (!Class.class.isInstance(type)) { // can't do anything
            return null;
        }

        List<Object> constructorParam = bean == null ? new ArrayList<Object>() : null;
        try {
            final Constructor constructor = Class.class.cast(type).getConstructors()[0];
            final BeanInfo descriptor = Introspector.getBeanInfo(Class.class.cast(type));
            final Map<String, PropertyDescriptor> descriptorMap = new HashMap<String, PropertyDescriptor>();
            for (final PropertyDescriptor d : descriptor.getPropertyDescriptors()) {
                descriptorMap.put(d.getName(), d);
            }
            Object value = bean;
            int consIdx = -1;
            for (final ParameterMetadata metadata : metadatas) {
                consIdx++;
                if (metadata.getType() != ParameterMetadata.ParamType.OPTION) { // we support only 1 level for now
                    continue;
                }


                final String name = metadata.getName().substring(metadata.getName().lastIndexOf('.') + 1);
                if (!descriptorMap.containsKey(name)) {
                    continue;
                }

                final PropertyDescriptor propertyDescriptor = descriptorMap.get(name);
                if (propertyDescriptor == null) {
                    continue;
                }

                if (bean != null) { // check it is not already set
                    final Method readMethod = propertyDescriptor.getReadMethod();
                    if (readMethod == null) {
                        continue;
                    }
                    try {
                        final Object val = readMethod.invoke(bean);
                        if (val != null) {
                            continue;
                        }
                    } catch (final IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    } catch (InvocationTargetException e) {
                        throw new IllegalStateException(e.getCause());
                    }
                }

                final Method write = propertyDescriptor.getWriteMethod();
                if (write != null) {
                    final Object val = visitor.onOption(consIdx, metadata, new AnnotatedElementImpl(write.getParameterAnnotations()[0]));
                    if (val != null) {
                        try {
                            if (value == null) {
                                constructorParam.add(val);
                            } else {
                                write.invoke(value, val);
                            }
                        } catch (final IllegalAccessException e) {
                            throw new IllegalStateException(e);
                        } catch (final InvocationTargetException e) {
                            throw new IllegalStateException(e.getCause());
                        }
                    }

                }
            }
            if (value == null) {
                try {
                    value = constructor.newInstance(constructorParam.toArray(new Object[constructorParam.size()]));
                } catch (final IllegalAccessException e) {
                    throw new IllegalStateException(e);
                } catch (final InstantiationException e) {
                    throw new IllegalStateException(e);
                } catch (final InvocationTargetException e) {
                    throw new IllegalStateException(e.getCause());
                }
            }
            return value;
        } catch (final IntrospectionException e) {
            throw new IllegalStateException(e);
        }
    }

    public interface OptionVisitor {
        /**
         * @param index index of the current option.
         * @param meta metadata for the current option.
         * @param annotations parameter annotations.
         */
        Object onOption(int index, ParameterMetadata meta, AnnotatedElement annotations);
    }

    public static abstract class DefaultOptionVisitor implements OptionVisitor {
        protected abstract Object doOnOption(int index, ParameterMetadata meta, AnnotatedElement annotations);

        @Override
        public Object onOption(final int index, final ParameterMetadata meta, final AnnotatedElement annotations) {
            return convert(meta, doOnOption(index, meta, annotations));
        }

        private Object convert(final ParameterMetadata meta, final Object o) {
            return o == null ? null :
                (String.class.isInstance(o) && String.class != meta.getReflectType() ?
                    convertString(o.toString(), meta.getReflectType(), meta.getName()) : o);
        }
    }

    private static class AnnotatedElementImpl implements AnnotatedElement {
        private final Annotation[] annotations;

        private AnnotatedElementImpl(final Annotation[] annotations) {
            this.annotations = annotations;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            for (final Annotation a : annotations) {
                if (a.annotationType() == annotationClass) {
                    return (T) a;
                }
            }
            return null;
        }

        @Override
        public boolean isAnnotationPresent(final Class<? extends Annotation> annotationClass) {
            return getAnnotation(annotationClass) != null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return annotations;
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return getAnnotations();
        }
    }
}
