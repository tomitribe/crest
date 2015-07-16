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
package org.tomitribe.crest.cmds.builder;

import org.tomitribe.crest.api.Options;
import org.tomitribe.crest.cmds.Arguments;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.ComplexParam;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.util.reflect.Parameter;

import java.lang.annotation.Annotation;
import java.util.Collections;

public class OptionsBuilder implements ParameterBuilder {
    @Override
    public Class<? extends Annotation> marker() {
        return Options.class;
    }

    @Override
    public ParamMeta buildParameter(final CmdMethod method, final Parameter parameter) {
        return new ParamMeta(new ComplexParam(method, parameter, parameter.getType()), null, Collections.<String>emptyList());
    }

    @Override
    public Object create(final CmdMethod method, final Param parameter, final Arguments arguments, final Arguments.Needed needed) {
        return ComplexParam.class.cast(parameter).convert(arguments, needed);
    }
}
