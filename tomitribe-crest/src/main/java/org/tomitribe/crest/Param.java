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

import org.tomitribe.util.reflect.Generics;
import org.tomitribe.util.reflect.Parameter;

import java.util.Collection;

public class Param extends Parameter {

    public Param(Parameter parameter) {
        super(parameter.getAnnotations(), parameter.getType(), parameter.getGenericType());
    }

    public String getDisplayType() {
        if (isListable()) {

            return getListableType().getSimpleName() + "[]";
        }

        return getType().getSimpleName();
    }

    public boolean isListable() {
        final Class<?> type = getType();

        return Collection.class.isAssignableFrom(type) || type.isArray();
    }

    public Class getListableType() {

        if (getType().isArray()) {

            return getType().getComponentType();

        } else {

            return (Class<?>) Generics.getType(this);
        }
    }
}
