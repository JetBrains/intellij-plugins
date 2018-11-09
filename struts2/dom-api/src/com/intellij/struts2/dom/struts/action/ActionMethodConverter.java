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

package com.intellij.struts2.dom.struts.action;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * {@code action} "method"
 *
 * @author Yann C&eacute;bron
 */
public class ActionMethodConverter extends ResolvingConverter<PsiMethod> {

  @Override
  @NotNull
  public Collection<? extends PsiMethod> getVariants(final ConvertContext context) {
    final Action action = getActionElement(context);
    return action.getActionMethods();
  }

  @Override
  public PsiMethod fromString(@Nullable @NonNls final String value, final ConvertContext context) {
    if (value == null) {
      return null;
    }

    final Action action = getActionElement(context);
    return action.findActionMethod(value);
  }


  @Override
  public String toString(@Nullable final PsiMethod psiMethod, final ConvertContext context) {
    return psiMethod != null ? psiMethod.getName() : null;
  }

  @Override
  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return "Cannot resolve action-method '" + s + "'";
  }

  @Override
  public LocalQuickFix[] getQuickFixes(final ConvertContext context) {
    final Action action = getActionElement(context);
    final String methodName = action.getMethod().getStringValue();
    final PsiClass actionClass = action.searchActionClass();

    return new LocalQuickFix[]{new CreateActionMethodQuickFix(actionClass, methodName)};
  }

  /**
   * Gets the enclosing {@code action}-element for the current context.
   *
   * @param context Current context.
   * @return Action-element.
   */
  @NotNull
  private static Action getActionElement(final ConvertContext context) {
    final DomElement domElement = context.getInvocationElement();
    final Action action = domElement.getParentOfType(Action.class, false);
    assert action != null : "not triggered within <action> for " + domElement.getXmlElement();
    return action;
  }

}