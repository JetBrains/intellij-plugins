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

package com.intellij.struts2.dom.struts;

import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.Nullable;

/**
 * DomElement providing name and {@link ResultType}.
 *
 * @author Yann C&eacute;bron
 */
public interface HasResultType {

  GenericAttributeValue<String> getName();

  /**
   * Returns the "local" result type (i.e. defined for this tag via attribute).
   *
   * @return Local result type.
   */
  GenericAttributeValue<ResultType> getType();

  /**
   * Determines the effective result type.
   *
   * @return local or parent's {@link StrutsPackage#searchDefaultResultType()}, {@code null} on errors.
   */
  @Nullable
  ResultType getEffectiveResultType();

}