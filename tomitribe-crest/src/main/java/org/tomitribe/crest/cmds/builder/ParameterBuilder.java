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

import org.tomitribe.crest.cmds.Arguments;
import org.tomitribe.crest.cmds.CmdMethod;
import org.tomitribe.crest.cmds.processors.Param;
import org.tomitribe.util.reflect.Parameter;

import java.lang.annotation.Annotation;
import java.util.Collection;

public interface ParameterBuilder {
    Class<? extends Annotation> marker();

    ParamMeta buildParameter(CmdMethod method, Parameter parameter);

    Object create(CmdMethod method, Param parameter, Arguments arguments, Arguments.Needed needed);

    class ParamMeta {
        private final Param param;
        private final String mainOption;
        private final Collection<String> aliases;

        public ParamMeta(final Param param, final String mainOption, final Collection<String> aliases) {
            this.param = param;
            this.mainOption = mainOption;
            this.aliases = aliases;
        }

        public Param getParam() {
            return param;
        }

        public String getMainOption() {
            return mainOption;
        }

        public Collection<String> getAliases() {
            return aliases;
        }
    }
}
