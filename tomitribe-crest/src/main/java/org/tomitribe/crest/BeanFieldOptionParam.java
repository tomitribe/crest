/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest;

import org.tomitribe.crest.api.OptionBean;
import org.tomitribe.util.reflect.Parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class BeanFieldOptionParam extends OptionParam {
    private static final Annotation OPTION_BEAN_ANNOTATION = Annotation.class.cast(
            Proxy.newProxyInstance(BeanFieldOptionParam.class.getClassLoader(),
            new Class<?>[] { Annotation.class, OptionBean.class }, new InvocationHandler() {
                @Override
                public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
                    if ("annotationType".equals(method.getName())) {
                        return OptionBean.class;
                    }
                    if (Annotation.class.equals(method.getDeclaringClass())) {
                        return Object.class.getMethod(method.getName(), method.getParameterTypes()).invoke(this, args);
                    }
                    return null;
                }
            }));

    private final Field field;
    private final Object instance;

    public BeanFieldOptionParam(final Parameter parameter, final Field field, final Object instance) {
        super(parameter);
        this.field = field;
        this.instance = instance;
    }

    @Override
    public void bind(final Object value) {
        if (!field.isAccessible()) {
            field.setAccessible(true);
        }

        try {
            field.set(instance, value);
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public Object getBoundValue() {
        return instance;
    }

    @Override
    public <T extends Annotation> T getAnnotation(final Class<T> annotationClass) {
        if (OptionBean.class.equals(annotationClass)) {
            return annotationClass.cast(OPTION_BEAN_ANNOTATION);
        }
        return super.getAnnotation(annotationClass);
    }

    @Override
    public String toString() {
        return "BeanField" + super.toString();
    }
}
