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

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A crest interceptor is defined by this annotation at method level.
 * The signature needs to be public Object &lt;name&gt;(CrestContext ctx);
 */
@Retention(RUNTIME)
@Target({METHOD, ANNOTATION_TYPE})
public @interface CrestInterceptor {
    /**
     * When used on an annotation, allows the interceptor class to be supplied.  This
     * is an alternative to using @Command(interceptedBy) to supply the interceptor
     * class.
     */
    Class<?> value() default Object.class;
}
