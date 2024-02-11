/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Loader;
import org.tomitribe.util.Archive;
import org.tomitribe.util.JarLocation;
import org.tomitribe.util.editor.Converter;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class Cli {

    private final File jar;

    public Cli(final File jar) {
        this.jar = jar;
    }

    public Java.Result run(final String... args) throws Exception {
        return Java.jar(jar, args);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Archive archive = Archive.archive();

        public Builder() {
            final List<Class<?>> required = Arrays.asList(Main.class, Command.class, Converter.class);

            for (final Class<?> clazz : required) {
                final File file = JarLocation.jarLocation(clazz);
                if (file.isDirectory()) {
                    archive.addDir(file);
                } else {
                    archive.addJar(file);
                }
            }

            // Overwrite any Manifest entries from the above jars
            final Manifest manifest = Manifest.builder().build();
            manifest(manifest);
        }

        public Builder manifest(final Manifest manifest) {
            archive.add("META-INF/MANIFEST.MF", manifest.write());
            return this;
        }

        public Builder manifest(final Manifest.Builder manifest) {
            return manifest(manifest.build());
        }

        public Builder loader(final Class<? extends Loader> loader) {
            archive.add("META-INF/services/" + Loader.class.getName(), CommandNameAndVersionTest.ColorLoader.class.getName());
            archive.add(loader);
            return this;
        }

        public Builder add(final java.lang.String name, final byte[] bytes) {
            archive.add(name, bytes);
            return this;
        }

        public Builder add(final java.lang.String name, final Supplier<byte[]> content) {
            archive.add(name, content);
            return this;
        }

        public Builder add(final java.lang.String name, final java.lang.String content) {
            archive.add(name, content);
            return this;
        }

        public Builder add(final java.lang.String name, final File content) {
            archive.add(name, content);
            return this;
        }

        public Builder add(final java.lang.String name, final Archive archive) {
            this.archive.add(name, archive);
            return this;
        }

        public Builder add(final java.lang.String name, final URL content) throws IOException {
            archive.add(name, content);
            return this;
        }

        public Builder add(final Class<?> clazz) {
            archive.add(clazz);
            return this;
        }

        public Cli build() {
            return new Cli(archive.asJar());
        }
    }
}
