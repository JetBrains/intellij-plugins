// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileBlock;
import name.kropp.intellij.makefile.psi.MakefileBranch;
import name.kropp.intellij.makefile.psi.MakefileTopconditional;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MakefileTopconditionalImpl extends ASTWrapperPsiElement implements MakefileTopconditional {

  public MakefileTopconditionalImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitTopconditional(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefileBlock> getBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileBlock.class);
  }

  @Override
  @NotNull
  public List<MakefileBranch> getBranchList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileBranch.class);
  }

}
