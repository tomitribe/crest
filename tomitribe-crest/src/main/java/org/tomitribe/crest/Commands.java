/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import org.tomitribe.crest.api.Command;
import org.tomitribe.util.collect.FilteredIterable;
import org.tomitribe.util.collect.FilteredIterator;
import org.tomitribe.util.reflect.Reflection;

import java.lang.reflect.Method;

public class Commands {

    public static Iterable<Method> commands(Class<?> clazz) {
        return new FilteredIterable<Method>(Reflection.methods(clazz),
                new FilteredIterator.Filter<Method>() {
                    @Override
                    public boolean accept(Method method) {
                        return method.isAnnotationPresent(Command.class);
                    }
                }
        );
    }
}
