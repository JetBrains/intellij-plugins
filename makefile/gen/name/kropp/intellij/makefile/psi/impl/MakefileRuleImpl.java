// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MakefileRuleImpl extends ASTWrapperPsiElement implements MakefileRule {

  public MakefileRuleImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitRule(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileRecipe getRecipe() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileRecipe.class));
  }

  @Override
  @NotNull
  public MakefileTargetLine getTargetLine() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileTargetLine.class));
  }

  @Override
  @NotNull
  public List<MakefileTarget> getTargets() {
    return MakefilePsiImplUtil.getTargets(this);
  }

}
