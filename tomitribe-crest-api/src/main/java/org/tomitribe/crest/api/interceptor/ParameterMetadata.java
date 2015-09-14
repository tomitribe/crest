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
package org.tomitribe.crest.api.interceptor;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Describe basic metadata about a crest parameter.
 */
public interface ParameterMetadata {
    enum ParamType {
        /**
         * {@see Environment} or stdout, stderr, stdin.
         */
        INTERNAL,

        /**
         * type matches the service registry of the {@see Environment}.
         */
        SERVICE,

        /**
         * a parameter without {@see org.tomitribe.crest.api.Option}.
         */
        PLAIN,

        /**
         * a parameter with an option, ie a name.
         */
        OPTION,

        /**
         * a bean option parameter.
         */
        BEAN_OPTION
    }

    /**
     * @return the parameter type.
     */
    ParamType getType();

    /**
     * @return the name of the parameter.
     */
    String getName();

    /**
     * @return the parameter type.
     */
    Type getReflectType();

    /**
     * @return true if the parameter is a collection or array.
     */
    boolean isListable();

    /**
     * @return the component type for a {@see isListable()} parameter, throw {@see java.lang.UnsupportedOperationException} otherwise.
     */
    Class<?> getComponentType();

    /**
     * @return the list of nested parameters if a complex parameter (BEAN_OPTION type) or null otherwise.
     */
    List<ParameterMetadata> getNested();
}
