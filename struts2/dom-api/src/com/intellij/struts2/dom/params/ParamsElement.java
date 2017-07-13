/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.dom.params;

import com.intellij.psi.PsiClass;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base-interface for all tags containing {@code param} child-tags.
 *
 * @author Yann C&eacute;bron
 */
public interface ParamsElement extends DomElement {

  @NotNull
  @SubTagList(value = "param")
  List<Param> getParams();

  /**
   * Returns the underlying class of the parent element.
   *
   * @return Underlying class or null if not applicable.
   */
  @Nullable
  PsiClass getParamsClass();

}