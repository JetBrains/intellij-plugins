// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileDirectory;
import name.kropp.intellij.makefile.psi.MakefilePattern;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import name.kropp.intellij.makefile.psi.MakefileVpath;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MakefileVpathImpl extends ASTWrapperPsiElement implements MakefileVpath {

  public MakefileVpathImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitVpath(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefileDirectory> getDirectoryList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileDirectory.class);
  }

  @Override
  @Nullable
  public MakefilePattern getPattern() {
    return PsiTreeUtil.getChildOfType(this, MakefilePattern.class);
  }

}
