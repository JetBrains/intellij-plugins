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

public class DartParameterTypeListImpl extends DartPsiCompositeElementImpl implements DartParameterTypeList {

  public DartParameterTypeListImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitParameterTypeList(this);
  }

  @Override
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
  @Nullable
  public DartOptionalParameterTypes getOptionalParameterTypes() {
    return findChildByClass(DartOptionalParameterTypes.class);
  }

}
