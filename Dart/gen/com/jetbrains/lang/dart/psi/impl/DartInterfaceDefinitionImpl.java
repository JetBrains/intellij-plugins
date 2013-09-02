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

public class DartInterfaceDefinitionImpl extends AbstractDartPsiClass implements DartInterfaceDefinition {

  public DartInterfaceDefinitionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitInterfaceDefinition(this);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public DartComponentName getComponentName() {
    return findNotNullChildByClass(DartComponentName.class);
  }

  @Override
  @Nullable
  public DartDefaultFactroy getDefaultFactroy() {
    return findChildByClass(DartDefaultFactroy.class);
  }

  @Override
  @Nullable
  public DartFactorySpecification getFactorySpecification() {
    return findChildByClass(DartFactorySpecification.class);
  }

  @Override
  @Nullable
  public DartInterfaceBody getInterfaceBody() {
    return findChildByClass(DartInterfaceBody.class);
  }

  @Override
  @Nullable
  public DartSuperinterfaces getSuperinterfaces() {
    return findChildByClass(DartSuperinterfaces.class);
  }

  @Override
  @Nullable
  public DartTypeParameters getTypeParameters() {
    return findChildByClass(DartTypeParameters.class);
  }

}
