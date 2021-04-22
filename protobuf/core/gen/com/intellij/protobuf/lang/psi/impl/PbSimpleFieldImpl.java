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

public class PbSimpleFieldImpl extends PbFieldBase implements PbSimpleField {

  public PbSimpleFieldImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitSimpleField(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PbOptionList getOptionList() {
    return PsiTreeUtil.getChildOfType(this, PbOptionList.class);
  }

  @Override
  @NotNull
  public PbTypeName getTypeName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, PbTypeName.class));
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return findChildByType(IDENTIFIER_LITERAL);
  }

  @Override
  @Nullable
  public PbNumberValue getFieldNumber() {
    return PsiTreeUtil.getChildOfType(this, PbNumberValue.class);
  }

  @Override
  @Nullable
  public PbFieldLabel getDeclaredLabel() {
    return PsiTreeUtil.getChildOfType(this, PbFieldLabel.class);
  }

}
