/*
 * Copyright 2015 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2.dom.struts.action;

import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.Nullable;

/**
 * {@code result} "type"-attribute.
 *
 * @author Yann C&eacute;bron
 * @see Result#getType()
 */
public abstract class ResultTypeResolvingConverter extends ResolvingConverter<ResultType> {

  @Override
  public String toString(@Nullable final ResultType resultType, final ConvertContext context) {
    return resultType != null ? resultType.getName().getStringValue() : null;
  }

  @Override
  public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
    return "Cannot resolve result-type '" + value + "'";
  }

}