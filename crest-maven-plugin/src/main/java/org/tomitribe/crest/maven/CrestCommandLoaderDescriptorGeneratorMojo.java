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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.TreeSet;

import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.Opcodes.ASM9;

@Mojo(name = "descriptor", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class CrestCommandLoaderDescriptorGeneratorMojo extends AbstractMojo {
    private static final String COMMAND_MARKER = "Lorg/tomitribe/crest/api/Command;";
    private static final String INTERCEPTOR_MARKER = "Lorg/tomitribe/crest/api/interceptor/CrestInterceptor;";
    private static final String EDITOR_MARKER = "Lorg/tomitribe/crest/api/Editor;";

    @Parameter(property = "crest.descriptor.classes", defaultValue = "${project.build.outputDirectory}")
    protected File classes;

    @Parameter(property = "crest.descriptor.output", defaultValue = "${project.build.outputDirectory}/crest-commands.txt")
    protected File output;

    private static final String LOADER_SERVICE = "META-INF/services/org.tomitribe.crest.api.Loader";
    private static final String CREST_COMMANDS_LOADER = "org.tomitribe.crest.CrestCommandsLoader";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (classes == null || !classes.isDirectory()) {
            getLog().warn((classes != null ? classes.getAbsolutePath() : "null") + " is not a directory, skipping");
            return;
        }

        // find all annotated classes: @Command, @Editor, @CrestInterceptor
        final Collection<String> found = new TreeSet<>(); // sorted if a human wants to check it
        try {
            scan(found, classes);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // write crest-commands.txt (all annotated classes in one file)
        if (!output.getParentFile().isDirectory() && !output.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Can't create " + output.getAbsolutePath());
        }
        try {
            Files.write(output.toPath(), String.join("\n", found).getBytes(StandardCharsets.UTF_8));
            getLog().info("Wrote " + output);
        } catch (final IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }

        // Generate META-INF/services/org.tomitribe.crest.api.Loader
        // unless the user already provides their own
        final File serviceFile = new File(classes, LOADER_SERVICE);
        if (!serviceFile.exists()) {
            if (!serviceFile.getParentFile().isDirectory() && !serviceFile.getParentFile().mkdirs()) {
                throw new MojoExecutionException("Can't create " + serviceFile.getParentFile().getAbsolutePath());
            }
            try {
                Files.write(serviceFile.toPath(), CREST_COMMANDS_LOADER.getBytes(StandardCharsets.UTF_8));
                getLog().info("Wrote " + serviceFile);
            } catch (final IOException e) {
                throw new MojoFailureException(e.getMessage(), e);
            }
        } else {
            getLog().info("User-provided " + LOADER_SERVICE + " found, skipping generation");
        }
    }

    private void scan(final Collection<String> found, final File file) throws IOException {
        if (file.isFile()) {
            if (file.getName().endsWith(".class")) {
                final ScanResult result = scanClass(file);
                if (result.type != ScanResultType.NONE) {
                    found.add(result.name);
                }
            }
        } else if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (final File child : children) {
                    scan(found, child);
                }
            }
        }
    }

    private ScanResult scanClass(final File classFile) throws IOException {
        try (InputStream stream = new FileInputStream(classFile)) {
            final ClassReader reader = new ClassReader(stream);
            reader.accept(new ClassVisitor(ASM9) {
                private String className;

                @Override
                public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                    className = name.replace('/', '.');
                }

                @Override
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    if (COMMAND_MARKER.equals(desc)) {
                        throw new CommandFoundException(new ScanResult(ScanResultType.COMMAND, className));
                    }
                    if (EDITOR_MARKER.equals(desc)) {
                        throw new CommandFoundException(new ScanResult(ScanResultType.EDITOR, className));
                    }
                    return super.visitAnnotation(desc, visible);
                }

                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                    return new MethodVisitor(ASM9) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                            if (COMMAND_MARKER.equals(desc)) {
                                throw new CommandFoundException(new ScanResult(ScanResultType.COMMAND, className));
                            }
                            if (INTERCEPTOR_MARKER.equals(desc)) {
                                throw new CommandFoundException(new ScanResult(ScanResultType.INTERCEPTOR, className));
                            }
                            return super.visitAnnotation(desc, visible);
                        }
                    };
                }
            }, SKIP_CODE + SKIP_DEBUG + SKIP_FRAMES);
        } catch (final CommandFoundException cfe) {
            return cfe.result;
        }
        return ScanResult.NONE;
    }

    private static class CommandFoundException extends RuntimeException {
        private final ScanResult result;

        public CommandFoundException(final ScanResult result) {
            super(result.name);
            this.result = result;
        }
    }

    private enum ScanResultType {
        NONE,
        COMMAND,
        INTERCEPTOR,
        EDITOR
    }

    private static class ScanResult {
        private static final ScanResult NONE = new ScanResult(ScanResultType.NONE, null);

        private final ScanResultType type;
        private final String name;

        private ScanResult(final ScanResultType type, final String name) {
            this.type = type;
            this.name = name;
        }
    }
}
