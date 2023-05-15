// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;
import com.intellij.plugins.drools.lang.psi.*;

public class DroolsLhsOOPSegmentImpl extends DroolsPsiCompositeElementImpl implements DroolsLhsOOPSegment {

  public DroolsLhsOOPSegmentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitLhsOOPSegment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DroolsConstraint> getConstraintList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsConstraint.class);
  }

  @Override
  @NotNull
  public DroolsLhsOOPathSegmentId getLhsOOPathSegmentId() {
    return findNotNullChildByClass(DroolsLhsOOPathSegmentId.class);
  }

}
