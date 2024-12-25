// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveVisitor;
import org.jetbrains.annotations.NotNull;

public abstract class JdlRecursiveElementVisitor extends JdlVisitor implements PsiRecursiveVisitor {
  @Override
  public void visitElement(final @NotNull PsiElement element) {
    element.acceptChildren(this);
  }
}