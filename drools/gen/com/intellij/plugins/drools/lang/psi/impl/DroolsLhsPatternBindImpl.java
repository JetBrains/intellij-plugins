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

public class DroolsLhsPatternBindImpl extends DroolsLhsPatternBindVariableImpl implements DroolsLhsPatternBind {

  public DroolsLhsPatternBindImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitLhsPatternBind(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DroolsAnnotation getAnnotation() {
    return findChildByClass(DroolsAnnotation.class);
  }

  @Override
  @NotNull
  public List<DroolsLhsPattern> getLhsPatternList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsLhsPattern.class);
  }

  @Override
  @Nullable
  public DroolsNameId getNameId() {
    return findChildByClass(DroolsNameId.class);
  }

}
