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

package com.intellij.struts2.model.constant.contributor;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.struts2.model.constant.ConstantValueConverterClassContributor;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.*;
import com.intellij.util.xml.impl.GenericDomValueReference;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Resolves to Shortcut-name, JAVA-Class or result from {@link ConstantValueConverterClassContributor}.
 */
class ConstantValueClassConverter extends ResolvingConverter<PsiClass> implements CustomReferenceConverter {

  private final JavaClassReferenceProvider javaClassReferenceProvider = new JavaClassReferenceProvider();

  private final Map<String, String> shortCutToPsiClassMap;
  private final boolean hasShortCuts;

  ConstantValueClassConverter(@NonNls @NotNull String baseClass,
                              final Map<String, String> shortCutToPsiClassMap) {
    this.shortCutToPsiClassMap = shortCutToPsiClassMap;
    this.hasShortCuts = !shortCutToPsiClassMap.isEmpty();

    javaClassReferenceProvider.setSoft(true);
    javaClassReferenceProvider.setAllowEmpty(false);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.CONCRETE, Boolean.TRUE);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.NOT_INTERFACE, Boolean.TRUE);
    javaClassReferenceProvider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, new String[]{baseClass});
  }

  @NotNull
  @Override
  public Collection<? extends PsiClass> getVariants(ConvertContext context) {
    return Collections.emptyList();
  }

  @Override
  public PsiClass fromString(@Nullable @NonNls final String s, final ConvertContext convertContext) {
    if (s == null) {
      return null;
    }

    // 1. via shortcut
    if (hasShortCuts) {
      final String shortCutClassName = shortCutToPsiClassMap.get(s);

      if (StringUtil.isNotEmpty(shortCutClassName)) {
        return DomJavaUtil.findClass(shortCutClassName, convertContext.getInvocationElement());
      }
    }

    // 2. first non-null result from extension point contributor (currently only Spring)
    for (final ConstantValueConverterClassContributor converterClassContributor :
      ConstantValueConverterClassContributor.EP_NAME.getExtensionList()) {
      final PsiClass contributorClass = converterClassContributor.fromString(s, convertContext);
      if (contributorClass != null) {
        return contributorClass;
      }
    }

    // 3. via JAVA-class
    final PsiClass psiClass = DomJavaUtil.findClass(s, convertContext.getInvocationElement());
    if (psiClass == null) {
      return null;
    }
    return !psiClass.isInterface() && !psiClass.hasModifierProperty(PsiModifier.ABSTRACT) ? psiClass : null;
  }

  @Override
  public String toString(@Nullable PsiClass aClass, ConvertContext context) {
    return aClass == null ? null : aClass.getName();
  }

  @Override
  @NotNull
  public Set<String> getAdditionalVariants(@NotNull final ConvertContext context) {
    return shortCutToPsiClassMap.keySet();
  }

  @Override
  public PsiReference @NotNull [] createReferences(GenericDomValue value, PsiElement element, ConvertContext context) {
    final PsiReference[] references = javaClassReferenceProvider.getReferencesByElement(element);
    //noinspection unchecked
    return ArrayUtil.append(references, new GenericDomValueReference(value), PsiReference.ARRAY_FACTORY);
  }

  @Override
  public String getErrorMessage(@Nullable final String s, final ConvertContext context) {
    return CodeInsightBundle.message("error.cannot.resolve.class", s);
  }
}
