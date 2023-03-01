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

public class DartPatternVariableDeclarationImpl extends DartPsiCompositeElementImpl implements DartPatternVariableDeclaration {

  public DartPatternVariableDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitPatternVariableDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public DartExpression getExpression() {
    return findNotNullChildByClass(DartExpression.class);
  }

  @Override
  @Nullable
  public DartListPattern getListPattern() {
    return findChildByClass(DartListPattern.class);
  }

  @Override
  @Nullable
  public DartMapPattern getMapPattern() {
    return findChildByClass(DartMapPattern.class);
  }

  @Override
  @NotNull
  public List<DartMetadata> getMetadataList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMetadata.class);
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

}
