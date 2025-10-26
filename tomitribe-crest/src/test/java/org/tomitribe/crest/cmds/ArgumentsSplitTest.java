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
package org.tomitribe.crest.cmds;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ArgumentsSplitTest {
    @Test
    public void testSplitWithGlobalCommandAndArgs() {
        final String[] input = {"--verbose", "-Dfoo=bar", "deploy", "--force", "staging"};
        final Arguments.Split split = Arguments.Split.split(input);

        assertArrayEquals(new String[]{"--verbose", "-Dfoo=bar"}, split.getGlobal());
        assertEquals("deploy", split.getCommand());
        assertArrayEquals(new String[]{"--force", "staging"}, split.getArgs());
    }

    @Test
    public void testSplitWithOnlyGlobals() {
        final String[] input = {"--help", "-version"};
        final Arguments.Split split = Arguments.Split.split(input);

        assertArrayEquals(new String[]{"--help", "-version"}, split.getGlobal());
        assertNull(split.getCommand());
        assertArrayEquals(new String[0], split.getArgs());
    }

    @Test
    public void testSplitWithOnlyCommandAndArgs() {
        final String[] input = {"run", "--fast", "file.txt"};
        final Arguments.Split split = Arguments.Split.split(input);

        assertArrayEquals(new String[0], split.getGlobal());
        assertEquals("run", split.getCommand());
        assertArrayEquals(new String[]{"--fast", "file.txt"}, split.getArgs());
    }

    @Test
    public void testSplitWithCommandOnly() {
        final String[] input = {"status"};
        final Arguments.Split split = Arguments.Split.split(input);

        assertArrayEquals(new String[0], split.getGlobal());
        assertEquals("status", split.getCommand());
        assertArrayEquals(new String[0], split.getArgs());
    }

    @Test
    public void testSplitWithEmptyArray() {
        final String[] input = {};
        final Arguments.Split split = Arguments.Split.split(input);

        assertArrayEquals(new String[0], split.getGlobal());
        assertNull(split.getCommand());
        assertArrayEquals(new String[0], split.getArgs());
    }

    @Test
    public void testSplitWithNullArray() {
        final Arguments.Split split = Arguments.Split.split(null);

        assertArrayEquals(new String[0], split.getGlobal());
        assertNull(split.getCommand());
        assertArrayEquals(new String[0], split.getArgs());
    }

    @Test
    public void testOriginalArrayUnmodified() {
        final String[] input = {"--debug", "start"};
        final String[] copy = Arrays.copyOf(input, input.length);

        Arguments.Split.split(input);

        assertArrayEquals(copy, input);
    }

    @Test
    public void testSplitWhereCommandLooksLikeFlag() {
        // Commands cannot start with '-', so all are treated as global flags
        final String[] input = {"-", "--force"};
        final Arguments.Split split = Arguments.Split.split(input);

        assertArrayEquals(new String[]{"-", "--force"}, split.getGlobal());
        assertNull(split.getCommand());
        assertArrayEquals(new String[0], split.getArgs());
    }
}