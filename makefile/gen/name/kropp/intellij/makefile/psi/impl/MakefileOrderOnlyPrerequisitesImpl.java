// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileOrderOnlyPrerequisites;
import name.kropp.intellij.makefile.psi.MakefilePrerequisite;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MakefileOrderOnlyPrerequisitesImpl extends ASTWrapperPsiElement implements MakefileOrderOnlyPrerequisites {

  public MakefileOrderOnlyPrerequisitesImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitOrderOnlyPrerequisites(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefilePrerequisite> getPrerequisiteList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefilePrerequisite.class);
  }

}
