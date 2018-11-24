/*
 * Copyright 2011 The authors
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

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.dom.BeanPropertyPathConverterBase;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;

/**
 * Converter for {@link Param#getName()}.
 * <p/>
 * Based on Spring plugin.
 *
 * @author Yann C&eacute;bron
 */
public abstract class ParamNameConverter extends BeanPropertyPathConverterBase {

  public static final ExtensionPointName<ParamNameCustomConverter> EP_NAME =
    new ExtensionPointName<>("com.intellij.struts2.paramNameCustomConverter");

  /**
   * Provides custom "name" resolving. Replaces the basic bean property resolving.
   */
  public abstract static class ParamNameCustomConverter {

    /**
     * Returns custom references or empty array if not applicable.
     *
     * @param nameAttributeValue    "name" attribute.
     * @param paramsElement
     * @return
     */
    @NotNull
    public abstract PsiReference[] getCustomReferences(final XmlAttributeValue nameAttributeValue, final DomElement paramsElement);
  }
}