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

package com.intellij.struts2.reference.jsp;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.action.CreateActionMethodQuickFix;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xml.DomUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * "Action"-tags attribute "method".
 *
 * @author Yann C&eacute;bron
 */
public class ActionMethodReferenceProvider extends PsiReferenceProvider {

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull final PsiElement psiElement,
                                                         @NotNull final ProcessingContext context) {
    final XmlAttributeValue methodAttribute = (XmlAttributeValue) psiElement;

    final PsiElement parent = methodAttribute.getParent().getParent();
    if (!(parent instanceof XmlTag tag)) {
      return PsiReference.EMPTY_ARRAY;
    }

    final XmlAttribute action = tag.getAttribute("action");
    if (action == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    final XmlAttributeValue valueElement = action.getValueElement();
    if (valueElement == null) {
      return PsiReference.EMPTY_ARRAY;
    }

    for (final PsiReference psiReference : valueElement.getReferences()) {
      if (psiReference instanceof ActionReferenceProvider.ActionReference) {
        // dynamic cannot be resolved
        final PsiElement resolve = psiReference.resolve();
        if (!(resolve instanceof XmlTag)) {
          continue;
        }

        return new PsiReference[]{new ActionMethodReference(methodAttribute,
                                                            (XmlTag) resolve)};
      }
    }

    return PsiReference.EMPTY_ARRAY;
  }


  private static final class ActionMethodReference
      extends PsiReferenceBase<XmlAttributeValue>
      implements LocalQuickFixProvider, EmptyResolveMessageProvider {

    @Nullable
    private final Action action;
    private final String methodName;

    private ActionMethodReference(@NotNull XmlAttributeValue methodElement,
                                  @NotNull final XmlTag actionTag) {
      super(methodElement);
      this.methodName = methodElement.getValue();
      action = DomUtil.findDomElement(actionTag, Action.class, false);
    }

    @Override
    public PsiElement resolve() {
      if (action == null) {
        return null;
      }

      return ContainerUtil.find(action.getActionMethods(), psiMethod -> Objects.equals(psiMethod.getName(), methodName));
    }

    @Override
    public Object @NotNull [] getVariants() {
      if (action == null) {
        return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
      }

      return ArrayUtil.toObjectArray(action.getActionMethods());
    }

    @NotNull
    @Override
    public String getUnresolvedMessagePattern() {
      return "Cannot resolve action-method '" + methodName + "'";
    }

    // TODO not called
    @Override
    public @NotNull LocalQuickFix @Nullable [] getQuickFixes() {
      if (action == null) {
        return LocalQuickFix.EMPTY_ARRAY;
      }

      final PsiClass actionClass = action.searchActionClass();
      return new LocalQuickFix[]{new CreateActionMethodQuickFix(actionClass, methodName)};
    }

  }

}