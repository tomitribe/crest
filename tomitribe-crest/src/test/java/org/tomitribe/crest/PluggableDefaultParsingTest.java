package org.tomitribe.crest;

import org.apache.xbean.finder.archive.ClassesArchive;
import org.junit.Test;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class PluggableDefaultParsingTest
{
    @Test
    public void testPlugInNewDefaultsContext() throws Exception
    {
        final String user = System.getProperty("user.name");
        final String new_user = String.format("NOT%s", user);

        final Main main = new Main(new ClassesArchive(Orange.class), new FixedValueDefaultsContext(new_user));

        Object out = main.exec("defaults");

        assertEquals(out, String.format("Hello %s", new_user));
    }

    public static class Orange
    {

        @Command
        public String property(String name)
        {
            return System.getProperty(name);
        }

        @Command
        public String defaults(@Option("user") @Default("${user.name}") String user)
        {
            return String.format("Hello %s", user);
        }
    }

    public static class FixedValueDefaultsContext implements DefaultsContext
    {
        private final String value;

        FixedValueDefaultsContext(String value) {
            this.value = value;
        }

        @Override
        public String find(final Target cmd, final Method commandMethod, final String key)
        {
            return value;
        }
    }
}
