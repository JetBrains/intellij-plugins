// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.prisma.lang.psi.PrismaElementTypes.*;
import org.intellij.prisma.lang.psi.*;
import org.intellij.prisma.lang.psi.stubs.PrismaEnumDeclarationStub;
import com.intellij.psi.stubs.IStubElementType;

public class PrismaEnumDeclarationImpl extends PrismaEnumDeclarationMixin implements PrismaEnumDeclaration {

  public PrismaEnumDeclarationImpl(@NotNull ASTNode node) {
    super(node);
  }

  public PrismaEnumDeclarationImpl(@NotNull PrismaEnumDeclarationStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public void accept(@NotNull PrismaVisitor visitor) {
    visitor.visitEnumDeclaration(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PrismaVisitor) accept((PrismaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PrismaEnumDeclarationBlock getEnumDeclarationBlock() {
    return PsiTreeUtil.getChildOfType(this, PrismaEnumDeclarationBlock.class);
  }

  @Override
  @Nullable
  public PsiElement getIdentifier() {
    return findChildByType(IDENTIFIER);
  }

}
