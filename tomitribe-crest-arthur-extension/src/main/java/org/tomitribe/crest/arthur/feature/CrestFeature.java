/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.arthur.feature;

import com.oracle.svm.core.annotate.AutomaticFeature;
import com.oracle.svm.core.jdk.Resources;
import com.oracle.svm.core.option.HostedOptionKey;
import org.graalvm.compiler.options.Option;
import org.graalvm.compiler.options.OptionDescriptor;
import org.graalvm.compiler.options.OptionDescriptors;
import org.graalvm.compiler.options.OptionType;
import org.graalvm.nativeimage.hosted.Feature;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

@AutomaticFeature
public class CrestFeature implements Feature {
    public static final class Options {
        @Option(help = "Crest commands list file.", type = OptionType.User)
        // CHECKSTYLE:OFF
        static final HostedOptionKey<String> TomitribeCrestCommands = new HostedOptionKey<>(null);
        // CHECKSTYLE:ON
    }

    // org.graalvm.compiler.options.processor is not on central
    public static class CrestOptions implements OptionDescriptors {
        @Override
        public OptionDescriptor get(final String value) {
            switch (value) {
                case "TomitribeCrestCommands":
                    return OptionDescriptor.create(
                            value, OptionType.User, String.class,
                            "Crest commands.",
                            Options.class, value,
                            Options.TomitribeCrestCommands);
                default:
                    return null;
            }
        }

        @Override
        public Iterator<OptionDescriptor> iterator() {
            return Stream.of("TomitribeCrestCommands").map(this::get).iterator();
        }
    }

    @Override
    public void beforeAnalysis(final BeforeAnalysisAccess access) {
        if (Options.TomitribeCrestCommands.hasBeenSet()) {
            register(Options.TomitribeCrestCommands.getValue(), "crest-commands.txt");
        }
    }

    private void register(final String path, final String resource) {
        try (final InputStream stream = Files.newInputStream(Paths.get(path))) {
            Resources.registerResource(resource, stream);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
