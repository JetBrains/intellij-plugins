/*
 * Copyright 2019 The authors
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

import com.intellij.ide.presentation.Presentation;
import com.intellij.openapi.paths.PathReference;
import com.intellij.struts2.Struts2PresentationProvider;
import com.intellij.struts2.dom.params.ParamsElement;
import com.intellij.struts2.dom.struts.HasResultType;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

/**
 * {@code result}.
 *
 * @author Yann C&eacute;bron
 */
@Convert(StrutsPathReferenceConverter.class)
@Presentation(typeName = "Result", icon = "AllIcons.Vcs.Arrow_right", provider = Struts2PresentationProvider.class)
public interface Result extends HasResultType, ParamsElement, GenericDomValue<PathReference> {

  /**
   * Default result name.
   *
   * @see #getNameOrDefault()
   */
  @NonNls
  String DEFAULT_NAME = "success";

  @Override
  @NameValue(unique = false)
  @Scope(ParentScopeProvider.class)
  GenericAttributeValue<String> getName();

  @Nullable
  String getNameOrDefault();

  /**
   * Returns the <em>local</em> result type. Usually {@link #getEffectiveResultType()} is required.
   *
   * @return null if none defined.
   */
  @Override
  @Convert(ResultTypeResolvingConverter.class)
  GenericAttributeValue<ResultType> getType();
}
