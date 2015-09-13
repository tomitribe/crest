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
import org.tomitribe.crest.api.Out;
import org.tomitribe.crest.api.interceptor.CrestContext;
import org.tomitribe.crest.api.interceptor.CrestInterceptor;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import static org.junit.Assert.assertEquals;

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
