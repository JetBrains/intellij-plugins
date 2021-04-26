// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.protobuf.lang.psi.PbTypes.*;
import com.intellij.protobuf.lang.psi.*;
import com.intellij.protobuf.lang.psi.util.PbPsiImplUtil;
import static com.intellij.protobuf.lang.psi.ProtoTokenTypes.*;

public class PbServiceMethodImpl extends PbServiceMethodMixin implements PbServiceMethod {

  public PbServiceMethodImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitServiceMethod(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PbMethodOptions getMethodOptions() {
    return PsiTreeUtil.getChildOfType(this, PbMethodOptions.class);
  }

  @Override
  @NotNull
  public List<PbServiceMethodType> getServiceMethodTypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbServiceMethodType.class);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return findChildByType(IDENTIFIER_LITERAL);
  }

}
