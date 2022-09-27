// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.psi.PsiElement;

public class DartCallChild {
  private final PsiElement myElement;
  private final PsiElement myReference;

  public DartCallChild(PsiElement element, PsiElement reference) {
    this.myElement = element;
    this.myReference = reference;
  }

  public PsiElement getElement() {
    return myElement;
  }

  public PsiElement getReference() {
    return myReference;
  }
}
