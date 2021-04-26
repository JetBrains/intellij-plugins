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
import com.intellij.protobuf.lang.stub.PbPackageStatementStub;
import com.intellij.psi.stubs.IStubElementType;

public class PbPackageStatementImpl extends PbPackageStatementMixin implements PbPackageStatement {

  public PbPackageStatementImpl(ASTNode node) {
    super(node);
  }

  public PbPackageStatementImpl(PbPackageStatementStub stub, IStubElementType type) {
    super(stub, type);
  }

  public void accept(@NotNull PbVisitor visitor) {
    visitor.visitPackageStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PbVisitor) accept((PbVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PbPackageName getPackageName() {
    return PsiTreeUtil.getChildOfType(this, PbPackageName.class);
  }

}
