// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileDirective;
import name.kropp.intellij.makefile.psi.MakefileFunctionName;
import name.kropp.intellij.makefile.psi.MakefileIdentifier;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MakefileIdentifierImpl extends ASTWrapperPsiElement implements MakefileIdentifier {

  public MakefileIdentifierImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitIdentifier(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefileDirective> getDirectiveList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileDirective.class);
  }

  @Override
  @NotNull
  public List<MakefileFunctionName> getFunctionNameList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileFunctionName.class);
  }

}
