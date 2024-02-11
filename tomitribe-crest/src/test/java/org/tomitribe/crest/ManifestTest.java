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

import org.junit.Test;
import org.tomitribe.util.Archive;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ManifestTest {

    @Test
    public void testManifestBuilderAndGetters() {
        // Create a manifest using the builder
        org.tomitribe.crest.Manifest manifest = new org.tomitribe.crest.Manifest.Builder()
                .manifestVersion("1.0")
                .createdBy("JUnit Test")
                .buildJdkSpec("11")
                .specificationTitle("Test Spec Title")
                .specificationVersion("1.0")
                .specificationVendor("Test Vendor")
                .implementationTitle("Test Impl Title")
                .implementationVersion("1.0-SNAPSHOT")
                .implementationVendor("Test Impl Vendor")
                .mainClass("org.example.Main")
                .commandName("com.example.Main")
                .commandVersion("6.7")
                .build();

        // Verify each getter returns the expected value
        assertEquals("Manifest-Version does not match.", "1.0", manifest.getManifestVersion());
        assertEquals("Created-By does not match.", "JUnit Test", manifest.getCreatedBy());
        assertEquals("Build-Jdk-Spec does not match.", "11", manifest.getBuildJdkSpec());
        assertEquals("Specification-Title does not match.", "Test Spec Title", manifest.getSpecificationTitle());
        assertEquals("Specification-Version does not match.", "1.0", manifest.getSpecificationVersion());
        assertEquals("Specification-Vendor does not match.", "Test Vendor", manifest.getSpecificationVendor());
        assertEquals("Implementation-Title does not match.", "Test Impl Title", manifest.getImplementationTitle());
        assertEquals("Implementation-Version does not match.", "1.0-SNAPSHOT", manifest.getImplementationVersion());
        assertEquals("Implementation-Vendor does not match.", "Test Impl Vendor", manifest.getImplementationVendor());
        assertEquals("Main-Class does not match.", "org.example.Main", manifest.getMainClass());
        assertEquals("Command-Name does not match.", "com.example.Main", manifest.getCommandName());
        assertEquals("Command-Version does not match.", "6.7", manifest.getCommandVersion());
    }


    @Test
    public void read() throws Exception {
        final ClassLoader classLoader = Archives.classLoader(
                Archive.archive().add("META-INF/MANIFEST.MF", Manifest.builder()
                        .manifestVersion("1.0")
                        .implementationVersion("12.0-SNAPSHOT")
                        .implementationVendor("Red")
                        .mainClass("org.example.Main")
                        .build().write()),
                Archive.archive().add("META-INF/MANIFEST.MF", Manifest.builder()
                        .manifestVersion("1.0")
                        .implementationVersion("5.0")
                        .implementationVendor("Green")
                        .mainClass(Main.class.getName())
                        .build().write()),
                Archive.archive().add("META-INF/MANIFEST.MF", Manifest.builder()
                        .manifestVersion("1.0")
                        .implementationVersion("1.0-SNAPSHOT")
                        .implementationVendor("Blue")
                        .mainClass("org.example.Main")
                        .build().write())
        );

        final List<Manifest> manifests = Manifest.read(classLoader);

        final List<Manifest> collect = manifests.stream()
                .filter(manifest -> manifest.getImplementationVendor() != null)
                .filter(manifest -> manifest.getImplementationVendor().matches("Red|Green|Blue"))
                .sorted(Comparator.comparing(Manifest::getImplementationVendor))
                .collect(Collectors.toList());

        assertEquals(3, collect.size());
    }

    @Test
    public void get() throws Exception {

        final List<File> urls = Arrays.asList(
                Archive.archive()
                        .add("META-INF/MANIFEST.MF", Manifest.builder()
                                .manifestVersion("1.0")
                                .implementationVersion("12.0-SNAPSHOT")
                                .implementationVendor("Red")
                                .mainClass(Main.class.getName()) // we should not just pick our Main
                                .build().write()).asJar(),
                Archive.archive()
                        .add(Correct.class)
                        .add(Incorrect.class)
                        .add(Manifest.class)
                        .add(ManifestTest.class)
                        .add(Manifest.Entry.class)
                        .add(Manifest.Builder.class)
                        .add(Manifest.ManifestWriteException.class)
                        .add(Manifest.ManifestUrlReadException.class)
                        .add("META-INF/MANIFEST.MF", Manifest.builder()
                                .manifestVersion("1.0")
                                .implementationVersion("5.0")
                                .implementationVendor("Green")
                                .commandVersion("4.5.6")
                                .commandName("forrest")
                                .mainClass(Correct.class)
                                .build().write()).asJar(),
                Archive.archive()
                        .add("META-INF/MANIFEST.MF", Manifest.builder()
                                .manifestVersion("1.0")
                                .implementationVersion("1.0-SNAPSHOT")
                                .implementationVendor("Blue")
                                .mainClass("org.example.Main")
                                .build().write()).asJar());

        final String classpath = urls.stream()
                .map(File::getAbsolutePath)
                .reduce((s, s2) -> s + File.pathSeparator + s2)
                .get();

        final Java.Result result = Java.java("-classpath", classpath, Correct.class.getName());

        assertEquals(String.format("" +
                "Main-Class: org.tomitribe.crest.ManifestTest$Correct%n" +
                "Implementation-Vendor: Green%n" +
                "Implementation-Version: 5.0%n" +
                "Command-Name: forrest%n" +
                "Command-Version: 4.5.6%n"), result.getOut());

    }

    @Test
    public void findMainClassName() throws IOException, ExecutionException, InterruptedException {
        final String manifest = Manifest.builder()
                .mainClass(Color.class)
                .implementationVersion("3.6.9")
                .build().write();
        final File jar = Archive.archive()
                .add("META-INF/MANIFEST.MF", manifest)
                .add(Thing.class)
                .add(Main.class)
                .add(Color.class)
                .add(Correct.class)
                .add(Incorrect.class)
                .add(ManifestTest.class)
                .add(Manifest.class)
                .add(Manifest.Entry.class)
                .add(Manifest.Builder.class)
                .add(Manifest.ManifestWriteException.class)
                .add(Manifest.ManifestUrlReadException.class)
                .asJar();

        final Java.Result result = Java.jar(jar);

        assertEquals("org.tomitribe.crest.ManifestTest$Color", result.getOut());
        assertEquals("org.tomitribe.crest.ManifestTest$Color", Color.class.getName());
    }

    public static class Thing {
        public void printMain() {
            System.out.print(Manifest.findMainClassName());
        }
    }

    /**
     * This is intended to try and trick the algorithm
     * The main method is not static so can't be the real main
     */
    public static class Main {
        public void main(String... args) {
            new Thing().printMain();
        }
    }

    public static class Color {
        public static void main(String[] args) {
            new Main().main(args);
        }
    }

    /* --- @Test get() ---------------------------------------------------------------------------- */

    public static class Correct {
        public static void main(String[] args) {
            Incorrect.main(args);
        }
    }

    public static class Incorrect {
        public static void main(String[] args) {
            final Manifest manifest = Manifest.get().orElse(null);
            if (manifest == null) {
                System.out.println("Manifest is null");
                return;
            }

            System.out.printf("%s: %s%n", "Main-Class", manifest.getMainClass());
            System.out.printf("%s: %s%n", "Implementation-Vendor", manifest.getImplementationVendor());
            System.out.printf("%s: %s%n", "Implementation-Version", manifest.getImplementationVersion());
            System.out.printf("%s: %s%n", "Command-Name", manifest.getCommandName());
            System.out.printf("%s: %s%n", "Command-Version", manifest.getCommandVersion());
        }
    }

    /* -------------------------------------------------------------------------------------------- */
}
