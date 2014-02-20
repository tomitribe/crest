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

import java.lang.reflect.Method;

/**
 * Used to provide substitution values for @Default("hello ${value}") type expressions.
 */
public interface DefaultsContext {
    /**
     * Implementations of DefaultsContext need to implement this method to convert
     * an expression inside ${...} type substitutions to String values which will
     * replace the substitution expression.
     *
     * @param target        The command class
     * @param commandMethod the command method
     * @param key           the ... in the ${...} expression
     * @return the value to replace key with. Null is equivalent to the empty string.
     */
    public String find(Target target, Method commandMethod, String key);
}
