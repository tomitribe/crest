/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.crest.javadoc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Javadoc {

    private final String content;
    private final List<Param> params;
    private final List<Throws> throwing;
    private final List<Author> authors;
    private final List<See> sees;
    private final List<Tag> unknown;
    private final Return aReturn;
    private final Since since;
    private final Version version;
    private final Deprecated deprecated;

    Javadoc(final String content,
            final List<Param> params,
            final List<Throws> throwing,
            final List<Author> authors,
            final List<See> sees,
            final List<Tag> unknown,
            final Return aReturn,
            final Since since,
            final Deprecated deprecated,
            final Version version) {
        this.content = content;
        this.params = params;
        this.throwing = throwing;
        this.authors = authors;
        this.sees = sees;
        this.unknown = unknown;
        this.aReturn = aReturn;
        this.since = since;
        this.version = version;
        this.deprecated = deprecated;
    }

    public boolean isEmpty() {
        return content == null &&
                params == null &&
                throwing == null &&
                authors == null &&
                sees == null &&
                unknown == null &&
                aReturn == null &&
                since == null &&
                version == null &&
                deprecated == null;
    }

    public static Builder builder() {
        return new Javadoc.Builder();
    }

    public Map<String, Param> getParametersByName() {
        if (this.getParams() == null) return new HashMap<>();

        return this.getParams().stream()
                .collect(Collectors.toMap(Param::getName, Function.identity()));
    }

    public List<Tag> getUnknown() {
        return unknown;
    }

    public Deprecated getDeprecated() {
        return deprecated;
    }

    public String getContent() {
        return this.content;
    }

    public List<Param> getParams() {
        return this.params;
    }

    public List<Throws> getThrowing() {
        return this.throwing;
    }

    public List<Author> getAuthors() {
        return this.authors;
    }

    public List<See> getSees() {
        return this.sees;
    }

    public Return getReturn() {
        return this.aReturn;
    }

    public Since getSince() {
        return this.since;
    }

    public Version getVersion() {
        return this.version;
    }

    public String toString() {
        return "Javadoc(content=" + this.getContent() + ", params=" + this.getParams() +
                ", throwing=" + this.getThrowing() + ", authors=" + this.getAuthors() +
                ", sees=" + this.getSees() + ", return=" + this.getReturn() +
                ", since=" + this.getSince() + ", version=" + this.getVersion() + ")";
    }

    public static class Param {
        private final String name;
        private final String description;

        Param(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static Builder builder() {
            return new Param.Builder();
        }

        public String getName() {
            return this.name;
        }

        public String getDescription() {
            return this.description;
        }

        public String toString() {
            return "Javadoc.Param(name=" + this.getName() + ", description=" + this.getDescription() + ")";
        }

        public static class Builder {
            private String name;
            private String description;

            Builder() {
            }

            public Param.Builder name(String name) {
                this.name = name;
                return this;
            }

            public Param.Builder description(String description) {
                this.description = description;
                return this;
            }

            public Param build() {
                return new Param(name, description);
            }

            public String toString() {
                return "Javadoc.Param.Builder(name=" + this.name + ", description=" + this.description + ")";
            }
        }
    }

    public static class Tag {
        private final String name;
        private final String content;

        Tag(String name, String content) {
            this.name = name;
            this.content = content;
        }

        public static Builder builder() {
            return new Tag.Builder();
        }

        public String getName() {
            return this.name;
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.Tag(name=" + this.getName() + ", content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String name;
            private String content;

            Builder() {
            }

            public Tag.Builder name(String name) {
                this.name = name;
                return this;
            }

            public Tag.Builder content(String content) {
                this.content = content;
                return this;
            }

            public Tag build() {
                return new Tag(name, content);
            }

            public String toString() {
                return "Javadoc.Tag.Builder(name=" + this.name + ", content=" + this.content + ")";
            }
        }
    }

    public static class Author {
        private final String content;

        Author(String content) {
            this.content = content;
        }

        public static Builder builder() {
            return new Author.Builder();
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.Author(content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String content;

            Builder() {
            }

            public Author.Builder content(String content) {
                this.content = content;
                return this;
            }

            public Author build() {
                return new Author(content);
            }

            public String toString() {
                return "Javadoc.Author.Builder(content=" + this.content + ")";
            }
        }
    }

    public static class Version {
        private final String content;

        Version(String content) {
            this.content = content;
        }

        public static Builder builder() {
            return new Version.Builder();
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.Version(content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String content;

            Builder() {
            }

            public Version.Builder content(String content) {
                this.content = content;
                return this;
            }

            public Version build() {
                return new Version(content);
            }

            public String toString() {
                return "Javadoc.Version.Builder(content=" + this.content + ")";
            }
        }
    }

    public static class See {
        private final String content;

        See(String content) {
            this.content = content;
        }

        public static Builder builder() {
            return new See.Builder();
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.See(content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String content;

            Builder() {
            }

            public See.Builder content(String content) {
                this.content = content;
                return this;
            }

            public See build() {
                return new See(content);
            }

            public String toString() {
                return "Javadoc.See.Builder(content=" + this.content + ")";
            }
        }
    }

    public static class Return {
        private final String content;

        Return(String content) {
            this.content = content;
        }

        public static Builder builder() {
            return new Return.Builder();
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.Return(content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String content;

            Builder() {
            }

            public Return.Builder content(String content) {
                this.content = content;
                return this;
            }

            public Return build() {
                return new Return(content);
            }

            public String toString() {
                return "Javadoc.Return.Builder(content=" + this.content + ")";
            }
        }
    }

    public static class Throws {
        private final String classname;
        private final String description;

        Throws(String classname, String description) {
            this.classname = classname;
            this.description = description;
        }

        public static Builder builder() {
            return new Throws.Builder();
        }

        public String getClassname() {
            return this.classname;
        }

        public String getDescription() {
            return this.description;
        }

        public String toString() {
            return "Javadoc.Throws(classname=" + this.getClassname() + ", description=" + this.getDescription() + ")";
        }

        public static class Builder {
            private String classname;
            private String description;

            Builder() {
            }

            public Throws.Builder classname(String classname) {
                this.classname = classname;
                return this;
            }

            public Throws.Builder description(String description) {
                this.description = description;
                return this;
            }

            public Throws build() {
                return new Throws(classname, description);
            }

            public String toString() {
                return "Javadoc.Throws.Builder(classname=" + this.classname + ", description=" + this.description + ")";
            }
        }
    }

    public static class Since {
        private final String content;

        Since(String content) {
            this.content = content;
        }

        public static Builder builder() {
            return new Since.Builder();
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.Since(content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String content;

            Builder() {
            }

            public Since.Builder content(String content) {
                this.content = content;
                return this;
            }

            public Since build() {
                return new Since(content);
            }

            public String toString() {
                return "Javadoc.Since.Builder(content=" + this.content + ")";
            }
        }
    }

    public static class Deprecated {
        private final String content;

        Deprecated(String content) {
            this.content = content;
        }

        public static Builder builder() {
            return new Deprecated.Builder();
        }

        public String getContent() {
            return this.content;
        }

        public String toString() {
            return "Javadoc.Deprecated(content=" + this.getContent() + ")";
        }

        public static class Builder {
            private String content;

            Builder() {
            }

            public Deprecated.Builder content(String content) {
                this.content = content;
                return this;
            }

            public Deprecated build() {
                return new Deprecated(content);
            }

            public String toString() {
                return "Javadoc.Deprecated.Builder(content=" + this.content + ")";
            }
        }
    }


    public static class Builder {
        private String content;
        private List<Param> params;
        private List<Throws> throwing;
        private List<Author> authors;
        private List<See> sees;
        private List<Tag> unknown;
        private Return aReturn;
        private Since since;
        private Version version;
        private Deprecated deprecated;

        Builder() {
        }

        public Javadoc.Builder content(String content) {
            this.content = content;
            return this;
        }

        public Javadoc.Builder param(final Param param) {
            if (params == null) params = new ArrayList<>();
            this.params.add(param);
            return this;
        }

        public Javadoc.Builder params(List<Param> params) {
            this.params = params;
            return this;
        }

        public Javadoc.Builder unknown(final Tag tag) {
            if (unknown == null) unknown = new ArrayList<>();
            this.unknown.add(tag);
            return this;
        }

        public Javadoc.Builder unknown(List<Tag> tags) {
            this.unknown = tags;
            return this;
        }

        public Javadoc.Builder throwing(List<Throws> throwing) {
            this.throwing = throwing;
            return this;
        }

        public Javadoc.Builder throwing(final Throws throwing) {
            if (this.throwing == null) this.throwing = new ArrayList<>();
            this.throwing.add(throwing);
            return this;
        }

        public Javadoc.Builder author(final Author author) {
            if (authors == null) authors = new ArrayList<>();
            this.authors.add(author);
            return this;
        }

        public Javadoc.Builder authors(List<Author> authors) {
            this.authors = authors;
            return this;
        }

        public Javadoc.Builder see(final See see) {
            if (sees == null) sees = new ArrayList<>();
            this.sees.add(see);
            return this;
        }

        public Javadoc.Builder sees(List<See> sees) {
            this.sees = sees;
            return this;
        }

        public Javadoc.Builder aReturn(Return aReturn) {
            this.aReturn = aReturn;
            return this;
        }

        public Javadoc.Builder since(Since since) {
            this.since = since;
            return this;
        }

        public Javadoc.Builder deprecated(Deprecated deprecated) {
            this.deprecated = deprecated;
            return this;
        }

        public Javadoc.Builder version(Version version) {
            this.version = version;
            return this;
        }

        public Javadoc build() {
            return new Javadoc(content, params, throwing, authors, sees, unknown, aReturn, since, deprecated, version);
        }

        public String toString() {
            return "Javadoc.Builder(content=" + this.content + ", params=" + this.params +
                    ", throwsList=" + this.throwing + ", authors=" + this.authors +
                    ", sees=" + this.sees + ", aReturn=" + this.aReturn + ", since=" +
                    this.since + ", version=" + this.version + ")";
        }

    }
}
