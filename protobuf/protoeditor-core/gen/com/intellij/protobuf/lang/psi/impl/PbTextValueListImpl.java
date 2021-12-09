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

public class PbTextValueListImpl extends PbTextElementBase implements PbTextValueList {

  public PbTextValueListImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbTextVisitor visitor) {
    visitor.visitValueList(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbTextVisitor) accept((PbTextVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PbTextIdentifierValue> getIdentifierValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbTextIdentifierValue.class);
  }

  @Override
  @NotNull
  public List<PbTextMessageValue> getMessageValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbTextMessageValue.class);
  }

  @Override
  @NotNull
  public List<PbTextNumberValue> getNumberValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbTextNumberValue.class);
  }

  @Override
  @NotNull
  public List<PbTextStringValue> getStringValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbTextStringValue.class);
  }

  @Override
  @NotNull
  public PsiElement getStart() {
    return findNotNullChildByType(LBRACK);
  }

  @Override
  @Nullable
  public PsiElement getEnd() {
    return findChildByType(RBRACK);
  }

}
