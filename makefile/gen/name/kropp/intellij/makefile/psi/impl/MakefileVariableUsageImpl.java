// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileFunctionName;
import name.kropp.intellij.makefile.psi.MakefileVariable;
import name.kropp.intellij.makefile.psi.MakefileVariableUsage;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileVariableUsageImpl extends ASTWrapperPsiElement implements MakefileVariableUsage {

  public MakefileVariableUsageImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitVariableUsage(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileFunctionName getFunctionName() {
    return PsiTreeUtil.getChildOfType(this, MakefileFunctionName.class);
  }

  @Override
  @Nullable
  public MakefileVariable getVariable() {
    return PsiTreeUtil.getChildOfType(this, MakefileVariable.class);
  }

}
