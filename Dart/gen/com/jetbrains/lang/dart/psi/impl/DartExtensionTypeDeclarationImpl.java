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

public class DartExtensionTypeDeclarationImpl extends DartPsiCompositeElementImpl implements DartExtensionTypeDeclaration {

  public DartExtensionTypeDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitExtensionTypeDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartClassBody getClassBody() {
    return findChildByClass(DartClassBody.class);
  }

  @Override
  @NotNull
  public List<DartComponentName> getComponentNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartComponentName.class);
  }

  @Override
  @Nullable
  public DartInterfaces getInterfaces() {
    return findChildByClass(DartInterfaces.class);
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

  @Override
  @Nullable
  public DartTypeParameters getTypeParameters() {
    return findChildByClass(DartTypeParameters.class);
  }

}
