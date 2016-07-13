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
package org.tomitribe.crest.cli.impl.command;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Out;
import org.tomitribe.util.IO;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.regex.Pattern;

import static java.lang.System.lineSeparator;
import static java.util.Objects.requireNonNull;

public class Streams {
    @Command
    public static void jgrep(final String pattern,
                             @Option("regex") @Default("false") final boolean isRegex,
                             @Option("i") @Default("true") final boolean insensitive,
                             @In final InputStream in,
                             @Out final PrintStream out) {
        if (pattern == null) {
            throw new NullPointerException("Pattern shouldnt be null");
        }
        final Collection<Predicate<String>> predicates = new ArrayList<>();
        for (final String patt : pattern.split("\\|")) {
            predicates.add(isRegex ?
                new Predicate<String>() {
                    private final Pattern pattern = Pattern.compile(patt); // costly so cache it for the method scope

                    @Override
                    public boolean test(final String s) {
                        return pattern.matcher(s).matches();
                    }
                } :
                new Predicate<String>() {
                    @Override
                    public boolean test(final String s) {
                        return insensitive ? s.toLowerCase(Locale.ENGLISH).contains(patt.toLowerCase(Locale.ENGLISH)) : s.contains(patt);
                    }
                });
        }

         try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                for (final Predicate<String> p : predicates) {
                    if (p.test(line)) {
                        out.println(line);
                        break;
                    }
                }
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Command
    public static void wc(@Option("l") @Default("true") final boolean line,
                          @Option("c") @Default("false") final boolean characters,
                          @In final InputStream in,
                          @Out final PrintStream out) {
        int count = 0;
        if (characters) {
            try (Reader reader = new InputStreamReader(in)) {
                while (reader.read() >= 0) {
                    count++;
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        } else if (line) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                while (reader.readLine() != null) {
                    count++;
                }
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalStateException("wc needs at least one active option");
        }
        out.println(count);
    }

    @Command
    public static void jsed(final String command,
                            @In final InputStream in,
                            @Out final PrintStream out) {
        if (!requireNonNull(command, "Please provide an option like: jsed s/foo/bar/g").startsWith("s/")) {
            throw new IllegalArgumentException("Only substitution commands supported.");
        }

        final int slash1 = command.indexOf('/');
        final int slash2 = command.indexOf('/', slash1 + 1);
        final int slash3 = command.indexOf('/', slash2 + 1);
        if (slash1 < 0 || slash2 < 0 || slash3 < 0) {
            throw new IllegalArgumentException("Wrong pattern '" + command + "', use s/pattern/replacement/[g]");
        }

        final String pattern = command.substring(slash1 + 1, slash2);
        final String substitution = command.substring(slash2 + 1, slash3);
        final boolean global = command.endsWith("/g");
        final Pattern compiled = Pattern.compile(pattern);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                out.println(global ? compiled.matcher(line).replaceAll(substitution) : compiled.matcher(line).replaceFirst(substitution));
            }
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Command
    public static void pretty(@In final InputStream in,
                              @Out final PrintStream out) throws IOException {
        final String content = IO.slurp(in);  // note: optimizing it to not load in memory the content is good but then optiomise http cmd as well
        if (content.startsWith("{") || content.startsWith("[")) {
            JSonP.format(content, out);
        } else { // xml?
            try {
                final TransformerFactory factory = TransformerFactory.newInstance();
                final Transformer transformer = factory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                final StreamResult result = new StreamResult(new StringWriter());
                transformer.transform(new StreamSource(new StringReader(content)), result);
                out.write(result.getWriter().toString().getBytes("UTF-8"));
            } catch (final TransformerException e) {
                out.write(content.getBytes("UTF-8"));
            }
        }
        out.write(lineSeparator().getBytes());

        try {
            in.close();
        } catch (final Exception e) {
            // no-op
        }
    }

    private Streams() {
        // no-op
    }

    // TODO: use j8 when upgraded
    interface Predicate<T> {
        boolean test(T t);
    }
}
