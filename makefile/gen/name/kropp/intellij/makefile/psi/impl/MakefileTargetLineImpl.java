// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import name.kropp.intellij.makefile.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileTargetLineImpl extends ASTWrapperPsiElement implements MakefileTargetLine {

  public MakefileTargetLineImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitTargetLine(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileOverride getOverride() {
    return findChildByClass(MakefileOverride.class);
  }

  @Override
  @Nullable
  public MakefilePrerequisites getPrerequisites() {
    return findChildByClass(MakefilePrerequisites.class);
  }

  @Override
  @Nullable
  public MakefilePrivatevar getPrivatevar() {
    return findChildByClass(MakefilePrivatevar.class);
  }

  @Override
  @Nullable
  public MakefileTargetPattern getTargetPattern() {
    return findChildByClass(MakefileTargetPattern.class);
  }

  @Override
  @NotNull
  public MakefileTargets getTargets() {
    return findNotNullChildByClass(MakefileTargets.class);
  }

  @Override
  @Nullable
  public MakefileVariableAssignment getVariableAssignment() {
    return findChildByClass(MakefileVariableAssignment.class);
  }

  @Nullable
  public String getTargetName() {
    return MakefilePsiImplUtil.getTargetName(this);
  }

}
