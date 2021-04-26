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

public class PbTextMessageValueImpl extends PbTextMessageValueMixin implements PbTextMessageValue {

  public PbTextMessageValueImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbTextVisitor visitor) {
    visitor.visitMessageValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbTextVisitor) accept((PbTextVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PbTextField> getFields() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbTextField.class);
  }

}
