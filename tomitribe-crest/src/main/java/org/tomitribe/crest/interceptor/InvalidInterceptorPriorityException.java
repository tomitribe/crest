/*
 * Copyright 2026 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.interceptor;

public class InvalidInterceptorPriorityException extends IllegalArgumentException {
    public InvalidInterceptorPriorityException(final Class<?> clazz, final double value) {
        super(String.format("Interceptor %s declares @Priority(%s) which is outside the valid range:" +
                " greater than 0 and less than 11.  Choose a decimal between the two, e.g. @Priority(5)." +
                "  The endpoints are excluded so there is always room to slot in:" +
                " to run before an interceptor at 1 use @Priority(0.9)," +
                " to run after one at 10 use @Priority(10.1)", clazz.getName(), value));
    }
}
