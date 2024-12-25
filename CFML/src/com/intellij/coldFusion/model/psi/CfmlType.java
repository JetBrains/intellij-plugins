// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiTypeVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class CfmlType extends PsiType {
  private final String myName;

  public CfmlType(@NotNull String name) {
    super(PsiAnnotation.EMPTY_ARRAY);
    myName = name;
  }

  @Override
  public @NotNull String getPresentableText() {
    return getCanonicalText();
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myName;
  }

  @Override
  public boolean isValid() {
    return false;
  }

  @Override
  public boolean equalsToText(@NotNull String text) {
    return text.endsWith(myName);
  }

  @Override
  public <A> A accept(@NotNull PsiTypeVisitor<A> visitor) {
    return visitor.visitType(this);
  }

  @Override
  public GlobalSearchScope getResolveScope() {
    return GlobalSearchScope.EMPTY_SCOPE;
  }

  @Override
  public PsiType @NotNull [] getSuperTypes() {
    return EMPTY_ARRAY;
  }
}