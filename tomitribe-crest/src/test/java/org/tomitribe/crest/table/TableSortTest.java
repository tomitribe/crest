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

import org.junit.Ignore;
import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.api.Table;
import org.tomitribe.util.PrintString;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @version $Revision$ $Date$
 */
public class TableSortTest {

    @Test
    public void test() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("alldefaults"),
                "                        class                           id   project   releaseDate   version \n" +
                        "------------------------------------------------------ ---- --------- ------------- ---------\n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   13         2   2012-04-27    1.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    7         2   2019-09-13    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   12         2   2012-09-28    1.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    6         1   2021-02-01    10.0.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   18         3   2015-11-30    5.13.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   10         2   2014-08-09    1.7.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    4         1   2016-06-12    8.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   11         2   2013-11-17    1.6.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    8         2   2018-09-02    7.1.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    2         1   2011-01-13    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   15         3   2020-06-25    5.16.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   17         3   2016-08-02    5.14.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    1         1   2007-02-27    6.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    3         1   2014-06-24    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   16         3   2017-06-27    5.15.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    9         2   2016-05-17    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    5         1   2018-01-17    9.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   14         3   2022-03-09    5.17.x  \n");
    }

    @Test
    public void testSortViaTable() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("sortProject"),
                "                        class                           id   project   releaseDate   version \n" +
                        "------------------------------------------------------ ---- --------- ------------- ---------\n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    6         1   2021-02-01    10.0.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    4         1   2016-06-12    8.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    2         1   2011-01-13    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    1         1   2007-02-27    6.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    3         1   2014-06-24    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    5         1   2018-01-17    9.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   13         2   2012-04-27    1.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    7         2   2019-09-13    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   12         2   2012-09-28    1.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   10         2   2014-08-09    1.7.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   11         2   2013-11-17    1.6.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    8         2   2018-09-02    7.1.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    9         2   2016-05-17    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   18         3   2015-11-30    5.13.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   15         3   2020-06-25    5.16.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   17         3   2016-08-02    5.14.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   16         3   2017-06-27    5.15.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   14         3   2022-03-09    5.17.x  \n");
    }

    @Test
    public void testMultipleSortViaTable() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("sortProjectAndReleaseDate"),
                "                        class                           id   project   releaseDate   version \n" +
                        "------------------------------------------------------ ---- --------- ------------- ---------\n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    1         1   2007-02-27    6.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    2         1   2011-01-13    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    3         1   2014-06-24    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    4         1   2016-06-12    8.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    5         1   2018-01-17    9.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    6         1   2021-02-01    10.0.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   13         2   2012-04-27    1.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   12         2   2012-09-28    1.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   11         2   2013-11-17    1.6.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   10         2   2014-08-09    1.7.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    9         2   2016-05-17    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    8         2   2018-09-02    7.1.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    7         2   2019-09-13    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   18         3   2015-11-30    5.13.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   17         3   2016-08-02    5.14.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   16         3   2017-06-27    5.15.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   15         3   2020-06-25    5.16.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   14         3   2022-03-09    5.17.x  \n");
    }

    @Test
    public void sortOverride() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("sortOverride", "--table-sort=releaseDate"),
                "                        class                           id   project   releaseDate   version \n" +
                        "------------------------------------------------------ ---- --------- ------------- ---------\n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    1         1   2007-02-27    6.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    2         1   2011-01-13    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   13         2   2012-04-27    1.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   12         2   2012-09-28    1.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   11         2   2013-11-17    1.6.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    3         1   2014-06-24    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   10         2   2014-08-09    1.7.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   18         3   2015-11-30    5.13.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    9         2   2016-05-17    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    4         1   2016-06-12    8.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   17         3   2016-08-02    5.14.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   16         3   2017-06-27    5.15.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    5         1   2018-01-17    9.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    8         2   2018-09-02    7.1.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    7         2   2019-09-13    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   15         3   2020-06-25    5.16.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    6         1   2021-02-01    10.0.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   14         3   2022-03-09    5.17.x  \n");
    }

    /**
     * We should be able to sort by any field, regardless if it will
     * show up in the final table
     */
    @Test
    @Ignore
    public void sortByNonVisibleField() throws Exception {

        final Main main = new Main(Foo.class);
        assertTable(main.exec("sortByNonVisibleField"),
                "                        class                           id   project   releaseDate   version \n" +
                        "------------------------------------------------------ ---- --------- ------------- ---------\n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    1         1   2007-02-27    6.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    2         1   2011-01-13    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   13         2   2012-04-27    1.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   12         2   2012-09-28    1.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   11         2   2013-11-17    1.6.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    3         1   2014-06-24    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   10         2   2014-08-09    1.7.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   18         3   2015-11-30    5.13.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    9         2   2016-05-17    7.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    4         1   2016-06-12    8.5.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   17         3   2016-08-02    5.14.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   16         3   2017-06-27    5.15.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    5         1   2018-01-17    9.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    8         2   2018-09-02    7.1.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    7         2   2019-09-13    8.0.x   \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   15         3   2020-06-25    5.16.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch    6         1   2021-02-01    10.0.x  \n" +
                        " class org.tomitribe.crest.table.TableSortTest$Branch   14         3   2022-03-09    5.17.x  \n");
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
                    new Branch(13, "2", "1.0.x", "2012-04-27"),
                    new Branch(7, "2", "8.0.x", "2019-09-13"),
                    new Branch(12, "2", "1.5.x", "2012-09-28"),
                    new Branch(6, "1", "10.0.x", "2021-02-01"),
                    new Branch(18, "3", "5.13.x", "2015-11-30"),
                    new Branch(10, "2", "1.7.x", "2014-08-09"),
                    new Branch(4, "1", "8.5.x", "2016-06-12"),
                    new Branch(11, "2", "1.6.x", "2013-11-17"),
                    new Branch(8, "2", "7.1.x", "2018-09-02"),
                    new Branch(2, "1", "7.0.x", "2011-01-13"),
                    new Branch(15, "3", "5.16.x", "2020-06-25"),
                    new Branch(17, "3", "5.14.x", "2016-08-02"),
                    new Branch(1, "1", "6.0.x", "2007-02-27"),
                    new Branch(3, "1", "8.0.x", "2014-06-24"),
                    new Branch(16, "3", "5.15.x", "2017-06-27"),
                    new Branch(9, "2", "7.0.x", "2016-05-17"),
                    new Branch(5, "1", "9.0.x", "2018-01-17"),
                    new Branch(14, "3", "5.17.x", "2022-03-09")
            );
        }

        @Table
        @Command
        public List<Branch> alldefaults() {
            return branches;
        }

        @Command
        @Table(sort = "project")
        public List<Branch> sortProject() {
            return branches;
        }

        @Command
        @Table(sort = "project releaseDate")
        public List<Branch> sortProjectAndReleaseDate() {
            return branches;
        }

        @Command
        @Table(sort = "releaseDate", fields = "class id project version")
        public List<Branch> sortByNonVisibleField() {
            return branches;
        }

        @Command
        @Table
        public List<Branch> sortOverride(@Option("table-sort") final String sort) {
            return branches;
        }
    }

    public static class Branch {
        private final int id;
        private final String project;
        private final String version;
        private final LocalDate releaseDate;

        public Branch(final int id, final String project, final String version, final String releaseDate) {
            this.id = id;
            this.project = project;
            this.version = version;
            this.releaseDate = LocalDate.parse(releaseDate);
        }

        public int getId() {
            return id;
        }

        public String getProject() {
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
