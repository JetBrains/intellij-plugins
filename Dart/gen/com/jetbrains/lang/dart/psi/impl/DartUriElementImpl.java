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
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;

public class DartUriElementImpl extends DartUriElementBase implements DartUriElement {

  public DartUriElementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitUriElement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public DartStringLiteralExpression getStringLiteralExpression() {
    return findNotNullChildByClass(DartStringLiteralExpression.class);
  }

  @Override
  public @NotNull Pair<String, TextRange> getUriStringAndItsRange() {
    return DartPsiImplUtil.getUriStringAndItsRange(this);
  }

}
