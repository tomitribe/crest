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
package org.tomitribe.crest;

import org.junit.Assert;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Exit;
import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.cmds.CommandFailedException;
import org.tomitribe.crest.test.Java;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.stream.Stream;

/**
 * TODO there's logic that treats IllegalArgumentException as something will cause us to print
 * the help message and then the exception message itself.  We need to test this.  Likely we
 * need to fix it as well, because it seems to print a stacktrace and that's not the concept.
 */
public class ExceptionHandlingTest {

    @Test
    public void testStringRuntimeException() {
        assertHandlingAndStacktrace("String", RuntimeException.class, "No good", 255,
                "java.lang.RuntimeException: No good\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStringError() {
        assertHandlingAndStacktrace("String", Error.class, "Do not compute", 255,
                "java.lang.Error: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStringLovePotionNumberNineException() {
        assertHandling("String", LovePotionNumberNineException.class, "A cop was kissed", 9, "A cop was kissed\n");
    }

    @Test
    public void testStringIShouldHaveKnownBetterException() {
        assertHandlingAndStacktrace("String", IShouldHaveKnownBetterException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$IShouldHaveKnownBetterException: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStringPleadTheFifthException() {
        assertHandling("String", PleadTheFifthException.class, "Do not compute", 5, "null\n");
    }

    @Test
    public void testStringActionsAreLouderThanWordsException() {
        assertHandlingAndStacktrace("String", ActionsAreLouderThanWordsException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$ActionsAreLouderThanWordsException\n" +
                        "\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamRuntimeException() {
        assertHandlingAndStacktrace("Stream", RuntimeException.class, "No good", 255,
                "java.lang.RuntimeException: No good\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamError() {
        assertHandlingAndStacktrace("Stream", Error.class, "Do not compute", 255,
                "java.lang.Error: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamLovePotionNumberNineException() {
        assertHandling("Stream", LovePotionNumberNineException.class, "A cop was kissed", 9, "A cop was kissed\n");
    }

    @Test
    public void testStreamIShouldHaveKnownBetterException() {
        assertHandlingAndStacktrace("Stream", IShouldHaveKnownBetterException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$IShouldHaveKnownBetterException: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamPleadTheFifthException() {
        assertHandling("Stream", PleadTheFifthException.class, "Do not compute", 5, "null\n");
    }

    @Test
    public void testStreamActionsAreLouderThanWordsException() {
        assertHandlingAndStacktrace("Stream", ActionsAreLouderThanWordsException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$ActionsAreLouderThanWordsException\n" +
                        "\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testIterableRuntimeException() {
        assertHandlingAndStacktrace("Iterable", RuntimeException.class, "No good", 255,
                "java.lang.RuntimeException: No good\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testIterableError() {
        assertHandlingAndStacktrace("Iterable", Error.class, "Do not compute", 255,
                "java.lang.Error: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testIterableLovePotionNumberNineException() {
        assertHandling("Iterable", LovePotionNumberNineException.class, "A cop was kissed", 9, "A cop was kissed\n");
    }

    @Test
    public void testIterableIShouldHaveKnownBetterException() {
        assertHandlingAndStacktrace("Iterable", IShouldHaveKnownBetterException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$IShouldHaveKnownBetterException: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testIterablePleadTheFifthException() {
        assertHandling("Iterable", PleadTheFifthException.class, "Do not compute", 5, "null\n");
    }

    @Test
    public void testIterableActionsAreLouderThanWordsException() {
        assertHandlingAndStacktrace("Iterable", ActionsAreLouderThanWordsException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$ActionsAreLouderThanWordsException\n" +
                        "\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testPrintOutputRuntimeException() {
        assertHandlingAndStacktrace("PrintOutput", RuntimeException.class, "No good", 255,
                "java.lang.RuntimeException: No good\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testPrintOutputError() {
        assertHandlingAndStacktrace("PrintOutput", Error.class, "Do not compute", 255,
                "java.lang.Error: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testPrintOutputLovePotionNumberNineException() {
        assertHandling("PrintOutput", LovePotionNumberNineException.class, "A cop was kissed", 9, "A cop was kissed\n");
    }

    @Test
    public void testPrintOutputIShouldHaveKnownBetterException() {
        assertHandlingAndStacktrace("PrintOutput", IShouldHaveKnownBetterException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$IShouldHaveKnownBetterException: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testPrintOutputPleadTheFifthException() {
        assertHandling("PrintOutput", PleadTheFifthException.class, "Do not compute", 5, "null\n");
    }

    @Test
    public void testPrintOutputActionsAreLouderThanWordsException() {
        assertHandlingAndStacktrace("PrintOutput", ActionsAreLouderThanWordsException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$ActionsAreLouderThanWordsException\n" +
                        "\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }


    @Test
    public void testStreamingOutputRuntimeException() {
        assertHandlingAndStacktrace("StreamingOutput", RuntimeException.class, "No good", 255,
                "java.lang.RuntimeException: No good\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamingOutputError() {
        assertHandlingAndStacktrace("StreamingOutput", Error.class, "Do not compute", 255,
                "java.lang.Error: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamingOutputLovePotionNumberNineException() {
        assertHandling("StreamingOutput", LovePotionNumberNineException.class, "A cop was kissed", 9, "A cop was kissed\n");
    }

    @Test
    public void testStreamingOutputIShouldHaveKnownBetterException() {
        assertHandlingAndStacktrace("StreamingOutput", IShouldHaveKnownBetterException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$IShouldHaveKnownBetterException: Do not compute\n\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    @Test
    public void testStreamingOutputPleadTheFifthException() {
        assertHandling("StreamingOutput", PleadTheFifthException.class, "Do not compute", 5, "null\n");
    }

    @Test
    public void testStreamingOutputActionsAreLouderThanWordsException() {
        assertHandlingAndStacktrace("StreamingOutput", ActionsAreLouderThanWordsException.class, "Do not compute", 255,
                "org.tomitribe.crest.ExceptionHandlingTest$ActionsAreLouderThanWordsException\n" +
                        "\tat ",
                "\tat org.tomitribe.crest.ExceptionHandlingTest",
                "\tat org.tomitribe.crest.Main.main(Main.java:"
        );
    }

    private void assertHandlingAndStacktrace(final String command, final Class<? extends Throwable> type, final String message, final int expected, final String expectedMessage, final String... contains) {
//        final Main main = new Main(Handling.class);
//        try {
//            main.main(new SystemEnvironment(), command, type.getName(), message);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        final Java.Result result = Crest.jar()
                .command(Handling.class)
                .add(ExceptionHandlingTest.class)
                .add(IShouldHaveKnownBetterException.class)
                .add(LovePotionNumberNineException.class)
                .add(PleadTheFifthException.class)
                .add(ActionsAreLouderThanWordsException.class)
                .exec(command, type.getName(), message);

        {
            // If we have windows line endings, yank them
            final String err = result.getErr().replaceAll("\r\n", "\n");

            // Check the start of the message is as expected.  This may be multi-line
            Assert.assertTrue(err.length() > expectedMessage.length());
            final String actualMessage = err.substring(0, expectedMessage.length());
            Assert.assertEquals(expectedMessage, actualMessage);

            // Check each separate string we were expecting
            for (final String string : contains) {
                Assert.assertTrue(err.contains(string));
            }
        }

        Assert.assertEquals("", result.getOut());
        Assert.assertEquals(expected, result.getExitCode());
    }

    /**
     * Exceptions that use @Exit do not get stack traces and can customize the error code
     */
    private void assertHandling(final String command, final Class<? extends Throwable> type, final String message, final int expected, final String expectedMessage) {
        final Java.Result result = Crest.jar()
                .command(Handling.class)
                .add(ExceptionHandlingTest.class)
                .add(IShouldHaveKnownBetterException.class)
                .add(LovePotionNumberNineException.class)
                .add(PleadTheFifthException.class)
                .add(ActionsAreLouderThanWordsException.class)
                .exec(command, type.getName(), message);

        Assert.assertEquals(expectedMessage, result.getErr());
        Assert.assertEquals("", result.getOut());
        Assert.assertEquals(expected, result.getExitCode());
    }

    public static class Handling {
        @Command("String")
        public String method(final Class<? extends Throwable> type, final String message) throws Throwable {
            throwable(type, message);
            return null;
        }

        @Command("StreamingOutput")
        public StreamingOutput streamingOutput(final Class<? extends Throwable> type, final String message) {
            return outputStream -> {
                runtime((Class<? extends Throwable>) type, message);
                return;
            };
        }

        @Command("PrintOutput")
        public PrintOutput printOutput(final Class<? extends Throwable> type, final String message) {
            return outputStream -> {
                runtime(type, message);
                return;
            };
        }

        @Command("Stream")
        public Stream<String> stream(final Class<? extends Throwable> type, final String message) {
            return Stream.of(message)
                    .peek(s -> runtime(type, message));
        }

        @Command("Iterable")
        public Iterable iterable(final Class<? extends Throwable> type, final String message) {
            return () -> new Iterator() {
                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public Object next() {
                    runtime(type, message);
                    return null;
                }
            };
        }

        private static void runtime(final Class<? extends Throwable> type, final String message) {
            try {
                throwable(type, message);
                return;
            } catch (RuntimeException | Error throwable) {
                throw throwable;
            } catch (Throwable throwable) {
                throw new CommandFailedException(throwable, "unknown");
            }
        }

        private static void throwable(final Class<? extends Throwable> type, final String message) throws Throwable {
            final Constructor<? extends Throwable> constructor = type.getConstructor(String.class);
            throw constructor.newInstance(message);
        }
    }


    /**
     * Don't kiss cops down on 34th and Vine.
     *
     * @author The Searchers
     */
    @Exit(9)
    public static class LovePotionNumberNineException extends RuntimeException {
        public LovePotionNumberNineException(final String message) {
            super(message);
        }
    }

    /**
     * I never realized what a kiss could be
     *
     * @author The Beatles
     */
    public static class IShouldHaveKnownBetterException extends Exception {
        public IShouldHaveKnownBetterException(final String message) {
            super(message);
        }
    }

    /**
     * I cannot tell you the cause of this exception out of fear it may incriminate me
     */
    @Exit(5)
    public static class PleadTheFifthException extends Throwable {
        public PleadTheFifthException(final String message) {
            // no message
        }
    }

    /**
     * You don't need good messages if you write a good stacktrace, right?
     */
    public static class ActionsAreLouderThanWordsException extends UnsupportedOperationException {
        public ActionsAreLouderThanWordsException(final String message) {
            // no message
        }
    }
}
