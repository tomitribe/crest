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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.Attributes.Name;

import static org.tomitribe.crest.Manifest.Entry.BUILD_JDK_SPEC;
import static org.tomitribe.crest.Manifest.Entry.COMMAND_NAME;
import static org.tomitribe.crest.Manifest.Entry.COMMAND_VERSION;
import static org.tomitribe.crest.Manifest.Entry.CREATED_BY;
import static org.tomitribe.crest.Manifest.Entry.IMPLEMENTATION_TITLE;
import static org.tomitribe.crest.Manifest.Entry.IMPLEMENTATION_VENDOR;
import static org.tomitribe.crest.Manifest.Entry.IMPLEMENTATION_VERSION;
import static org.tomitribe.crest.Manifest.Entry.MAIN_CLASS;
import static org.tomitribe.crest.Manifest.Entry.MANIFEST_VERSION;
import static org.tomitribe.crest.Manifest.Entry.SPECIFICATION_TITLE;
import static org.tomitribe.crest.Manifest.Entry.SPECIFICATION_VENDOR;
import static org.tomitribe.crest.Manifest.Entry.SPECIFICATION_VERSION;

public class Manifest {
    private final java.util.jar.Manifest manifest;

    private Manifest(java.util.jar.Manifest manifest) {
        this.manifest = manifest;
    }

    public String getManifestVersion() {
        return get(MANIFEST_VERSION);
    }

    public String getCreatedBy() {
        return get(CREATED_BY);
    }

    public String getBuildJdkSpec() {
        return get(BUILD_JDK_SPEC);
    }

    public String getSpecificationTitle() {
        return get(SPECIFICATION_TITLE);
    }

    public String getSpecificationVersion() {
        return get(SPECIFICATION_VERSION);
    }

    public String getSpecificationVendor() {
        return get(SPECIFICATION_VENDOR);
    }

    public String getImplementationTitle() {
        return get(IMPLEMENTATION_TITLE);
    }

    public String getImplementationVersion() {
        return get(IMPLEMENTATION_VERSION);
    }

    public String getImplementationVendor() {
        return get(IMPLEMENTATION_VENDOR);
    }

    public String getMainClass() {
        return get(MAIN_CLASS);
    }

    public String getCommandName() {
        return get(COMMAND_NAME);
    }

    public String getCommandVersion() {
        return get(COMMAND_VERSION);
    }

    public String get(final Entry entry) {
        return this.manifest.getMainAttributes().getValue(entry.toName());
    }

    public static List<Manifest> read() {
        return read(Thread.currentThread().getContextClassLoader());
    }

    public static List<Manifest> read(final ClassLoader classLoader) {
        final List<Manifest> manifests = new ArrayList<>();
        try {
            final Enumeration<URL> resources = classLoader.getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                final Manifest manifest = read(url);
                manifests.add(manifest);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read MANIFEST.MF entries from classpath", e);
        }
        return manifests;
    }

    public static Manifest read(final URL url) {
        try {
            final InputStream is = url.openStream();
            final java.util.jar.Manifest javaManifest = new java.util.jar.Manifest(is);
            return new Manifest(javaManifest);
        } catch (final IOException e) {
            throw new ManifestUrlReadException(url, e);
        }
    }

    public String write() {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            manifest.write(baos);
            return baos.toString("UTF-8");
        } catch (final IOException e) {
            throw new ManifestWriteException(e);
        }

    }

    @Override
    public String toString() {

        final String mainClass = getMainClass();
        final String implementationVendor = getImplementationVendor();
        final String implementationVersion = getImplementationVersion();
        final String commandName = getCommandName();
        final String commandVersion = getCommandVersion();


        return "Manifest{" +
                "mainClass='" + mainClass + '\'' +
                ", implementationVendor='" + implementationVendor + '\'' +
                ", implementationVersion='" + implementationVersion + '\'' +
                ", commandName='" + commandName + '\'' +
                ", commandVersion='" + commandVersion + '\'' +
                '}';
    }

    public static Optional<Manifest> get() {
        final String mainClassName = findMainClassName();
        if (mainClassName == null) return Optional.empty();

        return read().stream()
                .filter(manifest -> manifest.getMainClass() != null)
                .filter(manifest -> manifest.getMainClass().equals(mainClassName))
                .findFirst();
    }

    public static String findMainClassName() {
        final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        /*
         *  Reverse iteration to find the earliest 'main' method in the call stack
         */
        for (int i = stackTrace.length - 1; i >= 0; i--) {
            final StackTraceElement ste = stackTrace[i];
            if ("main".equals(ste.getMethodName())) {
                return ste.getClassName();
            }
        }

        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final java.util.jar.Manifest javaManifest = new java.util.jar.Manifest(new java.util.jar.Manifest());

        public Builder() {
            manifestVersion("1.0");
        }

        public Builder manifestVersion(String manifestVersion) {
            put(MANIFEST_VERSION, manifestVersion);
            return this;
        }

        public Builder createdBy(String createdBy) {
            put(CREATED_BY, createdBy);
            return this;
        }

        public Builder buildJdkSpec(String buildJdkSpec) {
            put(BUILD_JDK_SPEC, buildJdkSpec);
            return this;
        }

        public Builder specificationTitle(String specificationTitle) {
            put(SPECIFICATION_TITLE, specificationTitle);
            return this;
        }

        public Builder specificationVersion(String specificationVersion) {
            put(SPECIFICATION_VERSION, specificationVersion);
            return this;
        }

        public Builder specificationVendor(String specificationVendor) {
            put(SPECIFICATION_VENDOR, specificationVendor);
            return this;
        }

        public Builder implementationTitle(String implementationTitle) {
            put(IMPLEMENTATION_TITLE, implementationTitle);
            return this;
        }

        public Builder implementationVersion(String implementationVersion) {
            put(IMPLEMENTATION_VERSION, implementationVersion);
            return this;
        }

        public Builder implementationVendor(String implementationVendor) {
            put(IMPLEMENTATION_VENDOR, implementationVendor);
            return this;
        }

        public Builder mainClass(final Class<?> mainClass) {
            put(MAIN_CLASS, mainClass.getName());
            return this;
        }

        public Builder mainClass(String mainClass) {
            put(MAIN_CLASS, mainClass);
            return this;
        }

        public Builder commandName(String commandName) {
            put(COMMAND_NAME, commandName);
            return this;
        }

        public Builder commandVersion(String commandVersion) {
            put(COMMAND_VERSION, commandVersion);
            return this;
        }

        private Object put(final Entry name, final String value) {
            return this.javaManifest.getMainAttributes().put(name.toName(), value);
        }

        public Manifest build() {
            return new Manifest(this.javaManifest);
        }
    }


    // Enum for manifest entry keys
    public enum Entry {
        MANIFEST_VERSION("Manifest-Version"),
        COMMAND_NAME("Command-Name"),
        COMMAND_VERSION("Command-Version"),
        CREATED_BY("Created-By"),
        BUILD_JDK_SPEC("Build-Jdk-Spec"),
        SPECIFICATION_TITLE("Specification-Title"),
        SPECIFICATION_VERSION("Specification-Version"),
        SPECIFICATION_VENDOR("Specification-Vendor"),
        IMPLEMENTATION_TITLE("Implementation-Title"),
        IMPLEMENTATION_VERSION("Implementation-Version"),
        IMPLEMENTATION_VENDOR("Implementation-Vendor"),
        MAIN_CLASS("Main-Class");

        private final String value;

        Entry(String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        public Name toName() {
            return new Name(value);
        }
    }

    public static class ManifestUrlReadException extends RuntimeException {
        public ManifestUrlReadException(final URL url, final Throwable e) {
            super("Unable to read MANIFEST.MF file " + url, e);
        }
    }

    public static class ManifestWriteException extends RuntimeException {
        public ManifestWriteException(final Throwable e) {
            super("Unable to format manifest instance as string", e);
        }
    }
}
