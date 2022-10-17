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

public class DroolsTypeDeclarationImpl extends DroolsPsiClassImpl implements DroolsTypeDeclaration {

  public DroolsTypeDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitTypeDeclaration(this);
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
  @NotNull
  public List<DroolsField> getFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DroolsField.class);
  }

  @Override
  @Nullable
  public DroolsSuperType getSuperType() {
    return findChildByClass(DroolsSuperType.class);
  }

  @Override
  @Nullable
  public DroolsTraitable getTraitable() {
    return findChildByClass(DroolsTraitable.class);
  }

  @Override
  @NotNull
  public DroolsTypeName getTypeName() {
    return findNotNullChildByClass(DroolsTypeName.class);
  }

}
