// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileNormalPrerequisites;
import name.kropp.intellij.makefile.psi.MakefileOrderOnlyPrerequisites;
import name.kropp.intellij.makefile.psi.MakefilePrerequisites;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefilePrerequisitesImpl extends ASTWrapperPsiElement implements MakefilePrerequisites {

  public MakefilePrerequisitesImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitPrerequisites(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileNormalPrerequisites getNormalPrerequisites() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileNormalPrerequisites.class));
  }

  @Override
  @Nullable
  public MakefileOrderOnlyPrerequisites getOrderOnlyPrerequisites() {
    return PsiTreeUtil.getChildOfType(this, MakefileOrderOnlyPrerequisites.class);
  }

}
