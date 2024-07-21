// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.hierarchy.call;

import com.intellij.psi.PsiElement;

public class DartCallChild {
  private PsiElement element;
  private PsiElement reference;

  public DartCallChild(PsiElement element, PsiElement reference) {
    this.element = element;
    this.reference = reference;
  }

  public PsiElement getElement() {
    return element;
  }

  public PsiElement getReference() {
    return reference;
  }
}
