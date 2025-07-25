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
package org.tomitribe.crest.api.table;

import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Place @Table on any @Command method that returns a {@code java.util.Iterable} to have
 * that collection formatted as a table.  For example, given a method
 * like the following
 *<p>
 *
 * <pre>
 * &nbsp;@Table
 * &nbsp;@Command
 * &nbsp;public List&lt;Branch&gt; branches() {
 * &nbsp;    return branches;
 * &nbsp;}
 * </pre>
 *
 * And a Branch class that had the following fields with proper getters
 * <p>
 * <pre>
 * public static class Branch {
 *     private final int id;
 *     private final String project;
 *     private final String version;
 *     private final LocalDate releaseDate;
 *     // constructor and getters omitted for brevity...
 * }
 * </pre>
 *
 * That would output a table like the following
 * <p>
 * <pre>
 *     id               project           releaseDate   version
 * ----------- ------------------------- ------------- ---------
 *          9   Apache TomEE              2016-05-17    7.0.x
 *  523456789   Tomcat                    2018-01-17    9.0.x
 *         14   Apache ActiveMQ Classic   2022-03-09    5.17.x
 * </pre>
 *
 * <h2>Table Features</h2>
 *
 * <ul>
 *     <li><b>Alignment</b>: Numeric values will always be right-aligned.  Text values will be
 *     left-aligned</li>
 *     <li><b>Word Wrap</b>: The table will observe the size of the screen.  If the number of
 *     columns allows, the data will be word-wrapped in an attempt to keep large blocks of text
 *     like descriptions contained.</li>
 *     <li><b>Columns</b>: Public Fields or non-static get methods of items in the collection
 *     can be referenced to create columns.  A method such as {@code getReleaseDate()} would
 *     result in a {@code releaseDate} column in the table</li>
 *     <li><b>Types</b>: It is best if the instances returned by the {@code Iterable} are of
 *     the same class.  It is possible to return a mix of different class, however the columns
 *     will be influenced based on which type is seen first.</li>
 *     <li><b>Type Conversion</b>: Any type returned from a getter or public field can be included
 *     in the table.  At this time the {@code toString()} method is used to create the text output
 *     for each column.  PropertyEditor support is planned for future releases.</li>
 *     <li><b>Memory</b>: At this time the entire table contents are read from the returned
 *     {@code Iterable} before printing to the console, so all data must fit in memory even when
 *     being streamed from an external source.</li>
 * </ul>
 *
 * <h2>Overriding via @Table</h2>
 *
 * It is possible to override several aspects of how the tables look and are sorted via the @Table annotation as follows
 * <pre>
 * &nbsp;@Command
 * &nbsp;@Table(fields = "id project.name releaseDate version",
 * &nbsp;        sort = "releaseDate",
 * &nbsp;        header = false,
 * &nbsp;        border = Border.githubMarkdown)
 * &nbsp;public List&lt;Branch&gt; branches() {
 * &nbsp;    return branches;
 * &nbsp;}
 * </pre>
 * <p>
 * In this scenario users will get the table as designed in code and cannot override or change the formatting on the command line.
 *
 * <h2>Overriding via TableObjects and command-line flags</h2>
 *
 * The use of {@code TableOptions} can be used in the argument list of your {@code @Command} method allowing users to change
 * table formatting options on the command-line.  For example:
 * <p>
 * <pre>
 * &nbsp;@Command
 * &nbsp;@Table
 * &nbsp;public List&lt;Branch&gt; branches(final TableOptions tableOptions) {
 * &nbsp;    return branches;
 * &nbsp;}
 * </pre>
 * <p>
 * The TableOptions does not need to be used by the command method itself, but it will be seen by the Crest Interceptor
 * that implements the table functionality and allow users to specify the following command-line options
 * <pre>
 * branches --table-header --table-fields="id version releaseDate" --table-sort=version --table-border=mysqlStyle
 * </pre>
 *
 * <h2>Overriding via @Option and command-line flags</h2>
 *
 * The use of the regular Crest {@code Option} can also be used in the argument list of your {@code @Command} method
 * allowing users to change table formatting options on the command-line.  This can be useful you'd only like some
 * of the options to be overridden by users.
 * For example:
 * <p>
 * <pre>
 * &nbsp;@Command
 * &nbsp;@Table
 * &nbsp;public List&lt;Branch&gt; branches(
 * &nbsp;                @Option("table-border") final Border border,
 * &nbsp;                @Option("table-header") final Boolean header,
 * &nbsp;                @Option("table-sort") final String sort,
 * &nbsp;                @Option("table-fields") final String fields) {
 * &nbsp;    return branches;
 * &nbsp;}
 * </pre>
 * <p>
 * The as with TableOptions, the individual @Option arguments do not need to be used by the command method itself.  They
 * will be seen and used by the interceptor that implements the table functionality and allow users to specify
 * table options on the command line like the following.
 * <pre>
 * branches --no-table-header --table-fields="id version releaseDate" --table-sort=version --table-border=mysqlStyle
 * </pre>
 *
 * <h2>Combinations of @Table and TableOptions or @Option overrides</h2>
 *
 * It is possible to use @Table to set defaults for how the table will be rendered, but still add TableOptions or @Option
 * arguments to the command method so that users can override some or all of the options.
 * For example:
 * <p>
 * <pre>
 * &nbsp;@Command
 * &nbsp;@Table(fields = "id project.name releaseDate version",
 * &nbsp;        sort = "releaseDate",
 * &nbsp;        header = false,
 * &nbsp;        border = Border.githubMarkdown)
 * &nbsp;public List&lt;Branch&gt; branches(
 * &nbsp;                @Option("table-border") final Border border,
 * &nbsp;                @Option("table-header") final Boolean header,
 * &nbsp;                @Option("table-sort") final String sort,
 * &nbsp;                @Option("table-fields") final String fields) {
 * &nbsp;    return branches;
 * &nbsp;}
 * </pre>
 * <p>
 */
@CrestInterceptor
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Table {
    /**
     * A space-delimited list of fields to be selected as columns for the table.
     * This also dictates the order in which the columns appear.
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

    /**
     * When set to true a header will appear in the table identifying the field (or getter)
     * for each column.  This is the default.  When set to false, this header is omitted.
     * <p>
     * Omitting the header can will result in significantly smaller table widths in scenarios
     * where the the actual column values are smaller than the name of the column.
     */
    boolean header() default true;

//    Orientation orientation() default Orientation.horizontal;
//
//    Format format() default Format.text;

    /**
     * Changes the border used for the table to one of over 10 different styles
     */
    Border border() default Border.asciiCompact;

//    enum Orientation {
//        horizontal,
//        vertical
//    }
//
//    enum Format {
//        text,
//        csv,
//        tsv,
//        html
//    }

}
