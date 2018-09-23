// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
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
public class CucumberParameterTypeSelfReference implements PsiReference {
  @NotNull
  private final PsiElement myElement;

  public CucumberParameterTypeSelfReference(@NotNull PsiElement element) {
    myElement = element;
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
    return new TextRange(1, getElement().getTextLength() - 1);
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return myElement;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return myElement.getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return null;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    return element == myElement;
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
