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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface CrestContext {
    /**
     * @return continue the execution of the command.
     */
    Object proceed();

    /**
     * @return get the command method.
     */
    Method getMethod();

    /**
     * @return get the <b>mutable</b> list of command parameters.
     */
    List<Object> getParameters();

    /**
     * @return the command name.
     */
    String getName();

    /**
     * @return the metadata about parameters.
     */
    List<ParameterMetadata> getParameterMetadata();

    /**
     * The <b>mutable</b> option values of this command invocation, keyed by
     * option name: every option declared by the command method or by any
     * interceptor bound to it, whether or not a value was supplied.
     *
     * Where the command method declares an option, the entry is a live view
     * over the same storage as {@link #getParameters()} — a write through
     * either is seen through both.  Replacing a value is seen by every
     * interceptor later in the chain and by the command itself.  Values are
     * type checked against the option's declared type on put.
     *
     * @return the option values of this invocation, keyed by option name.
     */
    default Map<String, Object> getOptions() {
        return Collections.emptyMap();
    }
}
