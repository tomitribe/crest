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
package org.tomitribe.crest.api.interceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls the order in which interceptors bound to the same command
 * execute.  Interceptors run in ascending priority order: the lower the
 * value, the earlier (outermost) the interceptor runs.  An interceptor
 * with no @Priority runs at 5.  Interceptors with equal priority run in
 * the order they are declared on the command method.
 *
 * Think of the space as 1 to 10 with the ordinary interceptors in the
 * middle at 5.  The value is a double so a new interceptor can always be
 * slotted between two existing priorities without renumbering: between
 * 6 and 7 there is 6.5, between 6 and 6.5 there is 6.4, and so on.
 *
 * The valid range is greater than 0 and less than 11, exclusive on both
 * ends.  The endpoints are deliberately not allowed so no interceptor can
 * take the last spot and shut others out: if 1 is taken you can run
 * earlier with 0.9, if 0.9 is taken there is 0.8 — there is always room.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Priority {

    double value();
}
