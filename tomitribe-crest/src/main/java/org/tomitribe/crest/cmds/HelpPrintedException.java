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
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.api.Exit;

/**
 * We have a slightly unintuitive error handling scheme (my bad)
 * that prints help early and then suppresses printing error
 * messages and stacktraces in exception handling later in the call
 * stack.  The intention is that the message has already been printed
 * along with full help output, so don't print the message again later.
 *
 * Unfortunately, it results in some valid exceptions never being printed
 * and Crest silently failing with no help or output of any kind.
 *
 * The creation of this class is an attempt to make it explicit when we
 * have in fact printed help eagerly.
 *
 * The exception type that implied this before was IllegalStateException,
 * which is unfortunately very common in the vm and was getting thrown
 * outside our handling and suppressed.
 */
@Exit(10)
public class HelpPrintedException extends IllegalArgumentException {
    public HelpPrintedException(final Throwable throwable) {
        super(throwable);
    }
}
