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

import org.tomitribe.util.Archive;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.stream.Stream;

public class Archives {
    public static ClassLoader classLoader(final Archive... archives) {
        final URL[] urls = Stream.of(archives)
                .map(Archive::asJar)
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (final MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);

        return new URLClassLoader(urls);
    }
}
