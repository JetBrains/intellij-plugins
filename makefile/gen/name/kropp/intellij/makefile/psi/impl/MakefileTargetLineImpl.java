// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import name.kropp.intellij.makefile.psi.*;

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
  @NotNull
  public MakefileDependencies getDependencies() {
    return findNotNullChildByClass(MakefileDependencies.class);
  }

  public String getTarget() {
    return MakefilePsiImplUtil.getTarget(this);
  }

}
