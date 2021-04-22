// This is a generated file. Not intended for manual editing.
package com.intellij.protobuf.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.protobuf.lang.psi.PbTextTypes.*;
import com.intellij.protobuf.lang.psi.*;
import static com.intellij.protobuf.lang.psi.ProtoTokenTypes.*;

public class PbTextFieldNameImpl extends PbTextFieldNameMixin implements PbTextFieldName {

  public PbTextFieldNameImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbTextVisitor visitor) {
    visitor.visitFieldName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbTextVisitor) accept((PbTextVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PbTextExtensionName getExtensionName() {
    return findChildByClass(PbTextExtensionName.class);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return findChildByType(IDENTIFIER_LITERAL);
  }

}
