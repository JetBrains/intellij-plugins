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

public class DroolsLhsUnaryImpl extends DroolsPsiCompositeElementImpl implements DroolsLhsUnary {

  public DroolsLhsUnaryImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitLhsUnary(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DroolsLhsAccumulate getLhsAccumulate() {
    return findChildByClass(DroolsLhsAccumulate.class);
  }

  @Override
  @Nullable
  public DroolsLhsEval getLhsEval() {
    return findChildByClass(DroolsLhsEval.class);
  }

  @Override
  @Nullable
  public DroolsLhsExists getLhsExists() {
    return findChildByClass(DroolsLhsExists.class);
  }

  @Override
  @Nullable
  public DroolsLhsForall getLhsForall() {
    return findChildByClass(DroolsLhsForall.class);
  }

  @Override
  @Nullable
  public DroolsLhsNamedConsequence getLhsNamedConsequence() {
    return findChildByClass(DroolsLhsNamedConsequence.class);
  }

  @Override
  @Nullable
  public DroolsLhsNot getLhsNot() {
    return findChildByClass(DroolsLhsNot.class);
  }

  @Override
  @Nullable
  public DroolsLhsOr getLhsOr() {
    return findChildByClass(DroolsLhsOr.class);
  }

  @Override
  @Nullable
  public DroolsLhsPatternBind getLhsPatternBind() {
    return findChildByClass(DroolsLhsPatternBind.class);
  }

}
