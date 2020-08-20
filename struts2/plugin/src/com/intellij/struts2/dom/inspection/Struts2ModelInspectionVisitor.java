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

package com.intellij.struts2.dom.inspection;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.ExtendableClassConverter;
import com.intellij.struts2.dom.struts.Bean;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.Result;
import com.intellij.struts2.dom.struts.strutspackage.DefaultClassRef;
import com.intellij.struts2.dom.struts.strutspackage.Interceptor;
import com.intellij.struts2.dom.struts.strutspackage.ResultType;
import com.intellij.struts2.dom.struts.strutspackage.StrutsPackage;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.xml.*;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;

import java.util.Collection;
import java.util.Map;

/**
 * Provides extended highlighting for various elements.
 *
 * @author Yann C&eacute;bron
 */
class Struts2ModelInspectionVisitor implements DomElementVisitor {

  private final DomElementAnnotationHolder holder;
  private final boolean myIgnoreExtendableClass;

  Struts2ModelInspectionVisitor(final DomElementAnnotationHolder holder,
                                boolean ignoreExtendableClass) {
    this.holder = holder;
    myIgnoreExtendableClass = ignoreExtendableClass;
  }

  @Override
  public void visitDomElement(final DomElement element) {
  }

  public void visitAction(final Action action) {
    final GenericAttributeValue<PsiClass> actionClass = action.getActionClass();
    if (action.isWildcardMapping() &&
        ConverterUtil.hasWildcardReference(actionClass.getStringValue())) {
      return;
    }

    checkExtendableClassConverter(actionClass);

    // default DOM duplicate highlighting works only for "plain" @NameValue
    MultiMap<String, Result> duplicatedResultNames = MultiMap.create();
    for (Result result : action.getResults()) {
      duplicatedResultNames.putValue(result.getNameOrDefault(), result);
    }

    for (Map.Entry<String, Collection<Result>> entry : duplicatedResultNames.entrySet()) {
      if (entry.getValue().size() == 1) continue;

      for (Result result : entry.getValue()) {
        holder.createProblem(result.getName(), XmlDomBundle.message("dom.inspections.identity",
                                                                    ElementPresentationManager.getTypeNameForObject(result)));
      }
    }
  }

  public void visitBean(final Bean bean) {
    checkExtendableClassConverter(bean.getBeanClass());
  }

  public void visitDefaultClassRef(final DefaultClassRef defaultClassRef) {
    checkExtendableClassConverter(defaultClassRef.getDefaultClass());
  }

  public void visitInterceptor(final Interceptor interceptor) {
    checkExtendableClassConverter(interceptor.getInterceptorClass());
  }

  public void visitResultType(final ResultType resultType) {
    checkExtendableClassConverter(resultType.getResultTypeClass());
  }

  public void visitStrutsPackage(final StrutsPackage strutsPackage) {
    final String namespace = strutsPackage.getNamespace().getStringValue();
    if (namespace != null && !StringUtil.startsWithChar(namespace, '/')) {
      holder.createProblem(strutsPackage.getNamespace(),
                           StrutsBundle.message("dom.highlighting.struts.package.must.start.with.slash"));
    }
  }

  private void checkExtendableClassConverter(final GenericAttributeValue clazzAttributeValue) {
    if (myIgnoreExtendableClass) return;

    final XmlElement xmlElement = DomUtil.getValueElement(clazzAttributeValue);
    if (xmlElement == null) {
      return;
    }

    final PsiReference[] psiReferences = xmlElement.getReferences();
    for (final PsiReference psiReference : psiReferences) {
      final PsiElement resolveElement = psiReference.resolve();
      if (resolveElement instanceof PsiClass) {
        return;
      }
    }

    final String[] referenceTypesUserData = clazzAttributeValue.getUserData(ExtendableClassConverter.REFERENCES_TYPES);
    final String referenceTypes = referenceTypesUserData != null ?
                                  StringUtil.join(referenceTypesUserData, "|") :
                                  StrutsBundle.message("dom.extendable.class.converter.type.class");

    final String message = StrutsBundle.message("dom.extendable.class.converter.cannot.resolve",
                                                referenceTypes,
                                                clazzAttributeValue.getStringValue());

    // merge all available QuickFixes (create class/package(s)..)
    LocalQuickFix[] quickFixes = LocalQuickFix.EMPTY_ARRAY;
    for (final PsiReference psiReference : psiReferences) {
      if (psiReference instanceof LocalQuickFixProvider) {
        final LocalQuickFix[] fixes = ((LocalQuickFixProvider)psiReference).getQuickFixes();
        if (fixes != null) {
          quickFixes = ArrayUtil.mergeArrays(fixes, quickFixes);
        }
      }
    }

    holder.createProblem(clazzAttributeValue, message, quickFixes);
  }
}