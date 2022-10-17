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

public class DroolsDeclareStatementImpl extends DroolsPsiCompositeElementImpl implements DroolsDeclareStatement {

  public DroolsDeclareStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitDeclareStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DroolsEntryPointDeclaration getEntryPointDeclaration() {
    return findChildByClass(DroolsEntryPointDeclaration.class);
  }

  @Override
  @Nullable
  public DroolsEnumDeclaration getEnumDeclaration() {
    return findChildByClass(DroolsEnumDeclaration.class);
  }

  @Override
  @Nullable
  public DroolsTypeDeclaration getTypeDeclaration() {
    return findChildByClass(DroolsTypeDeclaration.class);
  }

  @Override
  @Nullable
  public DroolsWindowDeclaration getWindowDeclaration() {
    return findChildByClass(DroolsWindowDeclaration.class);
  }

}
