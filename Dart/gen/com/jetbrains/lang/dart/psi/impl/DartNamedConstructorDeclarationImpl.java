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

public class DartNamedConstructorDeclarationImpl extends AbstractDartComponentImpl implements DartNamedConstructorDeclaration {

  public DartNamedConstructorDeclarationImpl(ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public DartComponentName getComponentName() {
    return findNotNullChildByClass(DartComponentName.class);
  }

  @Override
  @NotNull
  public List<DartExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartExpression.class);
  }

  @Override
  @NotNull
  public DartFormalParameterList getFormalParameterList() {
    return findNotNullChildByClass(DartFormalParameterList.class);
  }

  @Override
  @Nullable
  public DartFunctionBody getFunctionBody() {
    return findChildByClass(DartFunctionBody.class);
  }

  @Override
  @Nullable
  public DartInitializers getInitializers() {
    return findChildByClass(DartInitializers.class);
  }

  @Override
  @Nullable
  public DartRedirection getRedirection() {
    return findChildByClass(DartRedirection.class);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitNamedConstructorDeclaration(this);
    else super.accept(visitor);
  }

}
