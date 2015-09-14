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
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Err;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.api.interceptor.ParameterMetadata;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.BEAN_OPTION;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.INTERNAL;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.OPTION;
import static org.tomitribe.crest.api.interceptor.ParameterMetadata.ParamType.PLAIN;

public class InterceptorTest {
    @Test
    public void intercept() throws Exception {
        assertEquals(
            "replaced2truetruefalsep3http://localhost:1253",
            new Main(InterceptMe.class, In1.class, In2.class, In3.class).exec("test1", "--o1=1", "--o2=2", "--o3=p3", "http://localhost:1253"));
    }

    @Test
    public void noProceed() throws Exception {
        assertEquals(
            "mock",
            new Main(InterceptMe.class, In3.class)
                .exec("test2", "--o1=1", "--o2=2", "--o3=p3", "http://localhost:1253"));
    }


    @Test
    public void changeParameters() throws Exception {
        assertEquals(
            "changedX2truetruetruep3http://localhost:1253",
            new Main(InterceptMe.class, InComplex.class)
                .exec("complex", "--prefix.val=1", "--o2=2", "--o3=p3", "http://localhost:1253"));
    }

    public static class InterceptMe {
        @Command(interceptedBy = { In1.class, In2.class })
        public String test1(
                         @Option("o1") final String o1,
                         @Option("o2") final int o2,
                         @Err final PrintStream err,
                         @Out final PrintStream out,
                         @In final InputStream is,
                         @Option("o3") final String o3,
                         final URL url) {
            return "" + o1 + o2 + Boolean.toString(err != null) + Boolean.toString(out != null) + Boolean.toString(is != null) + o3 + url.toExternalForm();
        }

        @Command(interceptedBy = In3.class)
        public String test2(
                         @Option("o1") final String o1,
                         @Option("o2") final int o2,
                         @Err final PrintStream err,
                         @Out final PrintStream out,
                         @In final InputStream is,
                         @Option("o3") final String o3,
                         final URL url) {
            return test1(o1, o2, err, out, is, o3, url);
        }

        @Command(interceptedBy = InComplex.class)
        public String complex(
                         @Option("prefix.") final CustomParam o1,
                         @Option("o2") final int o2,
                         @Err final PrintStream err,
                         @Out final PrintStream out,
                         @In final InputStream is,
                         @Option("o3") final String[] o3,
                         final URL url) {
            return test1(o1.val, o2, err, out, is, o3[0], url);
        }
    }

    public static class InComplex {
        @CrestInterceptor
        public Object intercept(final CrestContext crestContext) {
            final List<ParameterMetadata> options = crestContext.getParameterMetadata();
            {
                assertFalse(options.get(0).isListable());
                assertEquals(BEAN_OPTION, options.get(0).getType());
                assertEquals(1, options.get(0).getNested().size());

                final ParameterMetadata option = options.get(0).getNested().get(0);
                assertFalse(option.isListable());
                assertEquals(OPTION, option.getType());
                assertEquals("prefix.val", option.getName());
                assertEquals(String.class, option.getReflectType());
            }
            {
                assertFalse(options.get(1).isListable());
                assertEquals(OPTION, options.get(1).getType());
                assertEquals("o2", options.get(1).getName());
                assertEquals(int.class, options.get(1).getReflectType());
            }
            {
                assertEquals(INTERNAL, options.get(2).getType());
                assertEquals(PrintStream.class, options.get(2).getReflectType());
            }
            {
                assertEquals(INTERNAL, options.get(3).getType());
                assertEquals(PrintStream.class, options.get(3).getReflectType());
            }
            {
                assertEquals(INTERNAL, options.get(4).getType());
                assertEquals(InputStream.class, options.get(4).getReflectType());
            }
            {
                assertTrue(options.get(5).isListable());
                assertEquals(OPTION, options.get(5).getType());
                assertEquals("o3", options.get(5).getName());
                assertEquals(String[].class, options.get(5).getReflectType());
            }
            {
                assertFalse(options.get(6).isListable());
                assertEquals(PLAIN, options.get(6).getType());
                assertNull(options.get(6).getName());
                assertEquals(URL.class, options.get(6).getReflectType());
            }
            crestContext.getParameters().set(0, new CustomParam("changedX"));
            return crestContext.proceed();
        }
    }

    @Options
    public static class CustomParam {
        private final String val;

        public CustomParam(@Option("val") final String v) {
            val = v;
        }
    }

    public static class In1 {
        @CrestInterceptor
        public Object intercept1(final CrestContext crestContext) {
            crestContext.getParameters().set(0, "replaced");
            return crestContext.proceed();
        }
    }

    public static class In2 {
        @CrestInterceptor
        public Object intercept2(final CrestContext crestContext) {
            crestContext.getParameters().set(4, null);
            return crestContext.proceed();
        }
    }

    public static class In3 {
        @CrestInterceptor
        public Object intercept3(final CrestContext crestContext) {
            return "mock";
        }
    }
}
