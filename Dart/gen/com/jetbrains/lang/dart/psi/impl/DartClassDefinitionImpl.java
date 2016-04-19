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

public class DartClassDefinitionImpl extends AbstractDartPsiClass implements DartClassDefinition {

  public DartClassDefinitionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitClassDefinition(this);
  }

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
  public DartComponentName getComponentName() {
    return findNotNullChildByClass(DartComponentName.class);
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
  public DartMixinApplication getMixinApplication() {
    return findChildByClass(DartMixinApplication.class);
  }

  @Override
  @Nullable
  public DartMixins getMixins() {
    return findChildByClass(DartMixins.class);
  }

  @Override
  @Nullable
  public DartStringLiteralExpression getStringLiteralExpression() {
    return findChildByClass(DartStringLiteralExpression.class);
  }

  @Override
  @Nullable
  public DartSuperclass getSuperclass() {
    return findChildByClass(DartSuperclass.class);
  }

  @Override
  @Nullable
  public DartTypeParameters getTypeParameters() {
    return findChildByClass(DartTypeParameters.class);
  }

}
