// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @Nullable
  @Override
  public PsiElement resolve() {
    return getElement();
  }
}
