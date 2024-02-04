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
package org.tomitribe.crest.compiler;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;

public final class SimpleCompiler {
    private SimpleCompiler() {
        // no-op
    }

    public static MemoryLoader compile(final String file) {
        final Path path = Paths.get(file);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("No source file at '" + path.toAbsolutePath().normalize() + "'");
        }

        final Locale locale = Locale.getDefault();
        final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        final DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
        final MemoryLoader loader = new MemoryLoader();
        try {
            final JavaFileObject from = new StringInputJavaFileObject(
                    path.toAbsolutePath().normalize().toUri(),
                    new String(Files.readAllBytes(path)).replace("//-- ", ""));
            final JavaCompiler.CompilationTask task = compiler.getTask(
                    null,
                    new ForwardingJavaFileManager<JavaFileManager>(compiler.getStandardFileManager(null, locale, UTF_8)) {
                        @Override
                        public JavaFileObject getJavaFileForOutput(final Location location, final String className,
                                                                   final JavaFileObject.Kind kind, final FileObject sibling) {
                            final ClassOutputJavaFileObject clazz = new ClassOutputJavaFileObject(URI.create("class://" + className + ".class"), className);
                            loader.registry.put(className, clazz.bytecode::toByteArray);
                            return clazz;
                        }
                    },
                    collector, null, null,
                    singleton(from));

            boolean success = task.call();
            final List<Diagnostic<? extends JavaFileObject>> diagnostics = collector.getDiagnostics();
            if (!diagnostics.isEmpty()) {
                final Logger logger = Logger.getLogger(SimpleCompiler.class.getName());
                success = diagnostics.stream()
                        .peek(d -> logMessage(locale, logger, d))
                        .reduce(success, (c, it) -> it.getKind() != Diagnostic.Kind.ERROR && c, (a, b) -> a && b);
            }

            if (!success) {
                throw new IllegalStateException("Invalid compilation of '" + path.toAbsolutePath().normalize() + "'");
            }
            return loader;
        } catch (final IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    private static void logMessage(final Locale locale, final Logger logger, Diagnostic<? extends JavaFileObject> d) {
        final String message = "" +
                "[" + (d.getSource() == null ? "-" : d.getSource().toUri()) + "]" +
                "[" + d.getLineNumber() + ", " + d.getColumnNumber() + "] " +
                d.getMessage(locale);
        switch (d.getKind()) {
            case ERROR:
                logger.severe(message);
                break;

            // keep in mind it stays a cli so we don't want these errors generally
            case WARNING:
            case MANDATORY_WARNING: // it is generally ok to ignore them
                logger.fine(message);
                break;
            default: // more than ok to ignore
                logger.finest(message);
        }
    }

    public static class MemoryLoader extends ClassLoader {
        private final Map<String, Supplier<byte[]>> registry = new HashMap<>();

        @Override
        protected Class<?> findClass(final String name) throws ClassNotFoundException {
            final Supplier<byte[]> bytes = registry.get(name);
            if (bytes != null) {
                final byte[] content = bytes.get();
                return defineClass(name, content, 0, content.length);
            }
            return super.findClass(name);
        }

        public Stream<Class<?>> ownedClasses() {
            return registry.keySet().stream().map(name -> {
                try {
                    return loadClass(name);
                } catch (final ClassNotFoundException e) {
                    throw new IllegalArgumentException(e);
                }
            });
        }
    }

    private static class ClassOutputJavaFileObject extends SimpleJavaFileObject {
        private final ByteArrayOutputStream bytecode = new ByteArrayOutputStream();
        private final String className;

        private ClassOutputJavaFileObject(final URI uri, final String className) {
            super(uri, Kind.CLASS);
            this.className = className;
        }

        @Override
        public OutputStream openOutputStream() {
            return bytecode;
        }
    }

    private static class StringInputJavaFileObject extends SimpleJavaFileObject {
        private final String source;

        private StringInputJavaFileObject(final URI uri, String content) {
            super(uri, Kind.SOURCE);
            this.source = content;
        }

        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return source;
        }
    }
}
