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
package org.tomitribe.crest.cmds;

import org.tomitribe.crest.api.Exit;
import org.tomitribe.crest.cmds.processors.Help;
import org.tomitribe.crest.cmds.processors.OptionParam;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.interceptor.internal.InternalInterceptor;
import org.tomitribe.util.Join;
import org.tomitribe.util.PrintString;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class OverloadedCmdMethod implements Cmd {

    private final String name;
    private final List<CmdMethod> methods = new ArrayList<>();

    public OverloadedCmdMethod(final String name) {
        this.name = name;
    }

    public List<CmdMethod> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @Override
    public String getUsage() {
        final StringBuilder sb = new StringBuilder();
        for (final CmdMethod method : methods) {
            sb.append(method.getUsage()).append('\n');
        }
        return sb.toString().trim();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Object exec(final Map<Class<?>, InternalInterceptor> globalInterceptors, final String... rawArgs) {

        final Iterator<CmdMethod> iterator = methods.iterator();

        while (iterator.hasNext()) {
            final CmdMethod method = iterator.next();

            final List<Object> args;
            try {

                args = method.parse(rawArgs);

            } catch (final Exception e) {
                if (iterator.hasNext()) {

                    continue;

                } else {

                    /*
                     * If any exception in the chain was annotated with @Exit
                     * then we want to let that exception handle the error message
                     */
                    final RuntimeException handled = CmdMethod.getExitCode(e);
                    if (handled != null) {
                        final Exit exit = handled.getClass().getAnnotation(Exit.class);
                        if (exit.help()) {
                            reportWithHelp(e);
                        }
                        throw handled;
                    }
                    reportWithHelp(e);

                    throw CmdMethod.toRuntimeException(e);

                }
            }

            return method.exec(globalInterceptors, args);

        }

        throw new IllegalStateException(String.format("Unable to find matching method for command: %s", Join.join(" "
                + "", rawArgs)));
    }

    private void reportWithHelp(final Throwable e) {
        final PrintStream err = Environment.ENVIRONMENT_THREAD_LOCAL.get().getError();
        err.println(e.getMessage());

        help(err);

        throw new HelpPrintedException(e);
    }


    @Override
    public void help(final PrintStream out) {
        if (methods.isEmpty()) {
            throw new IllegalStateException("No method in group: " + name);
        }

        out.println();
        { // usage
            final Iterator<CmdMethod> it = methods.iterator();

            int i = 1;
            out.printf("Usage: %s%n", it.next().getUsage().replace("[options]", "[options" + i + "]"));
            while (it.hasNext()) {
                i++;
                out.printf("       %s%n", it.next().getUsage().replace("[options]", "[options" + i + "]"));
            }
        }


        {
            int i = 1;
            for (final CmdMethod method : methods) {
                out.println();
                final Map<String, OptionParam> optionParameters = method.getOptionParameters();
                final PrintString help = new PrintString();
                Help.optionHelp(method.getMethod(), getName(), optionParameters.values(), help, false);
                out.print(help.toString().replace("Options:", "Options" + i + ":"));
                i++;
            }
        }

        Help.printNameAndVersion(out);
    }

    public void add(final CmdMethod cmd) {
        Comparator<CmdMethod> cmdMethodComparator = Comparator
                // First compare by argument length (More arguments first)
                .comparingInt((CmdMethod c) -> c.getArgumentParameters().size()).reversed()
                // Second compare by option length (More options first)
                .thenComparingInt((CmdMethod c) -> c.getOptionParameters().size()).reversed()
                // Third compare by concatenated option keys (Lexicographically)
                .thenComparing(c -> c.getOptionParameters().keySet().stream()
                        .reduce((s, s2) -> s + "\000" + s2)
                        .orElse(""));

        // Add and sort the list
        methods.add(cmd);
        methods.sort(cmdMethodComparator);
    }

    @Override
    public Collection<String> complete(String buffer, int cursorPosition) {
        throw new UnsupportedOperationException();
    }
}
