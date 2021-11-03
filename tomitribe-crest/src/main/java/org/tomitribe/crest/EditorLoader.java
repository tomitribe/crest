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

package org.tomitribe.crest;

import org.apache.xbean.finder.Annotated;
import org.apache.xbean.finder.AnnotationFinder;
import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClasspathArchive;
import org.apache.xbean.finder.archive.CompositeArchive;
import org.tomitribe.crest.api.Editor;
import org.tomitribe.util.JarLocation;

import java.beans.PropertyEditorManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.ServiceLoader;

import static java.util.Arrays.asList;

public class EditorLoader {

    private EditorLoader() {
        // no-op
    }

    static {
        Lazy.init();
    }

    public static void load() {
        // no-op
    }

    private static Archive thisArchive() {
        try {
            final Class<?> reference = EditorLoader.class;

            final File file = JarLocation.jarLocation(reference);
            return ClasspathArchive.archive(reference.getClassLoader(), file.toURI().toURL());
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Archive cpArchive() {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            return new ClasspathArchive(
                    classLoader,
                    new UrlSet(classLoader).excludeJvm().exclude(classLoader.getParent()).getUrls());
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    // just allow lazy loading and avoid issues if XBean is not here
    private static class XBeanLoader {
        public void load() {
            final AnnotationFinder finder = new AnnotationFinder(new CompositeArchive(thisArchive(), cpArchive())).enableFindSubclasses();
            for (final Annotated<Class<?>> clazz : finder.findMetaAnnotatedClasses(Editor.class)) {
                PropertyEditorManager.registerEditor(clazz.getAnnotation(Editor.class).value(), clazz.get());
            }
        }
    }

    public static class Lazy {
        private Lazy() {
            // no-op
        }

        public static void init() {
            try {
                new XBeanLoader().load();
            } catch (final Throwable skip) {
                // no-op
            }
            final ClassLoader loader = Thread.currentThread().getContextClassLoader();
            for (final Editor editor : ServiceLoader.load(Editor.class)) { // class MyEditor extends AbstractConverter
                try {
                    PropertyEditorManager.registerEditor(editor.value(), editor.getClass());
                } catch (final Exception e) {
                    // no-op
                }
            }
            lightInit(loader);
        }

        public static void lightInit(final ClassLoader loader) {
            for (final String prefix : asList("", "/")) {
                try {
                    final Enumeration<URL> urls = loader.getResources(prefix + "crest-editors.txt");
                    final boolean done = urls.hasMoreElements();
                    while (urls.hasMoreElements()) {
                        final URL url = urls.nextElement();
                        try (final InputStream stream = url.openStream();
                             final BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.trim().isEmpty()) {
                                    continue;
                                }
                                try {
                                    final Class<?> clazz = loader.loadClass(line);
                                    final Editor target = clazz.getAnnotation(Editor.class);
                                    if (target != null) {
                                        PropertyEditorManager.registerEditor(target.value(), clazz);
                                    }
                                } catch (final Exception e) {
                                    // no-op
                                }
                            }
                        } catch (final IOException ioe) {
                            // no-op
                        }
                    }
                    if (done) {
                        break;
                    }
                } catch (final IOException e) {
                    // no-op
                }
            }
        }
    }
}
