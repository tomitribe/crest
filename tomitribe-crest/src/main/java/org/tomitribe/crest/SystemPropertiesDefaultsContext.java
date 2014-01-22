package org.tomitribe.crest;

import java.lang.reflect.Method;

public class SystemPropertiesDefaultsContext implements DefaultsContext
{
    @Override
    public String find(final Target cmd, final Method commandMethod, final String key)
    {
        return System.getProperty(key);
    }
}
