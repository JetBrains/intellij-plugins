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

package com.intellij.struts2.dom.validator.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.search.PackageScope;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.dom.validator.FieldNameConverter;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TODO exclude properties: actionErrors|Messages, fieldErrors, errors
 *
 * @author Yann C&eacute;bron
 */
public class FieldNameConverterImpl extends FieldNameConverter {

  @Override
  protected PsiClass findBeanPropertyClass(@NotNull final DomElement domElement) {
    final XmlElement xmlElement = domElement.getXmlElement();
    assert xmlElement != null;

    final PsiFile psiFile = xmlElement.getContainingFile().getOriginalFile();
    final PsiDirectory containingDirectory = psiFile.getContainingDirectory();
    if (containingDirectory == null) {
      return null;
    }

    final PsiPackage containingPackage = JavaDirectoryService.getInstance().getPackage(containingDirectory);
    if (containingPackage == null) {
      return null;
    }
    // ClassName-[method-]validation.xml
    final String validationFileName = psiFile.getName();
    final String actionClassName = StringUtil.split(validationFileName, "-").get(0);
    final String qualifiedActionClassName = StringUtil.getQualifiedName(containingPackage.getQualifiedName(),
                                                                        actionClassName);
    final JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(psiFile.getProject());
    return psiFacade.findClass(qualifiedActionClassName, new PackageScope(containingPackage, false, false));
  }

  @Override
  public PsiReference @NotNull [] createReferences(final GenericDomValue<List<BeanProperty>> listGenericDomValue,
                                                   final PsiElement psiElement,
                                                   final ConvertContext convertContext) {
    final PsiClass actionClass = findBeanPropertyClass(convertContext.getInvocationElement());
    return new BeanPropertyPathReferenceSet(psiElement, actionClass, false).getPsiReferences();
  }

}