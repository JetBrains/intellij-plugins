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

public class DartLogicalOrPatternImpl extends DartPsiCompositeElementImpl implements DartLogicalOrPattern {

  public DartLogicalOrPatternImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitLogicalOrPattern(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DartConstantPattern> getConstantPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartConstantPattern.class);
  }

  @Override
  @NotNull
  public List<DartIdentifierPattern> getIdentifierPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartIdentifierPattern.class);
  }

  @Override
  @NotNull
  public List<DartListPattern> getListPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartListPattern.class);
  }

  @Override
  @NotNull
  public List<DartLogicalAndPattern> getLogicalAndPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartLogicalAndPattern.class);
  }

  @Override
  @Nullable
  public DartLogicalOrPattern getLogicalOrPattern() {
    return findChildByClass(DartLogicalOrPattern.class);
  }

  @Override
  @NotNull
  public List<DartMapPattern> getMapPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMapPattern.class);
  }

  @Override
  @NotNull
  public List<DartObjectPattern> getObjectPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartObjectPattern.class);
  }

  @Override
  @NotNull
  public List<DartParenthesizedPattern> getParenthesizedPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartParenthesizedPattern.class);
  }

  @Override
  @NotNull
  public List<DartRecordPattern> getRecordPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartRecordPattern.class);
  }

  @Override
  @NotNull
  public List<DartRelationalPattern> getRelationalPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartRelationalPattern.class);
  }

  @Override
  @NotNull
  public List<DartUnaryPattern> getUnaryPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartUnaryPattern.class);
  }

  @Override
  @NotNull
  public List<DartVariablePattern> getVariablePatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartVariablePattern.class);
  }

}
