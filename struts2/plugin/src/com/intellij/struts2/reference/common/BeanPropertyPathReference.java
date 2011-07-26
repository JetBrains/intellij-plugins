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

package com.intellij.struts2.reference.common;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.CreateBeanPropertyFix;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.util.ArrayUtil;
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
    implements PsiPolyVariantReference, EmptyResolveMessageProvider, LocalQuickFixProvider {

  public static final BeanPropertyPathReference[] EMPTY_REFERENCE = new BeanPropertyPathReference[0];

  private final BeanPropertyPathReferenceSet referenceSet;
  private final int index;

  BeanPropertyPathReference(final BeanPropertyPathReferenceSet referenceSet,
                            final TextRange range,
                            final int index) {
    super(referenceSet.getElement(), range, true);
    this.referenceSet = referenceSet;
    this.index = index;
  }

  private boolean isLast() {
    return referenceSet.getReferences().size() - 1 == index;
  }

  public PsiMethod resolve() {
    final ResolveResult[] resolveResults = multiResolve(false);
    return (PsiMethod) (resolveResults.length == 1 ? resolveResults[0].getElement() : null);
  }

  @NotNull
  public Object[] getVariants() {
    final PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    final Map<String, PsiMethod> properties = PropertyUtil.getAllProperties(psiClass, true, !isLast());

    final Object[] variants = new Object[properties.size()];
    int i = 0;
    for (final Map.Entry<String, PsiMethod> entry : properties.entrySet()) {
      final String propertyName = entry.getKey();

      final PsiMember member = entry.getValue();
      final PsiType propertyType = PropertyUtil.getPropertyType(member);
      assert propertyType != null;

      final LookupElementBuilder variant =
          LookupElementBuilder.create(propertyName)
                              .setIcon(member.getIcon(Iconable.ICON_FLAG_OPEN))
                              .setTypeText(propertyType.getPresentableText());
      variants[i++] = variant;
    }

    return variants;
  }

  @NotNull
  public ResolveResult[] multiResolve(final boolean incompleteCode) {
    final PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    final PsiMethod method = resolveProperty(psiClass, getValue());
    if (method == null) {
      return ResolveResult.EMPTY_ARRAY;
    }

    return new ResolveResult[]{new PsiElementResolveResult(method)};
  }

  public String getUnresolvedMessagePattern() {
    return "Cannot resolve property ''" + getValue() + "''";
  }

  public PsiElement handleElementRename(final String newElementName) throws IncorrectOperationException {
    final String name = PropertyUtil.getPropertyName(newElementName);
    return super.handleElementRename(name == null ? newElementName : name);
  }

  public PsiElement bindToElement(@NotNull final PsiElement element) throws IncorrectOperationException {
    if (element instanceof PsiMethod) {
      final String propertyName = PropertyUtil.getPropertyName((PsiMember) element);
      if (propertyName != null) {
        return super.handleElementRename(propertyName);
      }
    }
    return getElement();
  }

  public LocalQuickFix[] getQuickFixes() {
    final String value = getValue();
    if (StringUtil.isEmpty(value)) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    final PsiClass psiClass = getPsiClass();
    if (psiClass == null) {
      return LocalQuickFix.EMPTY_ARRAY;
    }

    return CreateBeanPropertyFix.createFixes(value, psiClass, null, true);
  }

  @Nullable
  private PsiMethod resolveProperty(@NotNull final PsiClass psiClass, final String propertyName) {
    final PsiMethod method = isLast() ? PropertyUtil.findPropertySetter(psiClass, propertyName, false, true) :
        PropertyUtil.findPropertyGetter(psiClass, propertyName, false, true);
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
        return ((PsiClassType) type).resolve();
      }
    }

    return null;
  }

}