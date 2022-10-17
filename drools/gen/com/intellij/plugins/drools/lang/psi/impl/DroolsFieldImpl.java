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

public class DroolsFieldImpl extends DroolsPsiFieldImpl implements DroolsField {

  public DroolsFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitField(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DroolsAnnotation> getAnnotationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsAnnotation.class);
  }

  @Override
  @Nullable
  public DroolsConditionalExpr getConditionalExpr() {
    return findChildByClass(DroolsConditionalExpr.class);
  }

  @Override
  @NotNull
  public DroolsFieldName getFieldName() {
    return findNotNullChildByClass(DroolsFieldName.class);
  }

  @Override
  @Nullable
  public DroolsFieldType getFieldType() {
    return findChildByClass(DroolsFieldType.class);
  }

}
