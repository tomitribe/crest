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

public enum Border {

    /**
     * Example:
     * <pre>
     *     id               project           releaseDate   version
     * ----------- ------------------------- ------------- ---------
     *          9   Apache TomEE              2016-05-17    7.0.x
     *  523456789   Tomcat                    2018-01-17    9.0.x
     *         14   Apache ActiveMQ Classic   2022-03-09    5.17.x
     * </pre>
     */
    asciiCompact,

    /**
     * Example:
     * <pre>
     * ...............................................................
     * :    id     :         project         : releaseDate : version :
     * :...........:.........................:.............:.........:
     * :         9 : Apache TomEE            : 2016-05-17  : 7.0.x   :
     * : 523456789 : Tomcat                  : 2018-01-17  : 9.0.x   :
     * :        14 : Apache ActiveMQ Classic : 2022-03-09  : 5.17.x  :
     * :...........:.........................:.............:.........:
     * </pre>
     */
    asciiDots,

    /**
     * Example:
     * <pre>
     * +===========+=========================+=============+=========+
     * |    id     |         project         | releaseDate | version |
     * +===========+=========================+=============+=========+
     * |         9 | Apache TomEE            | 2016-05-17  | 7.0.x   |
     * +-----------+-------------------------+-------------+---------+
     * | 523456789 | Tomcat                  | 2018-01-17  | 9.0.x   |
     * +-----------+-------------------------+-------------+---------+
     * |        14 | Apache ActiveMQ Classic | 2022-03-09  | 5.17.x  |
     * +-----------+-------------------------+-------------+---------+
     * </pre>
     */
    asciiSeparated,

    /**
     * Example:
     * <pre>
     * |    id     |         project         | releaseDate | version |
     * |-----------|-------------------------|-------------|---------|
     * |         9 | Apache TomEE            | 2016-05-17  | 7.0.x   |
     * | 523456789 | Tomcat                  | 2018-01-17  | 9.0.x   |
     * |        14 | Apache ActiveMQ Classic | 2022-03-09  | 5.17.x  |
     * </pre>
     */
    githubMarkdown,

    /**
     * Example:
     * <pre>
     * +-----------+-------------------------+-------------+---------+
     * |    id     |         project         | releaseDate | version |
     * +-----------+-------------------------+-------------+---------+
     * |         9 | Apache TomEE            | 2016-05-17  | 7.0.x   |
     * | 523456789 | Tomcat                  | 2018-01-17  | 9.0.x   |
     * |        14 | Apache ActiveMQ Classic | 2022-03-09  | 5.17.x  |
     * +-----------+-------------------------+-------------+---------+
     * </pre>
     */
    mysqlStyle,

    /**
     * Example:
     * <pre>
     * +-----------+-------------------------+-------------+---------+
     * |    id     |         project         | releaseDate | version |
     * +===========+=========================+=============+=========+
     * |         9 | Apache TomEE            | 2016-05-17  | 7.0.x   |
     * | 523456789 | Tomcat                  | 2018-01-17  | 9.0.x   |
     * |        14 | Apache ActiveMQ Classic | 2022-03-09  | 5.17.x  |
     * +-----------+-------------------------+-------------+---------+
     * </pre>
     */
    reStructuredTextGrid,

    /**
     * Example:
     * <pre>
     * =========== ========================= ============= =========
     *     id               project           releaseDate   version
     * =========== ========================= ============= =========
     *          9   Apache TomEE              2016-05-17    7.0.x
     *  523456789   Tomcat                    2018-01-17    9.0.x
     *         14   Apache ActiveMQ Classic   2022-03-09    5.17.x
     * =========== ========================= ============= =========
     * </pre>
     */
    reStructuredTextSimple,

    /**
     * Example:
     * <pre>
     *     id     |         project         | releaseDate | version
     * -----------|-------------------------|-------------|---------
     *          9 | Apache TomEE            | 2016-05-17  | 7.0.x
     *  523456789 | Tomcat                  | 2018-01-17  | 9.0.x
     *         14 | Apache ActiveMQ Classic | 2022-03-09  | 5.17.x
     * </pre>
     */
    redditMarkdown,

    /**
     * Example:
     * <pre>
     * ╔═══════════╦═════════════════════════╦═════════════╦═════════╗
     * ║    id     ║         project         ║ releaseDate ║ version ║
     * ╠═══════════╬═════════════════════════╬═════════════╬═════════╣
     * ║         9 ║ Apache TomEE            ║ 2016-05-17  ║ 7.0.x   ║
     * ║ 523456789 ║ Tomcat                  ║ 2018-01-17  ║ 9.0.x   ║
     * ║        14 ║ Apache ActiveMQ Classic ║ 2022-03-09  ║ 5.17.x  ║
     * ╚═══════════╩═════════════════════════╩═════════════╩═════════╝
     * </pre>
     */
    unicodeDouble,

    /**
     * Example:
     * <pre>
     * ┌───────────┬─────────────────────────┬─────────────┬─────────┐
     * │    id     │         project         │ releaseDate │ version │
     * ├───────────┼─────────────────────────┼─────────────┼─────────┤
     * │         9 │ Apache TomEE            │ 2016-05-17  │ 7.0.x   │
     * │ 523456789 │ Tomcat                  │ 2018-01-17  │ 9.0.x   │
     * │        14 │ Apache ActiveMQ Classic │ 2022-03-09  │ 5.17.x  │
     * └───────────┴─────────────────────────┴─────────────┴─────────┘
     * </pre>
     */
    unicodeSingle,

    /**
     * Example:
     * <pre>
     * ┌───────────┬─────────────────────────┬─────────────┬─────────┐
     * │    id     │         project         │ releaseDate │ version │
     * ├═══════════┼═════════════════════════┼═════════════┼═════════┤
     * │         9 │ Apache TomEE            │ 2016-05-17  │ 7.0.x   │
     * ├───────────┼─────────────────────────┼─────────────┼─────────┤
     * │ 523456789 │ Tomcat                  │ 2018-01-17  │ 9.0.x   │
     * ├───────────┼─────────────────────────┼─────────────┼─────────┤
     * │        14 │ Apache ActiveMQ Classic │ 2022-03-09  │ 5.17.x  │
     * └───────────┴─────────────────────────┴─────────────┴─────────┘
     * </pre>
     */
    unicodeSingleSeparated,

    /**
     * Example:
     * <pre>
     *    id               project           releaseDate   version
     *
     *         9   Apache TomEE              2016-05-17    7.0.x
     * 523456789   Tomcat                    2018-01-17    9.0.x
     *        14   Apache ActiveMQ Classic   2022-03-09    5.17.x
     * </pre>
     */
    whitespaceCompact,

    /**
     * Example:
     * <pre>
     *    id               project           releaseDate   version
     *
     *         9   Apache TomEE              2016-05-17    7.0.x
     *
     * 523456789   Tomcat                    2018-01-17    9.0.x
     *
     *        14   Apache ActiveMQ Classic   2022-03-09    5.17.x
     * </pre>
     */
    whitespaceSeparated,
}
