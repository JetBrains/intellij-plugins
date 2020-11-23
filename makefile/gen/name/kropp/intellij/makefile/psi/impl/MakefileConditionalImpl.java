// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MakefileConditionalImpl extends ASTWrapperPsiElement implements MakefileConditional {

  public MakefileConditionalImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitConditional(this);
  }

  @Override
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

  @Override
  @NotNull
  public List<MakefileConditionalElse> getConditionalElseList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileConditionalElse.class);
  }

}
