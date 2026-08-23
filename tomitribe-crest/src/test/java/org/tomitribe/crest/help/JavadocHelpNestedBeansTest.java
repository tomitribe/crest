/*
 * Copyright 2026 Tomitribe and community
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
package org.tomitribe.crest.help;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;

import static org.junit.Assert.assertEquals;

/**
 * @Options beans nested three deep: Cluster owns Node, Node owns Disk.
 * The DESCRIPTION inlines each bean's narrative in owner-then-nested
 * order, and every level's options are documented by its own
 * constructor's @param entries.
 */
public class JavadocHelpNestedBeansTest {

    @Test
    public void test() throws Exception {
        final TestEnvironment env = TestEnvironment.builder().build();
        new Main(Fleet.class).main(env, new String[]{"help", "deploy"});

        assertEquals("NAME\n" +
                        "       deploy\n" +
                        "\n" +
                        "SYNOPSIS\n" +
                        "       deploy [options]\n" +
                        "\n" +
                        "DESCRIPTION\n" +
                        "       The deploy command installs the application into a cluster.\n" +
                        "\n" +
                        "       The cluster options choose which cluster receives the deployment.\n" +
                        "\n" +
                        "       The  node  options  pin the deployment to a single node instead of letting the cluster\n" +
                        "       choose one.\n" +
                        "\n" +
                        "       The disk options choose where artifacts land once a node is picked.\n" +
                        "\n" +
                        "OPTIONS\n" +
                        "       --cluster=<String>\n" +
                        "              the cluster the deployment is installed into\n" +
                        "       \n" +
                        "              default: staging\n" +
                        "\n" +
                        "       --node=<String>\n" +
                        "              the node the deployment lands on\n" +
                        "\n" +
                        "       --disk=<String>\n" +
                        "              the directory artifacts are unpacked into\n" +
                        "       \n" +
                        "              default: /var/apps\n",
                env.getOut().toString());
    }

    public static class Fleet {

        /**
         * The deploy command installs the application into a cluster.
         */
        @Command
        public void deploy(final Cluster cluster) {
        }
    }

    /**
     * The cluster options choose which cluster receives the deployment.
     */
    @Options
    public static class Cluster {

        /**
         * @param cluster the cluster the deployment is installed into
         */
        public Cluster(@Option("cluster") @Default("staging") final String cluster,
                       final Node node) {
        }
    }

    /**
     * The node options pin the deployment to a single node instead of
     * letting the cluster choose one.
     */
    @Options
    public static class Node {

        /**
         * @param node the node the deployment lands on
         */
        public Node(@Option("node") final String node,
                    final Disk disk) {
        }
    }

    /**
     * The disk options choose where artifacts land once a node is picked.
     */
    @Options
    public static class Disk {

        /**
         * @param disk the directory artifacts are unpacked into
         */
        public Disk(@Option("disk") @Default("/var/apps") final String disk) {
        }
    }
}
