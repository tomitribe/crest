/* =====================================================================
 *
 * Copyright (c) 2011 David Blevins.  All rights reserved.
 *
 * =====================================================================
 */
package org.tomitribe.crest;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Revision$ $Date$
 */
public class SubstitutionTest extends Assert {

    //    public static Map<String, String> interpolate(Map<String, String> map, Map<String, String> properties) {
//        final Map<String, String> values = new HashMap<String, String>();
//
//        for (String s : values.keySet()) {
//
//        }
//    }

    @Test
    public void test() throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        map.put("one", "uno");
        map.put("two", "dos");
        map.put("three", "tres");
        map.put("user.dir", "/tmp/foo");
        map.put("red", "${green}");
        map.put("green", "${blue}");
        map.put("blue", "azul");

        // circular reference
        map.put("a", "${b}");
        map.put("b", "${a}");

        assertEquals("uno", Substitution.format("${one}", map));
        assertEquals("uunoo", Substitution.format("u${one}o", map));
        assertEquals("uno dos tres", Substitution.format("${one} ${two} ${three}", map));
        assertEquals("/tmp/foo", Substitution.format("${user.dir}", map));
        assertEquals("azul", Substitution.format("${red}", map));

        try {
            Substitution.format("${a}", map);
            fail();
        } catch (IllegalStateException e) {
            // pass
        }
    }
}
