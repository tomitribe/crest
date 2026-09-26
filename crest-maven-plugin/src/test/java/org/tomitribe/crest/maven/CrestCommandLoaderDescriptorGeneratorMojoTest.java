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
package org.tomitribe.crest.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.GlobalOptions;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CrestCommandLoaderDescriptorGeneratorMojoTest {
    @Test
    public void scan() throws IOException, MojoFailureException, MojoExecutionException {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = new CrestCommandLoaderDescriptorGeneratorMojo();
        mojo.classes = new File("target/test-classes");
        mojo.output = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/test.txt");
        mojo.execute();

        final BufferedReader reader = new BufferedReader(new FileReader(mojo.output));
        final Collection<String> found = new HashSet<>();
        String line;
        while ((line = reader.readLine()) != null) {
            found.add(line);
        }
        reader.close();

        assertEquals(new HashSet<String>() {{ add(ClassCommand.class.getName()); add(MethodCommand.class.getName()); add(Options.class.getName()); }}, found);
    }

    @Test
    public void excludeByExactName() throws IOException, MojoFailureException, MojoExecutionException {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = new CrestCommandLoaderDescriptorGeneratorMojo();
        mojo.classes = new File("target/test-classes");
        mojo.output = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/exclude-exact.txt");
        mojo.excludes = Collections.singletonList(ClassCommand.class.getName());
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertFalse(found.contains(ClassCommand.class.getName()));
        assertTrue(found.contains(MethodCommand.class.getName()));
    }

    @Test
    public void excludeByWildcard() throws IOException, MojoFailureException, MojoExecutionException {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = new CrestCommandLoaderDescriptorGeneratorMojo();
        mojo.classes = new File("target/test-classes");
        mojo.output = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/exclude-wildcard.txt");
        mojo.excludes = Collections.singletonList("*ClassCommand");
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertFalse(found.contains(ClassCommand.class.getName()));
        assertTrue(found.contains(MethodCommand.class.getName()));
    }

    @Test
    public void includeAdditionalClass() throws IOException, MojoFailureException, MojoExecutionException {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = new CrestCommandLoaderDescriptorGeneratorMojo();
        mojo.classes = new File("target/test-classes");
        mojo.output = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/include.txt");
        mojo.includes = Collections.singletonList("com.example.ManualCommand");
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertTrue(found.contains(ClassCommand.class.getName()));
        assertTrue(found.contains(MethodCommand.class.getName()));
        assertTrue(found.contains("com.example.ManualCommand"));
    }

    @Test
    public void includeAndExclude() throws IOException, MojoFailureException, MojoExecutionException {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = new CrestCommandLoaderDescriptorGeneratorMojo();
        mojo.classes = new File("target/test-classes");
        mojo.output = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/include-exclude.txt");
        mojo.excludes = Collections.singletonList(ClassCommand.class.getName());
        mojo.includes = Collections.singletonList("com.example.Extra");
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertFalse(found.contains(ClassCommand.class.getName()));
        assertTrue(found.contains(MethodCommand.class.getName()));
        assertTrue(found.contains("com.example.Extra"));
    }

    @Test
    public void scanDependenciesFindsClassesInJars() throws Exception {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = dependencyMojo("deps-jar", artifact("com.example", "lib", fixtureJar()));
        mojo.scanDependencies = true;
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertEquals(new HashSet<String>() {{ add(ClassCommand.class.getName()); add(MethodCommand.class.getName()); add(Options.class.getName()); }}, found);
    }

    @Test
    public void scanDependenciesIsOffByDefault() throws Exception {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = dependencyMojo("deps-off", artifact("com.example", "lib", fixtureJar()));
        mojo.execute();

        assertTrue(readLines(mojo.output).isEmpty());
    }

    @Test
    public void scanDependenciesSkipsCrestRuntime() throws Exception {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = dependencyMojo("deps-crest",
                artifact("org.tomitribe", "tomitribe-crest", fixtureJar()),
                artifact("org.tomitribe", "tomitribe-crest-xbean", fixtureJar()));
        mojo.scanDependencies = true;
        mojo.execute();

        assertTrue(readLines(mojo.output).isEmpty());
    }

    @Test
    public void scanDependenciesHonorsExcludes() throws Exception {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = dependencyMojo("deps-exclude", artifact("com.example", "lib", fixtureJar()));
        mojo.scanDependencies = true;
        mojo.excludes = Collections.singletonList(ClassCommand.class.getName());
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertFalse(found.contains(ClassCommand.class.getName()));
        assertTrue(found.contains(MethodCommand.class.getName()));
    }

    /**
     * A reactor dependency that has not been packaged yet resolves to its
     * classes directory rather than a jar.
     */
    @Test
    public void scanDependenciesHandlesDirectoryArtifacts() throws Exception {
        final CrestCommandLoaderDescriptorGeneratorMojo mojo = dependencyMojo("deps-dir", artifact("com.example", "reactor", new File("target/test-classes")));
        mojo.scanDependencies = true;
        mojo.execute();

        final Collection<String> found = readLines(mojo.output);
        assertTrue(found.contains(ClassCommand.class.getName()));
        assertTrue(found.contains(MethodCommand.class.getName()));
    }

    /**
     * A mojo whose own classes directory is empty, so anything found came
     * from the dependencies.
     */
    private static CrestCommandLoaderDescriptorGeneratorMojo dependencyMojo(final String name, final Artifact... artifacts) throws IOException {
        final File dir = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/" + name);
        final File empty = new File(dir, "classes");
        Files.createDirectories(empty.toPath());

        final MavenProject project = new MavenProject();
        project.setArtifacts(new HashSet<>(Arrays.asList(artifacts)));

        final CrestCommandLoaderDescriptorGeneratorMojo mojo = new CrestCommandLoaderDescriptorGeneratorMojo();
        mojo.classes = empty;
        mojo.output = new File(dir, "crest-commands.txt");
        mojo.project = project;
        return mojo;
    }

    private static Artifact artifact(final String groupId, final String artifactId, final File file) {
        final Artifact artifact = new DefaultArtifact(groupId, artifactId, "1.0", "compile", "jar", null, new DefaultArtifactHandler("jar"));
        artifact.setFile(file);
        return artifact;
    }

    /**
     * A jar holding the compiled fixture classes of this test, so the scan
     * sees the same names it would in target/test-classes.
     */
    private static File fixtureJar() throws IOException {
        final File jar = new File("target/CrestCommandLoaderDescriptorGeneratorMojoTest/fixtures.jar");
        Files.createDirectories(jar.getParentFile().toPath());
        try (OutputStream out = new FileOutputStream(jar); JarOutputStream jos = new JarOutputStream(out)) {
            for (final Class<?> clazz : Arrays.asList(ClassCommand.class, MethodCommand.class, Options.class, NotScannedClass.class, NotScannedMethod.class)) {
                final String path = clazz.getName().replace('.', '/') + ".class";
                jos.putNextEntry(new JarEntry("lib/" + path));
                try (InputStream in = new FileInputStream(new File("target/test-classes", path))) {
                    final byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        jos.write(buffer, 0, read);
                    }
                }
                jos.closeEntry();
            }
        }
        return jar;
    }

        private Collection<String> readLines(final File file) throws IOException {
        final Collection<String> lines = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    @Command
    public static class ClassCommand {
    }

    @GlobalOptions
    public static class Options {
    }

    public static class MethodCommand {
        @Command
        public static void mtd() {
            // no-op
        }
    }

    public static class NotScannedMethod {
        @Foo
        public static void mtd() {
            // no-op
        }
    }

    @Foo
    public static class NotScannedClass {
        public static void mtd() {
            // no-op
        }
    }

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface Foo {
        String value() default "";

        String usage() default "";
    }
}
