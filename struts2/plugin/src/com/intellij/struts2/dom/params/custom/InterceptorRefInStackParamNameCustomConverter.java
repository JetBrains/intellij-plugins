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

package com.intellij.struts2.dom.params.custom;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.references.PomService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.struts2.dom.params.ParamNameConverter;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorOrStackBase;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorRef;
import com.intellij.struts2.dom.struts.strutspackage.InterceptorStack;
import com.intellij.struts2.reference.common.BeanPropertyPathReferenceSet;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomTarget;
import com.intellij.util.xml.ElementPresentationManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resolves to {@code interceptorName.interceptorProperty} when referencing {@link InterceptorStack}.
 *
 * @author Yann C&eacute;bron
 */
public class InterceptorRefInStackParamNameCustomConverter extends ParamNameConverter.ParamNameCustomConverter {

  @Override
  public PsiReference @NotNull [] getCustomReferences(final XmlAttributeValue nameAttributeValue,
                                                      final DomElement paramsElement) {
    if (!(paramsElement instanceof InterceptorRef interceptorRef)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final InterceptorOrStackBase value = interceptorRef.getName().getValue();
    if (!(value instanceof InterceptorStack stack)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final String text = nameAttributeValue.getValue();
    final boolean hasDot = StringUtil.containsChar(text, '.');
    final int idx = hasDot ? text.indexOf('.') : text.length();
    final String refName = text.substring(0, idx);

    final InterceptorRef resolvedInterceptorRef =
        ContainerUtil.find(stack.getInterceptorRefs(), ref -> Comparing.strEqual(refName, ref.getName().getStringValue()));

    final List<PsiReference> customReferences = new ArrayList<>(2);
    customReferences.add(new InterceptorRefPsiReference(nameAttributeValue,
                                                        TextRange.from(1, idx),
                                                        resolvedInterceptorRef,
                                                        stack));

    if (resolvedInterceptorRef == null) {
      return customReferences.toArray(PsiReference.EMPTY_ARRAY);
    }

    final String propertyText = text.substring(idx + 1);
    final PsiClass paramsClass = resolvedInterceptorRef.getParamsClass();
    final BeanPropertyPathReferenceSet beanPropertyPathReferenceSet =
        new BeanPropertyPathReferenceSet(propertyText, nameAttributeValue, idx + 2, '.', paramsClass, false);

    Collections.addAll(customReferences, beanPropertyPathReferenceSet.getPsiReferences());

    return customReferences.toArray(PsiReference.EMPTY_ARRAY);
  }


  private static final class InterceptorRefPsiReference extends PsiReferenceBase<PsiElement>
      implements EmptyResolveMessageProvider {

    private final InterceptorRef resolvedInterceptorRef;
    private final InterceptorStack interceptorStack;

    private InterceptorRefPsiReference(final XmlAttributeValue psiElement,
                                       final TextRange textRange,
                                       final InterceptorRef resolvedInterceptorRef,
                                       final InterceptorStack interceptorStack) {
      super(psiElement, textRange, true);
      this.resolvedInterceptorRef = resolvedInterceptorRef;
      this.interceptorStack = interceptorStack;
    }

    @Override
    public PsiElement resolve() {
      if (resolvedInterceptorRef == null) {
        return null;
      }

      final InterceptorOrStackBase interceptorDeclaration = resolvedInterceptorRef.getName().getValue();
      if (interceptorDeclaration == null) {
        return null;
      }

      final DomTarget domTarget = DomTarget.getTarget(interceptorDeclaration);
      return domTarget == null ? null : PomService.convertToPsi(domTarget);
    }

    @Override
    public Object @NotNull [] getVariants() {
      final List<InterceptorRef> allInterceptorRefs = interceptorStack.getInterceptorRefs();
      List<LookupElement> names = new ArrayList<>(allInterceptorRefs.size());
      for (InterceptorRef interceptorRef : allInterceptorRefs) {
        final InterceptorOrStackBase resolvedInterceptor = interceptorRef.getName().getValue();
        if (resolvedInterceptor == null) {
          continue;
        }

        final String interceptorName = StringUtil.notNullize(resolvedInterceptor.getName().getStringValue());
        names.add(LookupElementBuilder.create(resolvedInterceptor, interceptorName)
                                      .withIcon(ElementPresentationManager.getIcon(resolvedInterceptor)));
      }
      return names.toArray(LookupElement.EMPTY_ARRAY);
    }

    @NotNull
    @Override
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve interceptor '" + getValue() + "' " +
             "in interceptor-stack '" + interceptorStack.getName().getStringValue() + "'";
    }
  }

}