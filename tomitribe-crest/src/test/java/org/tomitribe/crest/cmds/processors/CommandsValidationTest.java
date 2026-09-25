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
package org.tomitribe.crest.cmds.processors;

import org.junit.Test;
import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.Command;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Commands.commands must reject every @Command the runtime would otherwise
 * silently ignore, even when the class also has valid commands.
 */
public class CommandsValidationTest {

    @Test
    public void validClassPasses() {
        Commands.validate(AllPublic.class);
        assertTrue(Commands.get(AllPublic.class).containsKey("one"));
    }

    /**
     * One valid command must not hide an invalid one beside it.
     */
    @Test
    public void mixedValidAndInvalid() {
        final InvalidCommandUsageException e = expect(Mixed.class);
        assertEquals(1, e.getViolations().size());
        assertEquals("hidden() is private, must be public", e.getViolations().get(0));
    }

    @Test
    public void allInvalidUsesAreCollected() {
        final InvalidCommandUsageException e = expect(ThreeWrong.class);
        final List<String> v = e.getViolations();
        assertEquals(3, v.size());
        assertTrue(v.contains("a() is private, must be public"));
        assertTrue(v.contains("b(String) is protected, must be public"));
        assertTrue(v.contains("c(String, int) is package-private, must be public"));
    }

    /**
     * getMethods() cannot see a non-public method on a superclass either,
     * so the hierarchy is walked and the declaring class is named.
     */
    @Test
    public void inheritedInvalidUse() {
        final InvalidCommandUsageException e = expect(Child.class);
        assertEquals(Child.class, e.getClazz());
        assertEquals(1, e.getViolations().size());
        assertEquals(Parent.class.getName() + ".hidden() is private, must be public", e.getViolations().get(0));
    }

    @Test
    public void messageListsEveryViolation() {
        final String message = expect(ThreeWrong.class).getMessage();
        assertTrue(message, message.startsWith("Invalid @Command usage on class " + ThreeWrong.class.getName() + ":\n"));
        assertTrue(message, message.contains("\n  - a() is private, must be public\n"));
        assertTrue(message, message.contains("\n  - b(String) is protected, must be public\n"));
        assertTrue(message, message.contains("\n  - c(String, int) is package-private, must be public\n"));
        assertTrue(message, message.endsWith("move the annotation to the implementing method, or remove it."));
    }

    /**
     * The abstract class itself cannot be instantiated, so an abstract
     * @Command method on it can never run.
     */
    @Test
    public void abstractMethodOnAbstractClass() {
        final InvalidCommandUsageException e = expect(AbstractBase.class);
        assertEquals(1, e.getViolations().size());
        assertEquals("doIt() is abstract, must be concrete", e.getViolations().get(0));
    }

    /**
     * The concrete override does not inherit @Command, so the command is
     * silently lost unless we reject it.  The declaring class is named.
     */
    @Test
    public void abstractMethodOverriddenInConcreteSubclass() {
        final InvalidCommandUsageException e = expect(ConcreteImpl.class);
        assertEquals(1, e.getViolations().size());
        assertEquals(AbstractBase.class.getName() + ".doIt() is abstract, must be concrete", e.getViolations().get(0));
    }

    
    public void abstractMethodOnInterface() {
        final InvalidCommandUsageException e = expect(IfaceImpl.class);
        assertEquals(1, e.getViolations().size());
        assertEquals(Iface.class.getName() + ".viaIface() is abstract, must be concrete", e.getViolations().get(0));
    }

    /**
     * A static @Command needs no instance, so an abstract class is fine.
     */
    @Test
    public void staticCommandOnAbstractClassPasses() {
        Commands.validate(StaticOnAbstract.class);
        assertTrue(Commands.get(StaticOnAbstract.class).containsKey("stat"));
    }

    /**
     * A method that is both non-public and abstract is reported for both.
     */
    @Test
    public void abstractAndNonPublicBothReported() {
        final InvalidCommandUsageException e = expect(ProtectedAbstract.class);
        assertEquals(2, e.getViolations().size());
        assertTrue(e.getViolations().contains("both() is protected, must be public"));
        assertTrue(e.getViolations().contains("both() is abstract, must be concrete"));
    }

    @Test
    public void surfacesThroughMain() {
        try {
            new Main(Mixed.class);
            fail("Expected InvalidCommandUsageException");
        } catch (final InvalidCommandUsageException pass) {
            assertEquals(Mixed.class, pass.getClazz());
        }
    }

    private static InvalidCommandUsageException expect(final Class<?> clazz) {
        try {
            Commands.get(clazz);
        } catch (final InvalidCommandUsageException e) {
            return e;
        }
        throw new AssertionError("Expected InvalidCommandUsageException for " + clazz.getName());
    }

    public static class AllPublic {
        @Command
        public static String one() {
            return "one";
        }
    }

    public static class Mixed {
        @Command
        public static String visible() {
            return "visible";
        }

        @Command
        private static String hidden() {
            return "hidden";
        }
    }

    public static class ThreeWrong {
        @Command
        private static void a() {
        }

        @Command
        protected static void b(final String s) {
        }

        @Command
        static void c(final String s, final int i) {
        }
    }

    public static class Parent {
        @Command
        private static String hidden() {
            return "hidden";
        }
    }

    public static class Child extends Parent {
        @Command
        public static String shown() {
            return "shown";
        }
    }

    public abstract static class AbstractBase {
        @Command
        public abstract String doIt();
    }

    public static class ConcreteImpl extends AbstractBase {
        @Override
        public String doIt() {
            return "done";
        }
    }

    public interface Iface {
        @Command
        String viaIface();
    }

    public static class IfaceImpl implements Iface {
        @Override
        public String viaIface() {
            return "iface";
        }
    }

    public abstract static class StaticOnAbstract {
        @Command
        public static String stat() {
            return "static";
        }

        public abstract void other();
    }

    public abstract static class ProtectedAbstract {
        @Command
        protected abstract String both();
    }
}
