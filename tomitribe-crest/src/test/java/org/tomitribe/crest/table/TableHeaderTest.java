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
package org.tomitribe.crest.table;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.api.table.Table;
import org.tomitribe.util.PrintString;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Revision$ $Date$
 */
public class TableHeaderTest {

    @Test
    public void defaults() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("alldefaults"),
                " id               project               releaseDate   version \n" +
                        "---- --------------------------------- ------------- ---------\n" +
                        " 13   Project{name='Apache TomEE'}      2012-04-27    1.0.x   \n" +
                        "  7   Project{name='Apache TomEE'}      2019-09-13    8.0.x   \n" +
                        " 12   Project{name='Apache TomEE'}      2012-09-28    1.5.x   \n" +
                        "  6   Project{name='Apache Tomcat'}     2021-02-01    10.0.x  \n" +
                        " 18   Project{name='Apache ActiveMQ'}   2015-11-30    5.13.x  \n" +
                        " 10   Project{name='Apache TomEE'}      2014-08-09    1.7.x   \n" +
                        "  4   Project{name='Apache Tomcat'}     2016-06-12    8.5.x   \n" +
                        " 11   Project{name='Apache TomEE'}      2013-11-17    1.6.x   \n" +
                        "  8   Project{name='Apache TomEE'}      2018-09-02    7.1.x   \n" +
                        "  2   Project{name='Apache Tomcat'}     2011-01-13    7.0.x   \n" +
                        " 15   Project{name='Apache ActiveMQ'}   2020-06-25    5.16.x  \n" +
                        " 17   Project{name='Apache ActiveMQ'}   2016-08-02    5.14.x  \n" +
                        "  1   Project{name='Apache Tomcat'}     2007-02-27    6.0.x   \n" +
                        "  3   Project{name='Apache Tomcat'}     2014-06-24    8.0.x   \n" +
                        " 16   Project{name='Apache ActiveMQ'}   2017-06-27    5.15.x  \n" +
                        "  9   Project{name='Apache TomEE'}      2016-05-17    7.0.x   \n" +
                        "  5   Project{name='Apache Tomcat'}     2018-01-17    9.0.x   \n" +
                        " 14   Project{name='Apache ActiveMQ'}   2022-03-09    5.17.x  \n");
    }

    @Test
    public void annotation() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("annotation"),
                " 13   Apache TomEE      1.0.x  \n" +
                        "  7   Apache TomEE      8.0.x  \n" +
                        " 12   Apache TomEE      1.5.x  \n" +
                        "  6   Apache Tomcat     10.0.x \n" +
                        " 18   Apache ActiveMQ   5.13.x \n" +
                        " 10   Apache TomEE      1.7.x  \n" +
                        "  4   Apache Tomcat     8.5.x  \n" +
                        " 11   Apache TomEE      1.6.x  \n" +
                        "  8   Apache TomEE      7.1.x  \n" +
                        "  2   Apache Tomcat     7.0.x  \n" +
                        " 15   Apache ActiveMQ   5.16.x \n" +
                        " 17   Apache ActiveMQ   5.14.x \n" +
                        "  1   Apache Tomcat     6.0.x  \n" +
                        "  3   Apache Tomcat     8.0.x  \n" +
                        " 16   Apache ActiveMQ   5.15.x \n" +
                        "  9   Apache TomEE      7.0.x  \n" +
                        "  5   Apache Tomcat     9.0.x  \n" +
                        " 14   Apache ActiveMQ   5.17.x \n");
    }

    @Test
    public void override() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("override", "--no-table-header"),
                " Branch   1.0.x    13   1.0.x    2012-04-27 \n" +
                        " Branch   8.0.x     7   8.0.x    2019-09-13 \n" +
                        " Branch   1.5.x    12   1.5.x    2012-09-28 \n" +
                        " Branch   10.0.x    6   10.0.x   2021-02-01 \n" +
                        " Branch   5.13.x   18   5.13.x   2015-11-30 \n" +
                        " Branch   1.7.x    10   1.7.x    2014-08-09 \n" +
                        " Branch   8.5.x     4   8.5.x    2016-06-12 \n" +
                        " Branch   1.6.x    11   1.6.x    2013-11-17 \n" +
                        " Branch   7.1.x     8   7.1.x    2018-09-02 \n" +
                        " Branch   7.0.x     2   7.0.x    2011-01-13 \n" +
                        " Branch   5.16.x   15   5.16.x   2020-06-25 \n" +
                        " Branch   5.14.x   17   5.14.x   2016-08-02 \n" +
                        " Branch   6.0.x     1   6.0.x    2007-02-27 \n" +
                        " Branch   8.0.x     3   8.0.x    2014-06-24 \n" +
                        " Branch   5.15.x   16   5.15.x   2017-06-27 \n" +
                        " Branch   7.0.x     9   7.0.x    2016-05-17 \n" +
                        " Branch   9.0.x     5   9.0.x    2018-01-17 \n" +
                        " Branch   5.17.x   14   5.17.x   2022-03-09 \n");
    }

    @Test
    public void overrideBooleanPrimitive() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("override2", "--no-table-header"),
                " Branch   1.0.x    13   1.0.x    2012-04-27 \n" +
                        " Branch   8.0.x     7   8.0.x    2019-09-13 \n" +
                        " Branch   1.5.x    12   1.5.x    2012-09-28 \n" +
                        " Branch   10.0.x    6   10.0.x   2021-02-01 \n" +
                        " Branch   5.13.x   18   5.13.x   2015-11-30 \n" +
                        " Branch   1.7.x    10   1.7.x    2014-08-09 \n" +
                        " Branch   8.5.x     4   8.5.x    2016-06-12 \n" +
                        " Branch   1.6.x    11   1.6.x    2013-11-17 \n" +
                        " Branch   7.1.x     8   7.1.x    2018-09-02 \n" +
                        " Branch   7.0.x     2   7.0.x    2011-01-13 \n" +
                        " Branch   5.16.x   15   5.16.x   2020-06-25 \n" +
                        " Branch   5.14.x   17   5.14.x   2016-08-02 \n" +
                        " Branch   6.0.x     1   6.0.x    2007-02-27 \n" +
                        " Branch   8.0.x     3   8.0.x    2014-06-24 \n" +
                        " Branch   5.15.x   16   5.15.x   2017-06-27 \n" +
                        " Branch   7.0.x     9   7.0.x    2016-05-17 \n" +
                        " Branch   9.0.x     5   9.0.x    2018-01-17 \n" +
                        " Branch   5.17.x   14   5.17.x   2022-03-09 \n");
    }


    private void assertTable(final Object object, final String expected) throws IOException {
        final PrintOutput printOutput = (PrintOutput) object;
        final PrintString output = new PrintString();
        printOutput.write(output);
        assertEquals(expected, output.toString());
    }

    public static class Foo {

        private final List<Branch> branches;

        public Foo() {
            branches = Arrays.asList(
                    new Branch(13, new Project("Apache TomEE"), "1.0.x", "2012-04-27"),
                    new Branch(7, new Project("Apache TomEE"), "8.0.x", "2019-09-13"),
                    new Branch(12, new Project("Apache TomEE"), "1.5.x", "2012-09-28"),
                    new Branch(6, new Project("Apache Tomcat"), "10.0.x", "2021-02-01"),
                    new Branch(18, new Project("Apache ActiveMQ"), "5.13.x", "2015-11-30"),
                    new Branch(10, new Project("Apache TomEE"), "1.7.x", "2014-08-09"),
                    new Branch(4, new Project("Apache Tomcat"), "8.5.x", "2016-06-12"),
                    new Branch(11, new Project("Apache TomEE"), "1.6.x", "2013-11-17"),
                    new Branch(8, new Project("Apache TomEE"), "7.1.x", "2018-09-02"),
                    new Branch(2, new Project("Apache Tomcat"), "7.0.x", "2011-01-13"),
                    new Branch(15, new Project("Apache ActiveMQ"), "5.16.x", "2020-06-25"),
                    new Branch(17, new Project("Apache ActiveMQ"), "5.14.x", "2016-08-02"),
                    new Branch(1, new Project("Apache Tomcat"), "6.0.x", "2007-02-27"),
                    new Branch(3, new Project("Apache Tomcat"), "8.0.x", "2014-06-24"),
                    new Branch(16, new Project("Apache ActiveMQ"), "5.15.x", "2017-06-27"),
                    new Branch(9, new Project("Apache TomEE"), "7.0.x", "2016-05-17"),
                    new Branch(5, new Project("Apache Tomcat"), "9.0.x", "2018-01-17"),
                    new Branch(14, new Project("Apache ActiveMQ"), "5.17.x", "2022-03-09")
            );
        }

        @Table
        @Command
        public List<Branch> alldefaults() {
            return branches;
        }

        @Command
        @Table(fields = "id project.name version", header = false)
        public List<Branch> annotation() {
            return branches;
        }

        /**
         * Boolean wrapper class should work
         */
        @Command
        @Table(fields = "class.simpleName version id version releaseDate")
        public List<Branch> override(@Option("table-header") final Boolean header) {
            return branches;
        }

        /**
         * Boolean primitive should work
         */
        @Command
        @Table(fields = "class.simpleName version id version releaseDate")
        public List<Branch> override2(@Option("table-header") final boolean header) {
            return branches;
        }
    }

    public static class Project {
        private final String name;

        public Project(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Project{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static class Branch {
        private final int id;
        private final Project project;
        private final String version;
        private final LocalDate releaseDate;

        public Branch(final int id, final Project project, final String version, final String releaseDate) {
            this.id = id;
            this.project = project;
            this.version = version;
            this.releaseDate = LocalDate.parse(releaseDate);
        }

        public int getId() {
            return id;
        }

        public Project getProject() {
            return project;
        }

        public String getVersion() {
            return version;
        }

        public LocalDate getReleaseDate() {
            return releaseDate;
        }
    }
}
