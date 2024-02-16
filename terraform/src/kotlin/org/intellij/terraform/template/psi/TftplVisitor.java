// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.psi;

import com.intellij.psi.PsiElement;
import org.intellij.terraform.hil.psi.ILElementVisitor;
import org.jetbrains.annotations.NotNull;

public class TftplVisitor extends ILElementVisitor {

  public void visitDataLanguageSegment(@NotNull TftplDataLanguageSegment o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }
}

