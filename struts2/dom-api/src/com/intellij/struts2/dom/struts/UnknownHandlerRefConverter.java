/*
 * Copyright 2009 The authors
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

package com.intellij.struts2.dom.struts;

import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.Nullable;

/**
 * Converter for {@link UnknownHandlerRef#getName()}.
 *
 * @author Yann C&eacute;bron
 */
public abstract class UnknownHandlerRefConverter extends ResolvingConverter<Bean> {

  protected static final String UNKNOWN_HANDLER_CLASS = "com.opensymphony.xwork2.UnknownHandler";

  @Override
  public String toString(@Nullable final Bean bean, final ConvertContext convertContext) {
    if (bean == null) {
      return null;
    }

    return bean.getName().getStringValue();
  }

  @Override
  public String getErrorMessage(@Nullable final String value, final ConvertContext convertContext) {
    return "Cannot resolve bean of type " + UNKNOWN_HANDLER_CLASS + " with name '" + value + "'";
  }

}