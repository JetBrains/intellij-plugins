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

public class DartNormalFormalParameterImpl extends DartPsiCompositeElementImpl implements DartNormalFormalParameter {

  public DartNormalFormalParameterImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitNormalFormalParameter(this);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartComponentName getComponentName() {
    return findChildByClass(DartComponentName.class);
  }

  @Override
  @Nullable
  public DartFieldFormalParameter getFieldFormalParameter() {
    return findChildByClass(DartFieldFormalParameter.class);
  }

  @Override
  @Nullable
  public DartFunctionDeclaration getFunctionDeclaration() {
    return findChildByClass(DartFunctionDeclaration.class);
  }

  @Override
  @Nullable
  public DartVarDeclaration getVarDeclaration() {
    return findChildByClass(DartVarDeclaration.class);
  }

  @Nullable
  public DartComponentName findComponentName() {
    return DartPsiImplUtil.findComponentName(this);
  }

}
