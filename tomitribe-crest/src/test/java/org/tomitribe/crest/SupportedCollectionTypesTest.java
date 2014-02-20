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

import junit.framework.TestCase;

import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SupportedCollectionTypesTest extends TestCase {


    public void testList() {

        assertEquals(TreeSet.class, CmdMethod.instantiate(NavigableSet.class).getClass());
        assertEquals(TreeSet.class, CmdMethod.instantiate(SortedSet.class).getClass());
        assertEquals(LinkedHashSet.class, CmdMethod.instantiate(Set.class).getClass());

        assertEquals(LinkedList.class, CmdMethod.instantiate(Deque.class).getClass());
        assertEquals(LinkedList.class, CmdMethod.instantiate(Queue.class).getClass());

        assertEquals(ArrayList.class, CmdMethod.instantiate(List.class).getClass());
        assertEquals(LinkedList.class, CmdMethod.instantiate(Collection.class).getClass());

        assertEquals(ArrayList.class, CmdMethod.instantiate(ArrayList.class).getClass());
        assertEquals(LinkedList.class, CmdMethod.instantiate(LinkedList.class).getClass());
        assertEquals(LinkedHashSet.class, CmdMethod.instantiate(LinkedHashSet.class).getClass());


        try {
            CmdMethod.instantiate(AbstractList.class).getClass();
            fail();
        } catch (final IllegalStateException e) {
        }

        try {
            CmdMethod.instantiate(AbstractSet.class).getClass();
            fail();
        } catch (final IllegalStateException e) {
        }

    }
}
