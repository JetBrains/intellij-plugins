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

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.struts2.reference.common.BeanPropertyPathReference;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class ParamNameConverterImpl extends ParamNameConverter {

  public List<BeanProperty> fromString(@Nullable final String s, final ConvertContext convertContext) {
    if (s == null) {
      return null;
    }

    final GenericAttributeValue<List<BeanProperty>> value = (GenericAttributeValue<List<BeanProperty>>) convertContext.getInvocationElement();
    final BeanPropertyPathReference[] references = createReferences(value, value.getXmlAttributeValue(), convertContext);
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

  @NotNull
  public BeanPropertyPathReference[] createReferences(final GenericDomValue<List<BeanProperty>> listGenericDomValue,
                                               final PsiElement psiElement,
                                               final ConvertContext convertContext) {
    final DomElement paramsElement = getEnclosingElement(convertContext);
    if (paramsElement == null) {
      return BeanPropertyPathReference.EMPTY_REFERENCE;
    }

    final PsiClass rootPsiClass = getRootParamsClass(paramsElement);
    return new BeanPropertyPathReferenceSet(psiElement, rootPsiClass).getPsiReferences();
  }

}