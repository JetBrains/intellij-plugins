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

public class DartOptionalParameterTypesImpl extends DartPsiCompositeElementImpl implements DartOptionalParameterTypes {

  public DartOptionalParameterTypesImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitOptionalParameterTypes(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DartNormalParameterType> getNormalParameterTypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartNormalParameterType.class);
  }

  @Override
  @NotNull
  public List<DartType> getTypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartType.class);
  }

}
