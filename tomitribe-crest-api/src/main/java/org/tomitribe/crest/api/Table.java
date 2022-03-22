/*
 * Copyright 2022 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.crest.api;

import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@CrestInterceptor
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Table {
    /**
     * When a collection or stream of objects is returned, this setting allows
     * individual fields or attributes in the return objects to be selected as
     * columns for the table.  This also dictates the order in which the columns
     * appear.
     */
    String fields() default "";

    /**
     * <p>
     * A space-delimited list of fields to use for sorting the table output.  The
     * first field name listed will be the primary sort, the second field will be
     * the secondary sort and so on.
     * </p>
     * <p>
     * Sorts are always ascending.
     * </p>
     * <p>
     * For example, imagine the following data structure
     * </p>
     * <pre>
     * public class Person {
     *     private String firstName;
     *     private String lastName;
     *     private Address address;
     * }
     *
     * public class Address {
     *     private String street;
     *     private String city;
     *     private String state;
     *     private int zipCode;
     * }
     * </pre>
     * <p>
     * To sort by last name with a secondary sort by zip code, you can use the
     * following sort string
     * </p>
     * <pre>    @Table(sort = "lastName address.zipCode")
     * </pre>
     *
     */
    String sort() default "";

    Orientation orientation() default Orientation.horizontal;

    Format format() default Format.text;

    Border border() default Border.asciiCompact;

    enum Orientation {
        horizontal,
        vertical
    }

    enum Format {
        text,
        csv,
        tsv,
        html
    }

    enum Border {
        whitespaceSeparated,
        whitespaceCompact,
        mysqlStyle,
        asciiSeparated,
        asciiCompact,
        githubMarkdown,
        redditMarkdown,
        reStructuredTextGrid,
        reStructuredTextSimple,
        asciiDots,
        unicodeDouble,
        unicodeSingle,
        unicodeSingleSeparated
    }
}
