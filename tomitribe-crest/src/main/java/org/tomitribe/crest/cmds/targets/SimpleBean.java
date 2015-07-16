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
package org.tomitribe.crest.cmds.targets;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SimpleBean implements Target {
    private final Object bean;

    public SimpleBean(final Object bean) {
        this.bean = bean;
    }

    @Override
    public Object invoke(final Method method, final Object... args) throws InvocationTargetException, IllegalAccessException {
        final Object bean = getBean(method);
        return method.invoke(bean, args);
    }

    private Object getBean(final Method method) {
        if (bean != null) {
            return bean;
        }
        if (Modifier.isStatic(method.getModifiers())) {
            return null;
        }

        try {
            final Class<?> declaringClass = method.getDeclaringClass();
            final Constructor<?> constructor = declaringClass.getConstructor();
            return constructor.newInstance();
        } catch (final NoSuchMethodException e) {
            return null;
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        } catch (final Throwable e) {
            throw new IllegalStateException(e);
        }
    }
}
