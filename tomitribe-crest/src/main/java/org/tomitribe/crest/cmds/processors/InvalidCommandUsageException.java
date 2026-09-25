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
package org.tomitribe.crest.cmds.processors;

import java.util.Collections;
import java.util.List;

/**
 * Thrown when a class carries {@code @Command} in a way the runtime would
 * otherwise silently ignore.  Every offending use is collected and reported
 * together so a single failure names all of them.
 */
public class InvalidCommandUsageException extends IllegalArgumentException {

    private final Class<?> clazz;
    private final List<String> violations;

    public InvalidCommandUsageException(final Class<?> clazz, final List<String> violations) {
        super(message(clazz, violations));
        this.clazz = clazz;
        this.violations = Collections.unmodifiableList(violations);
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public List<String> getViolations() {
        return violations;
    }

    private static String message(final Class<?> clazz, final List<String> violations) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Invalid @Command usage on class ").append(clazz.getName()).append(":\n");
        for (final String violation : violations) {
            sb.append("  - ").append(violation).append('\n');
        }
        sb.append("@Command methods must be public and concrete.  Fix each method, ")
                .append("move the annotation to the implementing method, or remove it.");
        return sb.toString();
    }
}
