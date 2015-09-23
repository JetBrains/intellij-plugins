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

public class DartFactoryConstructorDeclarationImpl extends AbstractDartComponentImpl implements DartFactoryConstructorDeclaration {

  public DartFactoryConstructorDeclarationImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitFactoryConstructorDeclaration(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DartComponentName> getComponentNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartComponentName.class);
  }

  @Override
  @Nullable
  public DartExpression getExpression() {
    return findChildByClass(DartExpression.class);
  }

  @Override
  @Nullable
  public DartFormalParameterList getFormalParameterList() {
    return findChildByClass(DartFormalParameterList.class);
  }

  @Override
  @Nullable
  public DartFunctionBody getFunctionBody() {
    return findChildByClass(DartFunctionBody.class);
  }

  @Override
  @NotNull
  public List<DartMetadata> getMetadataList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMetadata.class);
  }

  @Override
  @Nullable
  public DartType getType() {
    return findChildByClass(DartType.class);
  }

  @Nullable
  public DartComponentName getComponentName() {
    return DartPsiImplUtil.getComponentName(this);
  }

}
