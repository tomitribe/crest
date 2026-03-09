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

import net.e175.klaus.zip.ZipPrefixer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;

/**
 * Prepends a shell stub to a shaded jar so it can be executed directly
 * on Unix systems without typing {@code java -jar}. The resulting file
 * is marked executable and optionally attached as a build artifact.
 */
@Mojo(name = "executable",
        threadSafe = true,
        defaultPhase = LifecyclePhase.PACKAGE)
public class ExecutableJarMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Specific file to make executable instead of the default shaded artifact.
     */
    @Parameter(property = "crest.executable.inputFile")
    private File inputFile;

    /**
     * Java command line arguments to embed in the shell stub.
     * The default passes the script name to Crest for help output
     * and allows users to set JVM flags via the JAVA_OPTS environment variable.
     */
    @Parameter(property = "crest.executable.flags",
            defaultValue = "-Dcmd=\"$0\" $JAVA_OPTS")
    private String flags;

    /**
     * Name of the generated executable in the target directory.
     */
    @Parameter(property = "crest.executable.programFile",
            defaultValue = "${project.artifactId}")
    private String programFile;

    /**
     * Classifier of the artifact to make executable.
     * Defaults to "all" which is the classifier produced by
     * maven-shade-plugin with shadedClassifierName.
     */
    @Parameter(property = "crest.executable.classifier",
            defaultValue = "all")
    private String classifier;

    /**
     * Attach the executable as a build artifact so it is included
     * in install and deploy.
     */
    @Parameter(property = "crest.executable.attach",
            defaultValue = "true")
    private boolean attachProgramFile;

    /**
     * Path to a custom shell script to use instead of the default stub.
     * Can be a filesystem path or a resource name within the jar.
     */
    @Parameter(property = "crest.executable.scriptFile")
    private String scriptFile;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            final File source = findSourceJar();
            final File dir = source.getParentFile();
            final File exec = new File(dir, programFile);

            Files.copy(source.toPath(), exec.toPath(), StandardCopyOption.REPLACE_EXISTING);
            makeExecutable(exec);

            if (attachProgramFile) {
                projectHelper.attachArtifact(project, "sh", exec);
            }
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private File findSourceJar() throws MojoExecutionException {
        if (inputFile != null) {
            if (inputFile.exists()) {
                return inputFile;
            }
            throw new MojoExecutionException("Unable to find " + inputFile);
        }

        // Look for an attached artifact with the configured classifier
        for (final Artifact artifact : project.getAttachedArtifacts()) {
            if ("jar".equals(artifact.getType()) && classifier.equals(artifact.getClassifier())) {
                return artifact.getFile();
            }
        }

        // Fall back to the main artifact if no classifier match
        final Artifact main = project.getArtifact();
        if (main != null && main.getFile() != null && main.getFile().exists()) {
            return main.getFile();
        }

        throw new MojoExecutionException(
                format("Could not find artifact with classifier '%s'. "
                        + "Is the maven-shade-plugin configured with shadedClassifierName?", classifier));
    }

    private void makeExecutable(final File file) throws MojoExecutionException {
        final Path target = file.toPath();
        try {
            ZipPrefixer.applyPrefixBytesToZip(target,
                    Arrays.asList(getPreamble(target.toUri()), "\n\n".getBytes(UTF_8)));
        } catch (final IOException e) {
            throw new MojoExecutionException(format("Failed to apply prefix to JAR [%s]", file.getAbsolutePath()), e);
        }

        if (!file.setExecutable(true, false)) {
            throw new MojoExecutionException(format("Could not make JAR [%s] executable", file.getAbsolutePath()));
        }
        getLog().info(format("Created executable: %s", file.getAbsolutePath()));
    }

    private byte[] getPreamble(final java.net.URI uri) throws MojoExecutionException {
        try {
            if (scriptFile == null) {
                return ("#!/bin/sh\n\nexec java " + flags + " -jar \"$0\" \"$@\"").getBytes(UTF_8);
            }

            if (Files.isReadable(Paths.get(scriptFile))) {
                return readAllBytes(Paths.get(scriptFile));
            }

            // Try loading scriptFile as a resource from within the jar
            try (URLClassLoader loader = new URLClassLoader(new URL[]{uri.toURL()}, null);
                 InputStream in = loader.getResourceAsStream(scriptFile)) {
                if (in == null) {
                    throw new IOException("Unable to load " + scriptFile);
                }
                return toBytes(in);
            }
        } catch (final IOException e) {
            throw new MojoExecutionException("Unable to load preamble from " + scriptFile, e);
        }
    }

    private static byte[] toBytes(final InputStream in) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}
