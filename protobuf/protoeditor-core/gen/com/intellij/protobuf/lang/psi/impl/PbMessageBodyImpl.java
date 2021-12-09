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

public class PbMessageBodyImpl extends PbMessageBodyMixin implements PbMessageBody {

  public PbMessageBodyImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitMessageBody(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<PbEnumDefinition> getEnumDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbEnumDefinition.class);
  }

  @Override
  @NotNull
  public List<PbExtendDefinition> getExtendDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbExtendDefinition.class);
  }

  @Override
  @NotNull
  public List<PbExtensionsStatement> getExtensionsStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbExtensionsStatement.class);
  }

  @Override
  @NotNull
  public List<PbGroupDefinition> getGroupDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbGroupDefinition.class);
  }

  @Override
  @NotNull
  public List<PbMapField> getMapFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbMapField.class);
  }

  @Override
  @NotNull
  public List<PbMessageDefinition> getMessageDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbMessageDefinition.class);
  }

  @Override
  @NotNull
  public List<PbOneofDefinition> getOneofDefinitionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbOneofDefinition.class);
  }

  @Override
  @NotNull
  public List<PbReservedStatement> getReservedStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbReservedStatement.class);
  }

  @Override
  @NotNull
  public List<PbSimpleField> getSimpleFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbSimpleField.class);
  }

  @Override
  @NotNull
  public List<PbOptionStatement> getOptionStatements() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, PbOptionStatement.class);
  }

  @Override
  @NotNull
  public PsiElement getStart() {
    return notNullChild(findChildByType(LBRACE));
  }

  @Override
  @Nullable
  public PsiElement getEnd() {
    return findChildByType(RBRACE);
  }

}
