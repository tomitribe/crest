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
