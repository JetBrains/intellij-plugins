// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Reference to itself for name element of Cucumber Parameter Type declaration
 * <blockquote><pre>
 *     ParameterType(
 *         name: 'anything',
 *         regexp: /(.*?)/,
 *     )
 * </pre></blockquote>
 * Necessary to find usages of Cucumber Parameter Type
 */
public class CucumberParameterTypeSelfReference extends PsiReferenceBase<PsiElement> {
  public CucumberParameterTypeSelfReference(@NotNull PsiElement element) {
    super(element, TextRange.create(1, element.getTextLength() - 1));
  }

  @Override
  public @Nullable PsiElement resolve() {
    return getElement();
  }
}
