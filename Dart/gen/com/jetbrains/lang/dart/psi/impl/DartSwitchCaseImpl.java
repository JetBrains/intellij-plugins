// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;

public class DartSwitchCaseImpl extends DartPsiCompositeElementImpl implements DartSwitchCase {

  public DartSwitchCaseImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitSwitchCase(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartConstantPattern getConstantPattern() {
    return findChildByClass(DartConstantPattern.class);
  }

  @Override
  @Nullable
  public DartExpression getExpression() {
    return findChildByClass(DartExpression.class);
  }

  @Override
  @Nullable
  public DartIdentifierPattern getIdentifierPattern() {
    return findChildByClass(DartIdentifierPattern.class);
  }

  @Override
  @NotNull
  public List<DartLabel> getLabelList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartLabel.class);
  }

  @Override
  @Nullable
  public DartListPattern getListPattern() {
    return findChildByClass(DartListPattern.class);
  }

  @Override
  @Nullable
  public DartLogicalAndPattern getLogicalAndPattern() {
    return findChildByClass(DartLogicalAndPattern.class);
  }

  @Override
  @Nullable
  public DartLogicalOrPattern getLogicalOrPattern() {
    return findChildByClass(DartLogicalOrPattern.class);
  }

  @Override
  @Nullable
  public DartMapPattern getMapPattern() {
    return findChildByClass(DartMapPattern.class);
  }

  @Override
  @Nullable
  public DartObjectPattern getObjectPattern() {
    return findChildByClass(DartObjectPattern.class);
  }

  @Override
  @Nullable
  public DartParenthesizedPattern getParenthesizedPattern() {
    return findChildByClass(DartParenthesizedPattern.class);
  }

  @Override
  @Nullable
  public DartRecordPattern getRecordPattern() {
    return findChildByClass(DartRecordPattern.class);
  }

  @Override
  @Nullable
  public DartRelationalPattern getRelationalPattern() {
    return findChildByClass(DartRelationalPattern.class);
  }

  @Override
  @NotNull
  public DartStatements getStatements() {
    return findNotNullChildByClass(DartStatements.class);
  }

  @Override
  @Nullable
  public DartUnaryPattern getUnaryPattern() {
    return findChildByClass(DartUnaryPattern.class);
  }

  @Override
  @Nullable
  public DartVariablePattern getVariablePattern() {
    return findChildByClass(DartVariablePattern.class);
  }

}
