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

import org.tomitribe.crest.util.Join;

import java.io.PrintStream;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class CmdGroup implements Cmd {

    private final String name;
    private final Set<CmdMethod> methods;

    public CmdGroup(String name) {
        this.name = name;

        this.methods = new TreeSet<CmdMethod>(new Comparator<CmdMethod>() {
            @Override
            public int compare(CmdMethod a, CmdMethod b) {
                return a.getArgumentParameters().size() - b.getArgumentParameters().size();
            }
        });
    }

    @Override
    public String getUsage() {
        final StringBuilder sb = new StringBuilder();
        for (CmdMethod method : methods) {
            sb.append(method.getUsage()).append('\n');
        }
        return sb.toString().trim();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object exec(String... rawArgs) {

        final Iterator<CmdMethod> iterator = methods.iterator();

        while (iterator.hasNext()) {
            final CmdMethod method = iterator.next();

            final List<Object> args;
            try {

                args = method.parse(rawArgs);

            } catch (Exception e) {
                if (iterator.hasNext()) {

                    continue;

                } else {

                    throw CmdMethod.toRuntimeException(e);

                }
            }

            return method.exec(args);

        }

        throw new IllegalStateException(String.format("Unable to find matching method for command: %s", Join.join(" ", rawArgs)));
    }

    @Override
    public void help(PrintStream out) {
        if (methods.size() == 0) throw new IllegalStateException("No method in group: " + name);

        out.println();
        { // usage
            final Iterator<CmdMethod> it = methods.iterator();

            out.printf("Usage: %s%n", it.next().getUsage());
            while (it.hasNext()) {
                out.printf("       %s%n", it.next().getUsage());
            }
        }
        out.println();

        final Map<String, CmdMethod.OptionParameter> options = new TreeMap<String, CmdMethod.OptionParameter>();
        for (CmdMethod method : methods) {
            options.putAll(method.getOptionParameters());
        }

        Help.optionHelp(Help.class, getName(), options.values(), out);
    }

    public void add(CmdMethod cmd) {
        methods.add(cmd);
    }
}
