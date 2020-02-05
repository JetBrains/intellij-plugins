// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileBlock;
import name.kropp.intellij.makefile.psi.MakefileCondition;
import name.kropp.intellij.makefile.psi.MakefileConditionalElse;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileConditionalElseImpl extends ASTWrapperPsiElement implements MakefileConditionalElse {

  public MakefileConditionalElseImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitConditionalElse(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileBlock getBlock() {
    return PsiTreeUtil.getChildOfType(this, MakefileBlock.class);
  }

  @Override
  @Nullable
  public MakefileCondition getCondition() {
    return PsiTreeUtil.getChildOfType(this, MakefileCondition.class);
  }

}
