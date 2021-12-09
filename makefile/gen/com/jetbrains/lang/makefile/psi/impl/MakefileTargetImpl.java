// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.makefile.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.makefile.psi.MakefileTypes.*;
import com.jetbrains.lang.makefile.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.jetbrains.lang.makefile.stub.MakefileTargetStubElement;
import com.intellij.psi.stubs.IStubElementType;

public class MakefileTargetImpl extends MakefileTargetNamedElementImpl implements MakefileTarget {

  public MakefileTargetImpl(@NotNull ASTNode node) {
    super(node);
  }

  public MakefileTargetImpl(@NotNull MakefileTargetStubElement stub, @NotNull IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitTarget(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefileFunction> getFunctionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileFunction.class);
  }

  @Override
  @NotNull
  public List<MakefileVariableUsage> getVariableUsageList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileVariableUsage.class);
  }

  @Override
  @NotNull
  public String getName() {
    return MakefilePsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String newName) {
    return MakefilePsiImplUtil.setName(this, newName);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return MakefilePsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return MakefilePsiImplUtil.getPresentation(this);
  }

  @Override
  public boolean isSpecialTarget() {
    return MakefilePsiImplUtil.isSpecialTarget(this);
  }

  @Override
  public boolean isPatternTarget() {
    return MakefilePsiImplUtil.isPatternTarget(this);
  }

  @Override
  public boolean matches(@NotNull String prerequisite) {
    return MakefilePsiImplUtil.matches(this, prerequisite);
  }

  @Override
  @Nullable
  public String getDocComment() {
    return MakefilePsiImplUtil.getDocComment(this);
  }

}
