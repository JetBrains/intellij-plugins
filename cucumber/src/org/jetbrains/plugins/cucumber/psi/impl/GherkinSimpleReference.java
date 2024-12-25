// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinPsiElement;

public class GherkinSimpleReference implements PsiReference {

  private final GherkinPsiElement myElement;

  public GherkinSimpleReference(GherkinPsiElement element) {
    myElement = element;
  }

  @Override
  public @NotNull PsiElement getElement() {
    return myElement;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
    return new TextRange(0, myElement.getTextLength());
  }

  @Override
  public PsiElement resolve() {
    return myElement;
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myElement.getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    if (myElement instanceof PsiNamedElement) {
      ((PsiNamedElement) myElement).setName(newElementName);
    }
    return myElement;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return myElement;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    PsiElement myResolved = resolve();
    PsiElement resolved = element.getReference() != null ? element.getReference().resolve() : null;
    if (resolved == null) {
      resolved = element;
    }
    return myResolved != null && resolved.equals(myResolved);
  }

  @Override
  public boolean isSoft() {
    return false;
  }
}
