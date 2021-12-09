/*
 * Copyright 2015 The authors
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

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import com.intellij.util.xml.converters.DelimitedListConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Converter for {@link Action#getActionMethods()}.
 *
 * @author Yann C&eacute;bron
 */
public class AllowedMethodsConverter extends DelimitedListConverter<String> {

  @SuppressWarnings("UnusedDeclaration")
  public AllowedMethodsConverter() {
    super(",");
  }

  @Override
  protected String convertString(@Nullable final String s, final ConvertContext convertContext) {
    final Action action = getActionElement(convertContext);
    return action.findActionMethod(s) != null ? s : null;
  }

  @Override
  protected String toString(@Nullable final String psiMethod) {
    return psiMethod;
  }

  @Override
  protected Object[] getReferenceVariants(final ConvertContext convertContext,
                                          final GenericDomValue<? extends List<String>> listGenericDomValue) {
    final Action action = getActionElement(convertContext);
    return ArrayUtil.toObjectArray(action.getActionMethods());
  }

  @Override
  protected PsiElement resolveReference(@Nullable final String psiMethod, final ConvertContext convertContext) {
    final Action action = getActionElement(convertContext);
    return action.findActionMethod(psiMethod);
  }

  @Override
  protected String getUnresolvedMessage(final String s) {
    return "Cannot resolve action-method '" + s + "'";
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