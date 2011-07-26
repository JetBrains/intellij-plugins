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

package com.intellij.struts2.dom;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * DOM-Converter for resolving path to (nested) bean property.
 *
 * See {@code BeanPropertyPathReference}.
 *
 * @author Yann C&eacute;bron
 */
public abstract class BeanPropertyPathConverterBase extends Converter<List<BeanProperty>>
    implements CustomReferenceConverter<List<BeanProperty>> {

  /**
   * Returns the class to search bean properties for.
   *
   * @param domElement Parent element.
   * @return Class or {@code null} if not determinable.
   */
  @Nullable
  protected abstract PsiClass findBeanPropertyClass(@NotNull final DomElement domElement);

  public String toString(@Nullable final List<BeanProperty> beanProperties, final ConvertContext convertContext) {
    return null;
  }

  public List<BeanProperty> fromString(@Nullable final String s, final ConvertContext convertContext) {
    if (s == null) {
      return null;
    }

    final GenericAttributeValue<List<BeanProperty>> value = (GenericAttributeValue<List<BeanProperty>>) convertContext.getInvocationElement();
    // always BeanPropertyPathReference
    final PsiPolyVariantReference[] references =
        (PsiPolyVariantReference[]) createReferences(value, value.getXmlAttributeValue(), convertContext);
    if (references.length < 1) {
      return null;
    }

    final ResolveResult[] results = references[references.length - 1].multiResolve(false);
    final ArrayList<BeanProperty> list = new ArrayList<BeanProperty>(results.length);
    for (final ResolveResult result : results) {
      final PsiMethod method = (PsiMethod) result.getElement();
      if (method != null) {
        final BeanProperty beanProperty = BeanProperty.createBeanProperty(method);
        ContainerUtil.addIfNotNull(beanProperty, list);
      }
    }
    return list;
  }

  /**
   * Gets the enclosing Tag-DomElement.
   *
   * @param context Current context.
   * @return Parent element or <code>null</code> if none found (should not happen in valid XML).
   */
  @Nullable
  protected DomElement findEnclosingTag(final ConvertContext context) {
    final DomElement current = context.getInvocationElement();
    final DomElement parent = current.getParent();
    return parent != null ? parent.getParent() : null;
  }

}