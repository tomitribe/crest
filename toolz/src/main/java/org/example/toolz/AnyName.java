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
package org.example.toolz;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.io.File;
import java.net.URI;
import java.util.regex.Pattern;

public class AnyName {

    @Command
    public void rsync(@Option("recursive") final boolean recursive,
                      @Option("links") final boolean links,
                      @Option("perms") final boolean perms,
                      @Option("owner") final boolean owner,
                      @Option("group") final boolean group,
                      @Option("devices") final boolean devices,
                      @Option("specials") final boolean specials,
                      @Option("times") final boolean times,
                      @Option("exclude") final Pattern exclude,
                      @Option("exclude-from") final File excludeFrom,
                      @Option("include") final Pattern include,
                      @Option("include-from") final File includeFrom,
                      @Option("progress") @Default("true") final boolean progress,
                      final URI[] sources,
                      final URI dest) {

        // TODO write the implementation...
    }
}
