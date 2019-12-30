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

public class Option implements Element {
    private final String flag;
    private final Document document;

    public Option(final String flag, final Document document) {
        this.flag = flag;
        this.document = document;
    }

    public String getFlag() {
        return flag;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public String getContent() {
        return null;
    }

    @Override
    public String toString() {
        return "Option{" +
                "flag='" + flag + '\'' +
                '}';
    }


}
