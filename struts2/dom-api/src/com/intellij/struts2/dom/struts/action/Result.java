/*
 * Copyright 2007 The authors
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

import com.intellij.openapi.paths.PathReference;
import com.intellij.struts2.dom.ParamsElement;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;

/**
 * <code>result</code>.
 *
 * @author Yann C&eacute;bron
 */
@Convert(StrutsPathReferenceConverter.class)
public interface Result extends ParamsElement, GenericDomValue<PathReference> {

  /**
   * Default result name.
   */
  @NonNls
  String DEFAULT_NAME = "success";

  @NameValue
  @Scope(ParentScopeProvider.class)
  GenericAttributeValue<String> getName();

  @Convert(ResultTypeResolvingConverter.class)
  GenericAttributeValue<ResultType> getType();
}