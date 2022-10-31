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
package org.tomitribe.crest.xbean;

import org.apache.xbean.finder.UrlSet;
import org.apache.xbean.finder.archive.Archive;
import org.apache.xbean.finder.archive.ClassesArchive;
import org.apache.xbean.finder.archive.ClasspathArchive;

import java.io.IOException;

public class ClasspathScanner extends XbeanScanningLoader {

    public ClasspathScanner() {
        super(System.getProperty("java.home") == null ? new ClassesArchive() : ClasspathScanner.defaultArchive());
    }

    private static Archive defaultArchive() {
        try {
            final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            UrlSet urls = new UrlSet(classLoader);
            urls = urls.excludeJvm();
            urls = urls.exclude(classLoader.getParent());

            return new ClasspathArchive(classLoader, urls.getUrls());

        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

}
