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
package org.tomitribe.crest;

import org.junit.Test;
import org.tomitribe.crest.cmds.processors.types.PrimitiveTypes;

import static org.junit.Assert.assertEquals;

public class PrimitiveTypesTest {

    @Test(expected = IllegalArgumentException.class)
    public void shouldReturnExceptionIfNotFoundType(){
        PrimitiveTypes.valueOf("TomiTribe").getWraper();
    }

    @Test
    public void shouldReturnWrapperType() {
        assertEquals(Boolean.class, PrimitiveTypes.valueOf(boolean.class.getSimpleName().toUpperCase()).getWraper());
        assertEquals(Byte.class, PrimitiveTypes.valueOf(byte.class.getSimpleName().toUpperCase()).getWraper());
        assertEquals(Character.class, PrimitiveTypes.valueOf(char.class.getSimpleName().toUpperCase()).getWraper());
        assertEquals(Long.class, PrimitiveTypes.valueOf(long.class.getSimpleName().toUpperCase()).getWraper());
        assertEquals(Float.class, PrimitiveTypes.valueOf(float.class.getSimpleName().toUpperCase()).getWraper());
        assertEquals(Integer.class, PrimitiveTypes.valueOf(int.class.getSimpleName().toUpperCase()).getWraper());
    }

    @Test
    public void shouldReturnDefaultType() {
        assertEquals("false", PrimitiveTypes.valueOf(boolean.class.getSimpleName().toUpperCase()).getDefaultValue());
        assertEquals("0", PrimitiveTypes.valueOf(byte.class.getSimpleName().toUpperCase()).getDefaultValue());
        assertEquals("\u0000", PrimitiveTypes.valueOf(char.class.getSimpleName().toUpperCase()).getDefaultValue());
        assertEquals("0", PrimitiveTypes.valueOf(long.class.getSimpleName().toUpperCase()).getDefaultValue());
        assertEquals("0", PrimitiveTypes.valueOf(float.class.getSimpleName().toUpperCase()).getDefaultValue());
        assertEquals("0", PrimitiveTypes.valueOf(int.class.getSimpleName().toUpperCase()).getDefaultValue());
    }
}
