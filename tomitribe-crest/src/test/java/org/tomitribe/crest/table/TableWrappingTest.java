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
package org.tomitribe.crest.table;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * Tests the formatting of our out-of-the-box table formats
 */
public class TableWrappingTest {

    private final Data data = Data.builder().headings(true)
            .row("id", "first_name", "last_name", "email", "slogan", "sentence")

            .row("1", "Wendie", "Marquet", "wmarquet0@blogspot.com", "unleash mission-critical experiences. unleash mission-critical experiences",
                    "Suspendisse ornare consequat lectus. In est risus, auctor sed, tristique in, tempus sit amet, sem.")

            .row("2", "Derry", "Henkmann", "dhenkmann1@cdbaby.com", "innovate seamless e-services",
                    "Sed vel enim sit amet nunc viverra dapibus. Nulla suscipit ligula in lacus. Curabitur at ipsum ac tellus semper interdum.")

            .row("3", "Heidi", "Bointon", "hbointon2@bloglovin.com", "visualize real-time architectures. visualize real-time architectures",
                    "Vivamus vel nulla eget eros elementum pellentesque. Quisque porta volutpat erat. Quisque erat eros, " +
                            "viverra eget, congue eget, semper rutrum, nulla.")

            .row("4", "Elladine", "Twelve", "etwelve3@friendfeed.com", "scale global platforms", "Integer non velit. " +
                    "Donec diam neque, vestibulum eget, vulputate ut, ultrices vel, augue. Vestibulum ante ipsum primis " +
                    "in faucibus orci luctus et ultrices posuere cubilia Curae; Donec pharetra, magna vestibulum aliquet " +
                    "ultrices, erat tortor sollicitudin mi, sit amet lobortis sapien sapien non mi. Integer ac neque. " +
                    "Duis bibendum. Morbi non quam nec dui luctus rutrum. Nulla tellus. In sagittis dui vel nisl. Duis ac " +
                    "nibh. Fusce lacus purus, aliquet at, feugiat non, pretium quis, lectus.")

            .row("5", "Erl", "Mellmer", "emellmer4@about.com", "harness cross-media infomediaries",
                    "Suspendisse potenti. In eleifend quam a odio. In hac habitasse platea dictumst. Maecenas ut" +
                            " massa quis augue luctus tincidunt.")

            .build();

    @Test
    public void asciiCompact() {
        assertTable(Border::asciiCompact, "" +
                " id   first_name   last_name            email                                slogan                                        sentence                   \n" +
                "---- ------------ ----------- ------------------------- ------------------------------------------------ ---------------------------------------------\n" +
                "  1   Wendie       Marquet     wmarquet0@blogspot.com    unleash mission-critical experiences.  unleash   Suspendisse ornare consequat lectus. In est \n" +
                "                                                         mission-critical experiences                     risus, auctor sed, tristique in, tempus sit \n" +
                "                                                                                                          amet, sem.                                  \n" +
                "  2   Derry        Henkmann    dhenkmann1@cdbaby.com     innovate seamless e-services                     Sed vel enim sit amet nunc viverra dapibus. \n" +
                "                                                                                                          Nulla suscipit ligula in lacus.   Curabitur \n" +
                "                                                                                                          at ipsum ac tellus semper interdum.         \n" +
                "  3   Heidi        Bointon     hbointon2@bloglovin.com   visualize  real-time architectures.  visualize   Vivamus vel nulla eget eros elementum       \n" +
                "                                                         real-time architectures                          pellentesque.  Quisque porta volutpat erat. \n" +
                "                                                                                                          Quisque  erat  eros,  viverra  eget, congue \n" +
                "                                                                                                          eget, semper rutrum, nulla.                 \n" +
                "  4   Elladine     Twelve      etwelve3@friendfeed.com   scale global platforms                           Integer  non  velit.    Donec  diam  neque, \n" +
                "                                                                                                          vestibulum  eget,  vulputate  ut,  ultrices \n" +
                "                                                                                                          vel, augue. Vestibulum ante ipsum primis in \n" +
                "                                                                                                          faucibus orci luctus  et  ultrices  posuere \n" +
                "                                                                                                          cubilia Curae; Donec pharetra, magna        \n" +
                "                                                                                                          vestibulum aliquet  ultrices,  erat  tortor \n" +
                "                                                                                                          sollicitudin  mi,  sit amet lobortis sapien \n" +
                "                                                                                                          sapien non mi.  Integer  ac  neque.    Duis \n" +
                "                                                                                                          bibendum.    Morbi  non quam nec dui luctus \n" +
                "                                                                                                          rutrum.  Nulla tellus.  In sagittis dui vel \n" +
                "                                                                                                          nisl.    Duis  ac nibh.  Fusce lacus purus, \n" +
                "                                                                                                          aliquet  at,  feugiat  non,  pretium  quis, \n" +
                "                                                                                                          lectus.                                     \n" +
                "  5   Erl          Mellmer     emellmer4@about.com       harness cross-media infomediaries                Suspendisse  potenti.    In eleifend quam a \n" +
                "                                                                                                          odio.  In hac  habitasse  platea  dictumst. \n" +
                "                                                                                                          Maecenas ut massa quis augue luctus         \n" +
                "                                                                                                          tincidunt.                                  \n");
    }

    @Test
    public void asciiDots() {
        assertTable(Border::asciiDots, "" +
                "......................................................................................................................................................\n" +
                ": id : first_name : last_name :          email          :                    slogan                     :                  sentence                  :\n" +
                ":....:............:...........:.........................:...............................................:............................................:\n" +
                ":  1 : Wendie     : Marquet   : wmarquet0@blogspot.com  : unleash mission-critical experiences. unleash : Suspendisse  ornare  consequat lectus.  In :\n" +
                ":    :            :           :                         : mission-critical experiences                  : est risus, auctor sed, tristique in,       :\n" +
                ":    :            :           :                         :                                               : tempus sit amet, sem.                      :\n" +
                ":  2 : Derry      : Henkmann  : dhenkmann1@cdbaby.com   : innovate seamless e-services                  : Sed vel enim sit amet nunc viverra         :\n" +
                ":    :            :           :                         :                                               : dapibus.  Nulla suscipit ligula in  lacus. :\n" +
                ":    :            :           :                         :                                               : Curabitur at ipsum ac tellus semper        :\n" +
                ":    :            :           :                         :                                               : interdum.                                  :\n" +
                ":  3 : Heidi      : Bointon   : hbointon2@bloglovin.com : visualize real-time architectures.  visualize : Vivamus  vel  nulla  eget  eros  elementum :\n" +
                ":    :            :           :                         : real-time architectures                       : pellentesque. Quisque porta volutpat erat. :\n" +
                ":    :            :           :                         :                                               : Quisque  erat  eros,  viverra eget, congue :\n" +
                ":    :            :           :                         :                                               : eget, semper rutrum, nulla.                :\n" +
                ":  4 : Elladine   : Twelve    : etwelve3@friendfeed.com : scale global platforms                        : Integer  non  velit.    Donec  diam neque, :\n" +
                ":    :            :           :                         :                                               : vestibulum eget,  vulputate  ut,  ultrices :\n" +
                ":    :            :           :                         :                                               : vel,  augue.  Vestibulum ante ipsum primis :\n" +
                ":    :            :           :                         :                                               : in faucibus orci luctus et ultrices        :\n" +
                ":    :            :           :                         :                                               : posuere  cubilia  Curae;  Donec  pharetra, :\n" +
                ":    :            :           :                         :                                               : magna vestibulum  aliquet  ultrices,  erat :\n" +
                ":    :            :           :                         :                                               : tortor  sollicitudin mi, sit amet lobortis :\n" +
                ":    :            :           :                         :                                               : sapien sapien non mi.  Integer  ac  neque. :\n" +
                ":    :            :           :                         :                                               : Duis  bibendum.    Morbi  non quam nec dui :\n" +
                ":    :            :           :                         :                                               : luctus rutrum.  Nulla tellus.  In sagittis :\n" +
                ":    :            :           :                         :                                               : dui  vel nisl.  Duis ac nibh.  Fusce lacus :\n" +
                ":    :            :           :                         :                                               : purus, aliquet at,  feugiat  non,  pretium :\n" +
                ":    :            :           :                         :                                               : quis, lectus.                              :\n" +
                ":  5 : Erl        : Mellmer   : emellmer4@about.com     : harness cross-media infomediaries             : Suspendisse  potenti.   In eleifend quam a :\n" +
                ":    :            :           :                         :                                               : odio.  In hac habitasse  platea  dictumst. :\n" +
                ":    :            :           :                         :                                               : Maecenas ut massa quis augue luctus        :\n" +
                ":    :            :           :                         :                                               : tincidunt.                                 :\n" +
                ":....:............:...........:.........................:...............................................:............................................:\n");
    }

    @Test
    public void asciiSeparated() {
        assertTable(Border::asciiSeparated, "" +
                "+====+============+===========+=========================+===============================================+============================================+\n" +
                "| id | first_name | last_name |          email          |                    slogan                     |                  sentence                  |\n" +
                "+====+============+===========+=========================+===============================================+============================================+\n" +
                "|  1 | Wendie     | Marquet   | wmarquet0@blogspot.com  | unleash mission-critical experiences. unleash | Suspendisse  ornare  consequat lectus.  In |\n" +
                "|    |            |           |                         | mission-critical experiences                  | est risus, auctor sed, tristique in,       |\n" +
                "|    |            |           |                         |                                               | tempus sit amet, sem.                      |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "|  2 | Derry      | Henkmann  | dhenkmann1@cdbaby.com   | innovate seamless e-services                  | Sed vel enim sit amet nunc viverra         |\n" +
                "|    |            |           |                         |                                               | dapibus.  Nulla suscipit ligula in  lacus. |\n" +
                "|    |            |           |                         |                                               | Curabitur at ipsum ac tellus semper        |\n" +
                "|    |            |           |                         |                                               | interdum.                                  |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "|  3 | Heidi      | Bointon   | hbointon2@bloglovin.com | visualize real-time architectures.  visualize | Vivamus  vel  nulla  eget  eros  elementum |\n" +
                "|    |            |           |                         | real-time architectures                       | pellentesque. Quisque porta volutpat erat. |\n" +
                "|    |            |           |                         |                                               | Quisque  erat  eros,  viverra eget, congue |\n" +
                "|    |            |           |                         |                                               | eget, semper rutrum, nulla.                |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "|  4 | Elladine   | Twelve    | etwelve3@friendfeed.com | scale global platforms                        | Integer  non  velit.    Donec  diam neque, |\n" +
                "|    |            |           |                         |                                               | vestibulum eget,  vulputate  ut,  ultrices |\n" +
                "|    |            |           |                         |                                               | vel,  augue.  Vestibulum ante ipsum primis |\n" +
                "|    |            |           |                         |                                               | in faucibus orci luctus et ultrices        |\n" +
                "|    |            |           |                         |                                               | posuere  cubilia  Curae;  Donec  pharetra, |\n" +
                "|    |            |           |                         |                                               | magna vestibulum  aliquet  ultrices,  erat |\n" +
                "|    |            |           |                         |                                               | tortor  sollicitudin mi, sit amet lobortis |\n" +
                "|    |            |           |                         |                                               | sapien sapien non mi.  Integer  ac  neque. |\n" +
                "|    |            |           |                         |                                               | Duis  bibendum.    Morbi  non quam nec dui |\n" +
                "|    |            |           |                         |                                               | luctus rutrum.  Nulla tellus.  In sagittis |\n" +
                "|    |            |           |                         |                                               | dui  vel nisl.  Duis ac nibh.  Fusce lacus |\n" +
                "|    |            |           |                         |                                               | purus, aliquet at,  feugiat  non,  pretium |\n" +
                "|    |            |           |                         |                                               | quis, lectus.                              |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "|  5 | Erl        | Mellmer   | emellmer4@about.com     | harness cross-media infomediaries             | Suspendisse  potenti.   In eleifend quam a |\n" +
                "|    |            |           |                         |                                               | odio.  In hac habitasse  platea  dictumst. |\n" +
                "|    |            |           |                         |                                               | Maecenas ut massa quis augue luctus        |\n" +
                "|    |            |           |                         |                                               | tincidunt.                                 |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n");
    }

    @Test
    public void mysqlStyle() {
        assertTable(Border::mysqlStyle, "" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "| id | first_name | last_name |          email          |                    slogan                     |                  sentence                  |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "|  1 | Wendie     | Marquet   | wmarquet0@blogspot.com  | unleash mission-critical experiences. unleash | Suspendisse  ornare  consequat lectus.  In |\n" +
                "|    |            |           |                         | mission-critical experiences                  | est risus, auctor sed, tristique in,       |\n" +
                "|    |            |           |                         |                                               | tempus sit amet, sem.                      |\n" +
                "|  2 | Derry      | Henkmann  | dhenkmann1@cdbaby.com   | innovate seamless e-services                  | Sed vel enim sit amet nunc viverra         |\n" +
                "|    |            |           |                         |                                               | dapibus.  Nulla suscipit ligula in  lacus. |\n" +
                "|    |            |           |                         |                                               | Curabitur at ipsum ac tellus semper        |\n" +
                "|    |            |           |                         |                                               | interdum.                                  |\n" +
                "|  3 | Heidi      | Bointon   | hbointon2@bloglovin.com | visualize real-time architectures.  visualize | Vivamus  vel  nulla  eget  eros  elementum |\n" +
                "|    |            |           |                         | real-time architectures                       | pellentesque. Quisque porta volutpat erat. |\n" +
                "|    |            |           |                         |                                               | Quisque  erat  eros,  viverra eget, congue |\n" +
                "|    |            |           |                         |                                               | eget, semper rutrum, nulla.                |\n" +
                "|  4 | Elladine   | Twelve    | etwelve3@friendfeed.com | scale global platforms                        | Integer  non  velit.    Donec  diam neque, |\n" +
                "|    |            |           |                         |                                               | vestibulum eget,  vulputate  ut,  ultrices |\n" +
                "|    |            |           |                         |                                               | vel,  augue.  Vestibulum ante ipsum primis |\n" +
                "|    |            |           |                         |                                               | in faucibus orci luctus et ultrices        |\n" +
                "|    |            |           |                         |                                               | posuere  cubilia  Curae;  Donec  pharetra, |\n" +
                "|    |            |           |                         |                                               | magna vestibulum  aliquet  ultrices,  erat |\n" +
                "|    |            |           |                         |                                               | tortor  sollicitudin mi, sit amet lobortis |\n" +
                "|    |            |           |                         |                                               | sapien sapien non mi.  Integer  ac  neque. |\n" +
                "|    |            |           |                         |                                               | Duis  bibendum.    Morbi  non quam nec dui |\n" +
                "|    |            |           |                         |                                               | luctus rutrum.  Nulla tellus.  In sagittis |\n" +
                "|    |            |           |                         |                                               | dui  vel nisl.  Duis ac nibh.  Fusce lacus |\n" +
                "|    |            |           |                         |                                               | purus, aliquet at,  feugiat  non,  pretium |\n" +
                "|    |            |           |                         |                                               | quis, lectus.                              |\n" +
                "|  5 | Erl        | Mellmer   | emellmer4@about.com     | harness cross-media infomediaries             | Suspendisse  potenti.   In eleifend quam a |\n" +
                "|    |            |           |                         |                                               | odio.  In hac habitasse  platea  dictumst. |\n" +
                "|    |            |           |                         |                                               | Maecenas ut massa quis augue luctus        |\n" +
                "|    |            |           |                         |                                               | tincidunt.                                 |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n");
    }

    @Test
    public void githubMarkdown() {
        assertTable(Border::githubMarkdown, "" +
                "| id | first_name | last_name |          email          |                    slogan                     |                  sentence                  |\n" +
                "|----|------------|-----------|-------------------------|-----------------------------------------------|--------------------------------------------|\n" +
                "|  1 | Wendie     | Marquet   | wmarquet0@blogspot.com  | unleash mission-critical experiences. unleash | Suspendisse  ornare  consequat lectus.  In |\n" +
                "|    |            |           |                         | mission-critical experiences                  | est risus, auctor sed, tristique in,       |\n" +
                "|    |            |           |                         |                                               | tempus sit amet, sem.                      |\n" +
                "|  2 | Derry      | Henkmann  | dhenkmann1@cdbaby.com   | innovate seamless e-services                  | Sed vel enim sit amet nunc viverra         |\n" +
                "|    |            |           |                         |                                               | dapibus.  Nulla suscipit ligula in  lacus. |\n" +
                "|    |            |           |                         |                                               | Curabitur at ipsum ac tellus semper        |\n" +
                "|    |            |           |                         |                                               | interdum.                                  |\n" +
                "|  3 | Heidi      | Bointon   | hbointon2@bloglovin.com | visualize real-time architectures.  visualize | Vivamus  vel  nulla  eget  eros  elementum |\n" +
                "|    |            |           |                         | real-time architectures                       | pellentesque. Quisque porta volutpat erat. |\n" +
                "|    |            |           |                         |                                               | Quisque  erat  eros,  viverra eget, congue |\n" +
                "|    |            |           |                         |                                               | eget, semper rutrum, nulla.                |\n" +
                "|  4 | Elladine   | Twelve    | etwelve3@friendfeed.com | scale global platforms                        | Integer  non  velit.    Donec  diam neque, |\n" +
                "|    |            |           |                         |                                               | vestibulum eget,  vulputate  ut,  ultrices |\n" +
                "|    |            |           |                         |                                               | vel,  augue.  Vestibulum ante ipsum primis |\n" +
                "|    |            |           |                         |                                               | in faucibus orci luctus et ultrices        |\n" +
                "|    |            |           |                         |                                               | posuere  cubilia  Curae;  Donec  pharetra, |\n" +
                "|    |            |           |                         |                                               | magna vestibulum  aliquet  ultrices,  erat |\n" +
                "|    |            |           |                         |                                               | tortor  sollicitudin mi, sit amet lobortis |\n" +
                "|    |            |           |                         |                                               | sapien sapien non mi.  Integer  ac  neque. |\n" +
                "|    |            |           |                         |                                               | Duis  bibendum.    Morbi  non quam nec dui |\n" +
                "|    |            |           |                         |                                               | luctus rutrum.  Nulla tellus.  In sagittis |\n" +
                "|    |            |           |                         |                                               | dui  vel nisl.  Duis ac nibh.  Fusce lacus |\n" +
                "|    |            |           |                         |                                               | purus, aliquet at,  feugiat  non,  pretium |\n" +
                "|    |            |           |                         |                                               | quis, lectus.                              |\n" +
                "|  5 | Erl        | Mellmer   | emellmer4@about.com     | harness cross-media infomediaries             | Suspendisse  potenti.   In eleifend quam a |\n" +
                "|    |            |           |                         |                                               | odio.  In hac habitasse  platea  dictumst. |\n" +
                "|    |            |           |                         |                                               | Maecenas ut massa quis augue luctus        |\n" +
                "|    |            |           |                         |                                               | tincidunt.                                 |\n");
    }

    @Test
    public void redditMarkdown() {
        assertTable(Border::redditMarkdown, "" +
                " id | first_name | last_name |          email          |                     slogan                     |                  sentence                   \n" +
                "----|------------|-----------|-------------------------|------------------------------------------------|---------------------------------------------\n" +
                "  1 | Wendie     | Marquet   | wmarquet0@blogspot.com  | unleash mission-critical experiences.  unleash | Suspendisse ornare consequat lectus. In est \n" +
                "    |            |           |                         | mission-critical experiences                   | risus, auctor sed, tristique in, tempus sit \n" +
                "    |            |           |                         |                                                | amet, sem.                                  \n" +
                "  2 | Derry      | Henkmann  | dhenkmann1@cdbaby.com   | innovate seamless e-services                   | Sed vel enim sit amet nunc viverra dapibus. \n" +
                "    |            |           |                         |                                                | Nulla suscipit ligula in lacus.   Curabitur \n" +
                "    |            |           |                         |                                                | at ipsum ac tellus semper interdum.         \n" +
                "  3 | Heidi      | Bointon   | hbointon2@bloglovin.com | visualize  real-time architectures.  visualize | Vivamus vel nulla eget eros elementum       \n" +
                "    |            |           |                         | real-time architectures                        | pellentesque.  Quisque porta volutpat erat. \n" +
                "    |            |           |                         |                                                | Quisque  erat  eros,  viverra  eget, congue \n" +
                "    |            |           |                         |                                                | eget, semper rutrum, nulla.                 \n" +
                "  4 | Elladine   | Twelve    | etwelve3@friendfeed.com | scale global platforms                         | Integer  non  velit.    Donec  diam  neque, \n" +
                "    |            |           |                         |                                                | vestibulum  eget,  vulputate  ut,  ultrices \n" +
                "    |            |           |                         |                                                | vel, augue. Vestibulum ante ipsum primis in \n" +
                "    |            |           |                         |                                                | faucibus orci luctus  et  ultrices  posuere \n" +
                "    |            |           |                         |                                                | cubilia Curae; Donec pharetra, magna        \n" +
                "    |            |           |                         |                                                | vestibulum aliquet  ultrices,  erat  tortor \n" +
                "    |            |           |                         |                                                | sollicitudin  mi,  sit amet lobortis sapien \n" +
                "    |            |           |                         |                                                | sapien non mi.  Integer  ac  neque.    Duis \n" +
                "    |            |           |                         |                                                | bibendum.    Morbi  non quam nec dui luctus \n" +
                "    |            |           |                         |                                                | rutrum.  Nulla tellus.  In sagittis dui vel \n" +
                "    |            |           |                         |                                                | nisl.    Duis  ac nibh.  Fusce lacus purus, \n" +
                "    |            |           |                         |                                                | aliquet  at,  feugiat  non,  pretium  quis, \n" +
                "    |            |           |                         |                                                | lectus.                                     \n" +
                "  5 | Erl        | Mellmer   | emellmer4@about.com     | harness cross-media infomediaries              | Suspendisse  potenti.    In eleifend quam a \n" +
                "    |            |           |                         |                                                | odio.  In hac  habitasse  platea  dictumst. \n" +
                "    |            |           |                         |                                                | Maecenas ut massa quis augue luctus         \n" +
                "    |            |           |                         |                                                | tincidunt.                                  \n");
    }

    @Test
    public void reStructuredTextGrid() {
        assertTable(Border::reStructuredTextGrid, "" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n" +
                "| id | first_name | last_name |          email          |                    slogan                     |                  sentence                  |\n" +
                "+====+============+===========+=========================+===============================================+============================================+\n" +
                "|  1 | Wendie     | Marquet   | wmarquet0@blogspot.com  | unleash mission-critical experiences. unleash | Suspendisse  ornare  consequat lectus.  In |\n" +
                "|    |            |           |                         | mission-critical experiences                  | est risus, auctor sed, tristique in,       |\n" +
                "|    |            |           |                         |                                               | tempus sit amet, sem.                      |\n" +
                "|  2 | Derry      | Henkmann  | dhenkmann1@cdbaby.com   | innovate seamless e-services                  | Sed vel enim sit amet nunc viverra         |\n" +
                "|    |            |           |                         |                                               | dapibus.  Nulla suscipit ligula in  lacus. |\n" +
                "|    |            |           |                         |                                               | Curabitur at ipsum ac tellus semper        |\n" +
                "|    |            |           |                         |                                               | interdum.                                  |\n" +
                "|  3 | Heidi      | Bointon   | hbointon2@bloglovin.com | visualize real-time architectures.  visualize | Vivamus  vel  nulla  eget  eros  elementum |\n" +
                "|    |            |           |                         | real-time architectures                       | pellentesque. Quisque porta volutpat erat. |\n" +
                "|    |            |           |                         |                                               | Quisque  erat  eros,  viverra eget, congue |\n" +
                "|    |            |           |                         |                                               | eget, semper rutrum, nulla.                |\n" +
                "|  4 | Elladine   | Twelve    | etwelve3@friendfeed.com | scale global platforms                        | Integer  non  velit.    Donec  diam neque, |\n" +
                "|    |            |           |                         |                                               | vestibulum eget,  vulputate  ut,  ultrices |\n" +
                "|    |            |           |                         |                                               | vel,  augue.  Vestibulum ante ipsum primis |\n" +
                "|    |            |           |                         |                                               | in faucibus orci luctus et ultrices        |\n" +
                "|    |            |           |                         |                                               | posuere  cubilia  Curae;  Donec  pharetra, |\n" +
                "|    |            |           |                         |                                               | magna vestibulum  aliquet  ultrices,  erat |\n" +
                "|    |            |           |                         |                                               | tortor  sollicitudin mi, sit amet lobortis |\n" +
                "|    |            |           |                         |                                               | sapien sapien non mi.  Integer  ac  neque. |\n" +
                "|    |            |           |                         |                                               | Duis  bibendum.    Morbi  non quam nec dui |\n" +
                "|    |            |           |                         |                                               | luctus rutrum.  Nulla tellus.  In sagittis |\n" +
                "|    |            |           |                         |                                               | dui  vel nisl.  Duis ac nibh.  Fusce lacus |\n" +
                "|    |            |           |                         |                                               | purus, aliquet at,  feugiat  non,  pretium |\n" +
                "|    |            |           |                         |                                               | quis, lectus.                              |\n" +
                "|  5 | Erl        | Mellmer   | emellmer4@about.com     | harness cross-media infomediaries             | Suspendisse  potenti.   In eleifend quam a |\n" +
                "|    |            |           |                         |                                               | odio.  In hac habitasse  platea  dictumst. |\n" +
                "|    |            |           |                         |                                               | Maecenas ut massa quis augue luctus        |\n" +
                "|    |            |           |                         |                                               | tincidunt.                                 |\n" +
                "+----+------------+-----------+-------------------------+-----------------------------------------------+--------------------------------------------+\n");
    }

    @Test
    public void reStructuredTextSimple() {
        assertTable(Border::reStructuredTextSimple, "" +
                "==== ============ =========== ========================= ================================================ =============================================\n" +
                " id   first_name   last_name            email                                slogan                                        sentence                   \n" +
                "==== ============ =========== ========================= ================================================ =============================================\n" +
                "  1   Wendie       Marquet     wmarquet0@blogspot.com    unleash mission-critical experiences.  unleash   Suspendisse ornare consequat lectus. In est \n" +
                "                                                         mission-critical experiences                     risus, auctor sed, tristique in, tempus sit \n" +
                "                                                                                                          amet, sem.                                  \n" +
                "  2   Derry        Henkmann    dhenkmann1@cdbaby.com     innovate seamless e-services                     Sed vel enim sit amet nunc viverra dapibus. \n" +
                "                                                                                                          Nulla suscipit ligula in lacus.   Curabitur \n" +
                "                                                                                                          at ipsum ac tellus semper interdum.         \n" +
                "  3   Heidi        Bointon     hbointon2@bloglovin.com   visualize  real-time architectures.  visualize   Vivamus vel nulla eget eros elementum       \n" +
                "                                                         real-time architectures                          pellentesque.  Quisque porta volutpat erat. \n" +
                "                                                                                                          Quisque  erat  eros,  viverra  eget, congue \n" +
                "                                                                                                          eget, semper rutrum, nulla.                 \n" +
                "  4   Elladine     Twelve      etwelve3@friendfeed.com   scale global platforms                           Integer  non  velit.    Donec  diam  neque, \n" +
                "                                                                                                          vestibulum  eget,  vulputate  ut,  ultrices \n" +
                "                                                                                                          vel, augue. Vestibulum ante ipsum primis in \n" +
                "                                                                                                          faucibus orci luctus  et  ultrices  posuere \n" +
                "                                                                                                          cubilia Curae; Donec pharetra, magna        \n" +
                "                                                                                                          vestibulum aliquet  ultrices,  erat  tortor \n" +
                "                                                                                                          sollicitudin  mi,  sit amet lobortis sapien \n" +
                "                                                                                                          sapien non mi.  Integer  ac  neque.    Duis \n" +
                "                                                                                                          bibendum.    Morbi  non quam nec dui luctus \n" +
                "                                                                                                          rutrum.  Nulla tellus.  In sagittis dui vel \n" +
                "                                                                                                          nisl.    Duis  ac nibh.  Fusce lacus purus, \n" +
                "                                                                                                          aliquet  at,  feugiat  non,  pretium  quis, \n" +
                "                                                                                                          lectus.                                     \n" +
                "  5   Erl          Mellmer     emellmer4@about.com       harness cross-media infomediaries                Suspendisse  potenti.    In eleifend quam a \n" +
                "                                                                                                          odio.  In hac  habitasse  platea  dictumst. \n" +
                "                                                                                                          Maecenas ut massa quis augue luctus         \n" +
                "                                                                                                          tincidunt.                                  \n" +
                "==== ============ =========== ========================= ================================================ =============================================\n");
    }

    @Test
    public void unicodeDouble() {
        assertTable(Border::unicodeDouble, "" +
                "╔════╦════════════╦═══════════╦═════════════════════════╦═══════════════════════════════════════════════╦════════════════════════════════════════════╗\n" +
                "║ id ║ first_name ║ last_name ║          email          ║                    slogan                     ║                  sentence                  ║\n" +
                "╠════╬════════════╬═══════════╬═════════════════════════╬═══════════════════════════════════════════════╬════════════════════════════════════════════╣\n" +
                "║  1 ║ Wendie     ║ Marquet   ║ wmarquet0@blogspot.com  ║ unleash mission-critical experiences. unleash ║ Suspendisse  ornare  consequat lectus.  In ║\n" +
                "║    ║            ║           ║                         ║ mission-critical experiences                  ║ est risus, auctor sed, tristique in,       ║\n" +
                "║    ║            ║           ║                         ║                                               ║ tempus sit amet, sem.                      ║\n" +
                "║  2 ║ Derry      ║ Henkmann  ║ dhenkmann1@cdbaby.com   ║ innovate seamless e-services                  ║ Sed vel enim sit amet nunc viverra         ║\n" +
                "║    ║            ║           ║                         ║                                               ║ dapibus.  Nulla suscipit ligula in  lacus. ║\n" +
                "║    ║            ║           ║                         ║                                               ║ Curabitur at ipsum ac tellus semper        ║\n" +
                "║    ║            ║           ║                         ║                                               ║ interdum.                                  ║\n" +
                "║  3 ║ Heidi      ║ Bointon   ║ hbointon2@bloglovin.com ║ visualize real-time architectures.  visualize ║ Vivamus  vel  nulla  eget  eros  elementum ║\n" +
                "║    ║            ║           ║                         ║ real-time architectures                       ║ pellentesque. Quisque porta volutpat erat. ║\n" +
                "║    ║            ║           ║                         ║                                               ║ Quisque  erat  eros,  viverra eget, congue ║\n" +
                "║    ║            ║           ║                         ║                                               ║ eget, semper rutrum, nulla.                ║\n" +
                "║  4 ║ Elladine   ║ Twelve    ║ etwelve3@friendfeed.com ║ scale global platforms                        ║ Integer  non  velit.    Donec  diam neque, ║\n" +
                "║    ║            ║           ║                         ║                                               ║ vestibulum eget,  vulputate  ut,  ultrices ║\n" +
                "║    ║            ║           ║                         ║                                               ║ vel,  augue.  Vestibulum ante ipsum primis ║\n" +
                "║    ║            ║           ║                         ║                                               ║ in faucibus orci luctus et ultrices        ║\n" +
                "║    ║            ║           ║                         ║                                               ║ posuere  cubilia  Curae;  Donec  pharetra, ║\n" +
                "║    ║            ║           ║                         ║                                               ║ magna vestibulum  aliquet  ultrices,  erat ║\n" +
                "║    ║            ║           ║                         ║                                               ║ tortor  sollicitudin mi, sit amet lobortis ║\n" +
                "║    ║            ║           ║                         ║                                               ║ sapien sapien non mi.  Integer  ac  neque. ║\n" +
                "║    ║            ║           ║                         ║                                               ║ Duis  bibendum.    Morbi  non quam nec dui ║\n" +
                "║    ║            ║           ║                         ║                                               ║ luctus rutrum.  Nulla tellus.  In sagittis ║\n" +
                "║    ║            ║           ║                         ║                                               ║ dui  vel nisl.  Duis ac nibh.  Fusce lacus ║\n" +
                "║    ║            ║           ║                         ║                                               ║ purus, aliquet at,  feugiat  non,  pretium ║\n" +
                "║    ║            ║           ║                         ║                                               ║ quis, lectus.                              ║\n" +
                "║  5 ║ Erl        ║ Mellmer   ║ emellmer4@about.com     ║ harness cross-media infomediaries             ║ Suspendisse  potenti.   In eleifend quam a ║\n" +
                "║    ║            ║           ║                         ║                                               ║ odio.  In hac habitasse  platea  dictumst. ║\n" +
                "║    ║            ║           ║                         ║                                               ║ Maecenas ut massa quis augue luctus        ║\n" +
                "║    ║            ║           ║                         ║                                               ║ tincidunt.                                 ║\n" +
                "╚════╩════════════╩═══════════╩═════════════════════════╩═══════════════════════════════════════════════╩════════════════════════════════════════════╝\n");
    }

    @Test
    public void unicodeSingle() {
        assertTable(Border::unicodeSingle, "" +
                "┌────┬────────────┬───────────┬─────────────────────────┬───────────────────────────────────────────────┬────────────────────────────────────────────┐\n" +
                "│ id │ first_name │ last_name │          email          │                    slogan                     │                  sentence                  │\n" +
                "├────┼────────────┼───────────┼─────────────────────────┼───────────────────────────────────────────────┼────────────────────────────────────────────┤\n" +
                "│  1 │ Wendie     │ Marquet   │ wmarquet0@blogspot.com  │ unleash mission-critical experiences. unleash │ Suspendisse  ornare  consequat lectus.  In │\n" +
                "│    │            │           │                         │ mission-critical experiences                  │ est risus, auctor sed, tristique in,       │\n" +
                "│    │            │           │                         │                                               │ tempus sit amet, sem.                      │\n" +
                "│  2 │ Derry      │ Henkmann  │ dhenkmann1@cdbaby.com   │ innovate seamless e-services                  │ Sed vel enim sit amet nunc viverra         │\n" +
                "│    │            │           │                         │                                               │ dapibus.  Nulla suscipit ligula in  lacus. │\n" +
                "│    │            │           │                         │                                               │ Curabitur at ipsum ac tellus semper        │\n" +
                "│    │            │           │                         │                                               │ interdum.                                  │\n" +
                "│  3 │ Heidi      │ Bointon   │ hbointon2@bloglovin.com │ visualize real-time architectures.  visualize │ Vivamus  vel  nulla  eget  eros  elementum │\n" +
                "│    │            │           │                         │ real-time architectures                       │ pellentesque. Quisque porta volutpat erat. │\n" +
                "│    │            │           │                         │                                               │ Quisque  erat  eros,  viverra eget, congue │\n" +
                "│    │            │           │                         │                                               │ eget, semper rutrum, nulla.                │\n" +
                "│  4 │ Elladine   │ Twelve    │ etwelve3@friendfeed.com │ scale global platforms                        │ Integer  non  velit.    Donec  diam neque, │\n" +
                "│    │            │           │                         │                                               │ vestibulum eget,  vulputate  ut,  ultrices │\n" +
                "│    │            │           │                         │                                               │ vel,  augue.  Vestibulum ante ipsum primis │\n" +
                "│    │            │           │                         │                                               │ in faucibus orci luctus et ultrices        │\n" +
                "│    │            │           │                         │                                               │ posuere  cubilia  Curae;  Donec  pharetra, │\n" +
                "│    │            │           │                         │                                               │ magna vestibulum  aliquet  ultrices,  erat │\n" +
                "│    │            │           │                         │                                               │ tortor  sollicitudin mi, sit amet lobortis │\n" +
                "│    │            │           │                         │                                               │ sapien sapien non mi.  Integer  ac  neque. │\n" +
                "│    │            │           │                         │                                               │ Duis  bibendum.    Morbi  non quam nec dui │\n" +
                "│    │            │           │                         │                                               │ luctus rutrum.  Nulla tellus.  In sagittis │\n" +
                "│    │            │           │                         │                                               │ dui  vel nisl.  Duis ac nibh.  Fusce lacus │\n" +
                "│    │            │           │                         │                                               │ purus, aliquet at,  feugiat  non,  pretium │\n" +
                "│    │            │           │                         │                                               │ quis, lectus.                              │\n" +
                "│  5 │ Erl        │ Mellmer   │ emellmer4@about.com     │ harness cross-media infomediaries             │ Suspendisse  potenti.   In eleifend quam a │\n" +
                "│    │            │           │                         │                                               │ odio.  In hac habitasse  platea  dictumst. │\n" +
                "│    │            │           │                         │                                               │ Maecenas ut massa quis augue luctus        │\n" +
                "│    │            │           │                         │                                               │ tincidunt.                                 │\n" +
                "└────┴────────────┴───────────┴─────────────────────────┴───────────────────────────────────────────────┴────────────────────────────────────────────┘\n");
    }

    @Test
    public void unicodeSingleSeparated() {
        assertTable(Border::unicodeSingleSeparated, "" +
                "┌────┬────────────┬───────────┬─────────────────────────┬───────────────────────────────────────────────┬────────────────────────────────────────────┐\n" +
                "│ id │ first_name │ last_name │          email          │                    slogan                     │                  sentence                  │\n" +
                "├════┼════════════┼═══════════┼═════════════════════════┼═══════════════════════════════════════════════┼════════════════════════════════════════════┤\n" +
                "│  1 │ Wendie     │ Marquet   │ wmarquet0@blogspot.com  │ unleash mission-critical experiences. unleash │ Suspendisse  ornare  consequat lectus.  In │\n" +
                "│    │            │           │                         │ mission-critical experiences                  │ est risus, auctor sed, tristique in,       │\n" +
                "│    │            │           │                         │                                               │ tempus sit amet, sem.                      │\n" +
                "├────┼────────────┼───────────┼─────────────────────────┼───────────────────────────────────────────────┼────────────────────────────────────────────┤\n" +
                "│  2 │ Derry      │ Henkmann  │ dhenkmann1@cdbaby.com   │ innovate seamless e-services                  │ Sed vel enim sit amet nunc viverra         │\n" +
                "│    │            │           │                         │                                               │ dapibus.  Nulla suscipit ligula in  lacus. │\n" +
                "│    │            │           │                         │                                               │ Curabitur at ipsum ac tellus semper        │\n" +
                "│    │            │           │                         │                                               │ interdum.                                  │\n" +
                "├────┼────────────┼───────────┼─────────────────────────┼───────────────────────────────────────────────┼────────────────────────────────────────────┤\n" +
                "│  3 │ Heidi      │ Bointon   │ hbointon2@bloglovin.com │ visualize real-time architectures.  visualize │ Vivamus  vel  nulla  eget  eros  elementum │\n" +
                "│    │            │           │                         │ real-time architectures                       │ pellentesque. Quisque porta volutpat erat. │\n" +
                "│    │            │           │                         │                                               │ Quisque  erat  eros,  viverra eget, congue │\n" +
                "│    │            │           │                         │                                               │ eget, semper rutrum, nulla.                │\n" +
                "├────┼────────────┼───────────┼─────────────────────────┼───────────────────────────────────────────────┼────────────────────────────────────────────┤\n" +
                "│  4 │ Elladine   │ Twelve    │ etwelve3@friendfeed.com │ scale global platforms                        │ Integer  non  velit.    Donec  diam neque, │\n" +
                "│    │            │           │                         │                                               │ vestibulum eget,  vulputate  ut,  ultrices │\n" +
                "│    │            │           │                         │                                               │ vel,  augue.  Vestibulum ante ipsum primis │\n" +
                "│    │            │           │                         │                                               │ in faucibus orci luctus et ultrices        │\n" +
                "│    │            │           │                         │                                               │ posuere  cubilia  Curae;  Donec  pharetra, │\n" +
                "│    │            │           │                         │                                               │ magna vestibulum  aliquet  ultrices,  erat │\n" +
                "│    │            │           │                         │                                               │ tortor  sollicitudin mi, sit amet lobortis │\n" +
                "│    │            │           │                         │                                               │ sapien sapien non mi.  Integer  ac  neque. │\n" +
                "│    │            │           │                         │                                               │ Duis  bibendum.    Morbi  non quam nec dui │\n" +
                "│    │            │           │                         │                                               │ luctus rutrum.  Nulla tellus.  In sagittis │\n" +
                "│    │            │           │                         │                                               │ dui  vel nisl.  Duis ac nibh.  Fusce lacus │\n" +
                "│    │            │           │                         │                                               │ purus, aliquet at,  feugiat  non,  pretium │\n" +
                "│    │            │           │                         │                                               │ quis, lectus.                              │\n" +
                "├────┼────────────┼───────────┼─────────────────────────┼───────────────────────────────────────────────┼────────────────────────────────────────────┤\n" +
                "│  5 │ Erl        │ Mellmer   │ emellmer4@about.com     │ harness cross-media infomediaries             │ Suspendisse  potenti.   In eleifend quam a │\n" +
                "│    │            │           │                         │                                               │ odio.  In hac habitasse  platea  dictumst. │\n" +
                "│    │            │           │                         │                                               │ Maecenas ut massa quis augue luctus        │\n" +
                "│    │            │           │                         │                                               │ tincidunt.                                 │\n" +
                "└────┴────────────┴───────────┴─────────────────────────┴───────────────────────────────────────────────┴────────────────────────────────────────────┘\n");
    }

    @Test
    public void whitespaceCompact() {
        assertTable(Border::whitespaceCompact, "" +
                "id   first_name   last_name            email                                slogan                                          sentence                  \n" +
                "                                                                                                                                                      \n" +
                " 1   Wendie       Marquet     wmarquet0@blogspot.com    unleash  mission-critical experiences.  unleash   Suspendisse ornare consequat lectus.  In est\n" +
                "                                                        mission-critical experiences                      risus, auctor sed, tristique in, tempus  sit\n" +
                "                                                                                                          amet, sem.                                  \n" +
                " 2   Derry        Henkmann    dhenkmann1@cdbaby.com     innovate seamless e-services                      Sed  vel enim sit amet nunc viverra dapibus.\n" +
                "                                                                                                          Nulla suscipit ligula in lacus. Curabitur at\n" +
                "                                                                                                          ipsum ac tellus semper interdum.            \n" +
                " 3   Heidi        Bointon     hbointon2@bloglovin.com   visualize  real-time  architectures.  visualize   Vivamus vel nulla eget eros elementum       \n" +
                "                                                        real-time architectures                           pellentesque.  Quisque porta volutpat  erat.\n" +
                "                                                                                                          Quisque  erat  eros,  viverra  eget,  congue\n" +
                "                                                                                                          eget, semper rutrum, nulla.                 \n" +
                " 4   Elladine     Twelve      etwelve3@friendfeed.com   scale global platforms                            Integer non velit. Donec diam neque,        \n" +
                "                                                                                                          vestibulum eget, vulputate ut, ultrices vel,\n" +
                "                                                                                                          augue.    Vestibulum  ante  ipsum  primis in\n" +
                "                                                                                                          faucibus orci  luctus  et  ultrices  posuere\n" +
                "                                                                                                          cubilia Curae; Donec pharetra, magna        \n" +
                "                                                                                                          vestibulum  aliquet  ultrices,  erat  tortor\n" +
                "                                                                                                          sollicitudin  mi,  sit  amet lobortis sapien\n" +
                "                                                                                                          sapien non mi.   Integer  ac  neque.    Duis\n" +
                "                                                                                                          bibendum.    Morbi  non  quam nec dui luctus\n" +
                "                                                                                                          rutrum.  Nulla tellus.  In sagittis dui  vel\n" +
                "                                                                                                          nisl.    Duis  ac  nibh.  Fusce lacus purus,\n" +
                "                                                                                                          aliquet at, feugiat non, pretium quis,      \n" +
                "                                                                                                          lectus.                                     \n" +
                " 5   Erl          Mellmer     emellmer4@about.com       harness cross-media infomediaries                 Suspendisse  potenti.    In  eleifend quam a\n" +
                "                                                                                                          odio.  In  hac  habitasse  platea  dictumst.\n" +
                "                                                                                                          Maecenas ut massa quis augue luctus         \n" +
                "                                                                                                          tincidunt.                                  \n");
    }

    @Test
    public void whitespaceSeparated() {
        assertTable(Border::whitespaceSeparated, "" +
                "id   first_name   last_name            email                                slogan                                          sentence                  \n" +
                "                                                                                                                                                      \n" +
                " 1   Wendie       Marquet     wmarquet0@blogspot.com    unleash  mission-critical experiences.  unleash   Suspendisse ornare consequat lectus.  In est\n" +
                "                                                        mission-critical experiences                      risus, auctor sed, tristique in, tempus  sit\n" +
                "                                                                                                          amet, sem.                                  \n" +
                "                                                                                                                                                      \n" +
                " 2   Derry        Henkmann    dhenkmann1@cdbaby.com     innovate seamless e-services                      Sed  vel enim sit amet nunc viverra dapibus.\n" +
                "                                                                                                          Nulla suscipit ligula in lacus. Curabitur at\n" +
                "                                                                                                          ipsum ac tellus semper interdum.            \n" +
                "                                                                                                                                                      \n" +
                " 3   Heidi        Bointon     hbointon2@bloglovin.com   visualize  real-time  architectures.  visualize   Vivamus vel nulla eget eros elementum       \n" +
                "                                                        real-time architectures                           pellentesque.  Quisque porta volutpat  erat.\n" +
                "                                                                                                          Quisque  erat  eros,  viverra  eget,  congue\n" +
                "                                                                                                          eget, semper rutrum, nulla.                 \n" +
                "                                                                                                                                                      \n" +
                " 4   Elladine     Twelve      etwelve3@friendfeed.com   scale global platforms                            Integer non velit. Donec diam neque,        \n" +
                "                                                                                                          vestibulum eget, vulputate ut, ultrices vel,\n" +
                "                                                                                                          augue.    Vestibulum  ante  ipsum  primis in\n" +
                "                                                                                                          faucibus orci  luctus  et  ultrices  posuere\n" +
                "                                                                                                          cubilia Curae; Donec pharetra, magna        \n" +
                "                                                                                                          vestibulum  aliquet  ultrices,  erat  tortor\n" +
                "                                                                                                          sollicitudin  mi,  sit  amet lobortis sapien\n" +
                "                                                                                                          sapien non mi.   Integer  ac  neque.    Duis\n" +
                "                                                                                                          bibendum.    Morbi  non  quam nec dui luctus\n" +
                "                                                                                                          rutrum.  Nulla tellus.  In sagittis dui  vel\n" +
                "                                                                                                          nisl.    Duis  ac  nibh.  Fusce lacus purus,\n" +
                "                                                                                                          aliquet at, feugiat non, pretium quis,      \n" +
                "                                                                                                          lectus.                                     \n" +
                "                                                                                                                                                      \n" +
                " 5   Erl          Mellmer     emellmer4@about.com       harness cross-media infomediaries                 Suspendisse  potenti.    In  eleifend quam a\n" +
                "                                                                                                          odio.  In  hac  habitasse  platea  dictumst.\n" +
                "                                                                                                          Maecenas ut massa quis augue luctus         \n" +
                "                                                                                                          tincidunt.                                  \n");
    }

    public void assertTable(final Supplier<Border.Builder> border, final String expected) {
        final Table table = new Table(data, border.get().build(), 150);
        final String actual = table.format();
        Assert.assertEquals(expected, actual);
    }
}
