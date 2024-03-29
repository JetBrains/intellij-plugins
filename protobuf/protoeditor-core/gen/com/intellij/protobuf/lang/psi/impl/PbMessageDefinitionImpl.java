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
import com.intellij.protobuf.lang.stub.PbMessageDefinitionStub;
import com.intellij.psi.stubs.IStubElementType;
import static com.intellij.protobuf.lang.psi.ProtoTokenTypes.*;

public class PbMessageDefinitionImpl extends PbMessageDefinitionMixin implements PbMessageDefinition {

  public PbMessageDefinitionImpl(ASTNode node) {
    super(node);
  }

  public PbMessageDefinitionImpl(PbMessageDefinitionStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitMessageDefinition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return findChildByType(IDENTIFIER_LITERAL);
  }

  @Override
  @Nullable
  public PbMessageBody getBody() {
    return PsiTreeUtil.getChildOfType(this, PbMessageBody.class);
  }

}
