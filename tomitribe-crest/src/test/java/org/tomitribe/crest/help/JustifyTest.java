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
package org.tomitribe.crest.help;

import org.junit.Assert;
import org.junit.Test;

public class JustifyTest {

    private final String source = "There are two different ways for rsync to contact a remote system: " +
            "using a remote-shell program as the transport (such as ssh or rsh) or contacting an rsync " +
            "daemon directly via TCP. The remote-shell transport is used whenever the source or destination" +
            " path contains a single colon (:) separator after a host specification. Contacting an rsync " +
            "daemon directly happens when the source or destination path contains a double colon (::) " +
            "separator after a host specification, OR when an rsync:// URL is specified (see also the \"USING " +
            "RSYNC-DAEMON FEATURES VIA A REMOTE-SHELL CONNECTION\" section for an exception to this latter rule).";

    @Test
    public void test120() throws Exception {
        final String justified = Justify.wrapAndJustify(source, 120);

        final String expected = "" +
                "There  are  two different ways for rsync to contact a remote system: using a remote-shell program as the transport (such\n" +
                "as ssh or rsh) or contacting an rsync daemon directly via TCP. The remote-shell transport is used whenever the source or\n" +
                "destination  path contains a single colon (:) separator after a host specification.  Contacting an rsync daemon directly\n" +
                "happens when the source or destination path contains a double colon (::) separator after a host specification,  OR  when\n" +
                "an  rsync://  URL  is specified (see also the \"USING RSYNC-DAEMON FEATURES VIA A REMOTE-SHELL CONNECTION\" section for an\n" +
                "exception to this latter rule).";
        Assert.assertEquals(expected, justified);

    }
    @Test
    public void test79() throws Exception {
        final String justified = Justify.wrapAndJustify(source, 79);

        final String expected = "" +
                "There  are  two  different  ways  for rsync to contact a remote system: using a\n" +
                "remote-shell program as the transport (such as ssh or  rsh)  or  contacting  an\n" +
                "rsync daemon directly via TCP.  The remote-shell transport is used whenever the\n" +
                "source or destination path contains a single colon (:) separator after  a  host\n" +
                "specification.   Contacting an rsync daemon directly happens when the source or\n" +
                "destination path contains a double colon (::) separator after a host\n" +
                "specification,  OR  when  an  rsync://  URL  is  specified (see also the \"USING\n" +
                "RSYNC-DAEMON FEATURES VIA A REMOTE-SHELL CONNECTION\" section for  an  exception\n" +
                "to this latter rule).";
        Assert.assertEquals(expected, justified);

    }

}
