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

public class DartElementImpl extends DartPsiCompositeElementImpl implements DartElement {

  public DartElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitElement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartExpression getExpression() {
    return findChildByClass(DartExpression.class);
  }

  @Override
  @Nullable
  public DartForElement getForElement() {
    return findChildByClass(DartForElement.class);
  }

  @Override
  @Nullable
  public DartIfElement getIfElement() {
    return findChildByClass(DartIfElement.class);
  }

  @Override
  @Nullable
  public DartMapEntry getMapEntry() {
    return findChildByClass(DartMapEntry.class);
  }

  @Override
  @Nullable
  public DartSpreadElement getSpreadElement() {
    return findChildByClass(DartSpreadElement.class);
  }

}
