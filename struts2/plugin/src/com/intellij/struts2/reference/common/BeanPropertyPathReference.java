/*
 * Copyright 2013 The authors
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

package com.intellij.struts2.reference.common;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.CreateBeanPropertyFixes;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Provides (part of) path to bean property.
 * <p/>
 * Based on Spring plugin.
 *
 * @author Yann C&eacute;bron
 */
public class BeanPropertyPathReference extends PsiReferenceBase<PsiElement>
  implements EmptyResolveMessageProvider, LocalQuickFixProvider {

  private final BeanPropertyPathReferenceSet referenceSet;
  private final int index;

  BeanPropertyPathReference(final BeanPropertyPathReferenceSet referenceSet,
                            final TextRange range,
                            final int index) {
    super(referenceSet.getElement(), range, referenceSet.isSoft());
    this.referenceSet = referenceSet;
    this.index = index;
  }

  private boolean isLast() {
    return referenceSet.getReferences().size() - 1 == index;
  }

  @Override
  public PsiMethod resolve() {
    final PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return null;
    }

    return resolveProperty(psiClass, getValue());
  }

  @Override
  public Object @NotNull [] getVariants() {
    final PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return ArrayUtilRt.EMPTY_OBJECT_ARRAY;
    }

    final Map<String, PsiMethod> properties =
      PropertyUtilBase.getAllProperties(psiClass, true, !isLast() || referenceSet.isSupportsReadOnlyProperties());

    final Object[] variants = new Object[properties.size()];
    int i = 0;
    for (final Map.Entry<String, PsiMethod> entry : properties.entrySet()) {
      final String propertyName = entry.getKey();

      final PsiMethod psiMethod = entry.getValue();
      final PsiType propertyType = PropertyUtilBase.getPropertyType(psiMethod);
      assert propertyType != null;

      final LookupElementBuilder variant =
        LookupElementBuilder.create(psiMethod, propertyName)
          .withIcon(psiMethod.getIcon(0))
          .withStrikeoutness(psiMethod.isDeprecated())
          .withTypeText(propertyType.getPresentableText());
      variants[i++] = variant;
    }

    return variants;
  }

  @Override
  @NotNull
  public String getUnresolvedMessagePattern() {
    return "Cannot resolve property '" + getValue() + "'";
  }

  @Override
  public PsiElement handleElementRename(@NotNull final String newElementName) throws IncorrectOperationException {
    final String name = PropertyUtilBase.getPropertyName(newElementName);
    return super.handleElementRename(name == null ? newElementName : name);
  }

  @Override
  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiMethod) {
      final String propertyName = PropertyUtilBase.getPropertyName((PsiMember)element);
      if (propertyName != null) {
        return super.handleElementRename(propertyName);
      }
    }
    return getElement();
  }

  @Override
  public @NotNull LocalQuickFix @Nullable [] getQuickFixes() {
    final String value = getValue();
    if (StringUtil.isEmpty(value)) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    return CreateBeanPropertyFixes.createFixes(value, psiClass, null, true);
  }

  @Nullable
  private PsiMethod resolveProperty(@NotNull final PsiClass psiClass, final String propertyName) {
    PsiMethod method = isLast() ?
                       PropertyUtilBase.findPropertySetter(psiClass, propertyName, false, true) :
                       PropertyUtilBase.findPropertyGetter(psiClass, propertyName, false, true);
    if (method == null && referenceSet.isSupportsReadOnlyProperties()) {
      method = PropertyUtilBase.findPropertyGetter(psiClass, propertyName, false, true);
    }
    return method == null || !method.hasModifierProperty(PsiModifier.PUBLIC) ? null : method;
  }

  @Nullable
  private PsiClass getPsiClass() {
    if (index == 0) {
      return referenceSet.getBeanClass();
    }

    final PsiMethod method = referenceSet.getReference(index - 1).resolve();
    if (method != null) {
      final PsiType type = method.getReturnType();
      if (type instanceof PsiClassType) {
        return ((PsiClassType)type).resolve();
      }
    }

    return null;
  }
}
