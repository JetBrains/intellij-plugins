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
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class ParamNameConverterImpl extends ParamNameConverter {

  @Override
  protected PsiClass findBeanPropertyClass(@NotNull final DomElement domElement) {
    return ((ParamsElement) domElement).getParamsClass();
  }

  @Override
  public PsiReference @NotNull [] createReferences(final GenericDomValue<List<BeanProperty>> listGenericDomValue,
                                                   final PsiElement psiElement,
                                                   final ConvertContext convertContext) {
    final DomElement paramsElement = findEnclosingTag(convertContext);
    if (paramsElement == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    for (ParamNameCustomConverter customConverter : EP_NAME.getExtensionList()) {
      final PsiReference[] customReferences = customConverter.getCustomReferences((XmlAttributeValue) psiElement,
                                                                                  paramsElement);
      if (customReferences.length > 0) {
        return customReferences;
      }
    }

    final PsiClass rootPsiClass = findBeanPropertyClass(paramsElement);
    return new BeanPropertyPathReferenceSet(psiElement, rootPsiClass, false).getPsiReferences();
  }

}