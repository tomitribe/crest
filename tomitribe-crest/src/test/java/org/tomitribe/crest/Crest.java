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

import org.apache.bval.jsr.ApacheValidationProvider;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.test.Jar;
import org.tomitribe.util.Join;

import javax.validation.Validation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight uber-jar builder to allow testing of command ouput
 * exactly from the user's perspective, including exit codes.
 */
public class Crest extends Jar<Crest> {

    private final List<Class<?>> commands = new ArrayList<Class<?>>();

    public static Crest jar() {
        return new Crest()
                .addJar(Main.class)
                .addJar(Command.class)
                .addJar(Join.class)
                .addJar(Validation.class)
                .addJar(ApacheValidationProvider.class)
                .manifest("Main-Class", Main.class);
    }

    public Crest command(final Class<?> command) {
        commands.add(command);
        return add(command);
    }

    @Override
    public File toJar(final File file) throws IOException {
        add("crest-commands.txt", Join.join("\n", commands) + "\n");
        return super.toJar(file);
    }
}
