// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.template.psi;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class TftplDataLanguageSegmentImpl extends ASTWrapperPsiElement implements TftplDataLanguageSegment {

  public TftplDataLanguageSegmentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull TftplVisitor visitor) {
    visitor.visitDataLanguageSegment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof TftplVisitor) {
      accept((TftplVisitor)visitor);
    }
    else {
      super.accept(visitor);
    }
  }
}