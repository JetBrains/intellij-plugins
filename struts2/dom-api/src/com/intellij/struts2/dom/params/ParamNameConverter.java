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

package com.intellij.struts2.dom.params;

import com.intellij.psi.PsiClass;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.Converter;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Converter for {@link Param#getName()}.
 * <p/>
 * Based on Spring plugin.
 *
 * @author Yann C&eacute;bron
 */
public abstract class ParamNameConverter extends Converter<List<BeanProperty>>
    implements CustomReferenceConverter<List<BeanProperty>> {

  public String toString(@Nullable final List<BeanProperty> beanProperties, final ConvertContext convertContext) {
    return null;
  }

  /**
   * Gets the enclosing parent element.
   *
   * @param context Current context.
   * @return Parent element or <code>null</code> if none found (should not happen in valid XML).
   */
  @Nullable
  protected static DomElement getEnclosingElement(final ConvertContext context) {
    final DomElement current = context.getInvocationElement();
    final DomElement parent = current.getParent();
    return parent != null ? parent.getParent() : null;
  }

  @Nullable
  protected static PsiClass getRootParamsClass(@NotNull final DomElement paramsElement) {
    assert paramsElement instanceof ParamsElement : "parent not ParamsElement: " + paramsElement;
    return ((ParamsElement) paramsElement).getParamsClass();
  }

}