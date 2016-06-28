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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.TreeSet;

import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassReader.SKIP_FRAMES;
import static org.objectweb.asm.Opcodes.ASM5;

@Mojo(name = "descriptor", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class CrestCommandLoaderDescriptorGeneratorMojo extends AbstractMojo {
    private static final String COMMAND_MARKER = "Lorg/tomitribe/crest/api/Command;";
    private static final String INTERCEPTOR_MARKER = "Lorg/tomitribe/crest/api/interceptor/CrestInterceptor;";

    @Parameter(property = "crest.descriptor.classes", defaultValue = "${project.build.outputDirectory}")
    protected File classes;

    @Parameter(property = "crest.descriptor.output", defaultValue = "${project.build.outputDirectory}/crest-commands.txt")
    protected File output;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (classes == null || !classes.isDirectory()) {
            getLog().warn((classes != null ? classes.getAbsolutePath() : "null") + " is not a directory, skipping");
            return;
        }

        // find commands
        final Collection<String> commands = new TreeSet<>(); // sorted if a human wants to check it
        try {
            scan(commands, classes);
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        // write it
        if (!output.getParentFile().isDirectory() && !output.getParentFile().mkdirs()) {
            throw new MojoExecutionException("Can't create " + output.getAbsolutePath());
        }
        try (FileWriter writer = new FileWriter(output)){
            for (final String cmd : commands) {
                writer.write(cmd + '\n');
            }
        } catch (final IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void scan(final Collection<String> commands, final File file) throws IOException {
        if (file.isFile()) {
            if (file.getName().endsWith(".class")) {
                final String command = commandName(file);
                if (command != null) {
                    commands.add(command);
                }
            } // else we don't care
        } else if (file.isDirectory()) {
            final File[] children = file.listFiles();
            if (children != null) {
                for (final File child : children) {
                    scan(commands, child);
                }
            }
        }
    }

    private String commandName(final File classFile) throws IOException {
        try (InputStream stream = new FileInputStream(classFile)) {
            final ClassReader reader = new ClassReader(stream);
            reader.accept(new ClassVisitor(ASM5) {
                private String className;

                @Override
                public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
                    className = name.replace('/', '.');
                }

                @Override
                public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
                    checkAnnotation(desc);
                    return super.visitAnnotation(desc, visible);
                }

                @Override
                public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
                    return new MethodVisitor(ASM5) {
                        @Override
                        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                            checkAnnotation(desc);
                            return super.visitAnnotation(desc, visible);
                        }
                    };
                }

                private void checkAnnotation(final String desc) {
                    if (COMMAND_MARKER.equals(desc) || INTERCEPTOR_MARKER.equals(desc)) {
                        throw new CommandFoundException(className);
                    }
                }
            }, SKIP_CODE + SKIP_DEBUG + SKIP_FRAMES);
        } catch (final CommandFoundException cfe) {
            return cfe.getMessage(); // class name
        }
        return null;
    }

    private static class CommandFoundException extends RuntimeException {
        public CommandFoundException(final String className) {
            super(className);
        }
    }
}
