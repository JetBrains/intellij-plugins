// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import name.kropp.intellij.makefile.psi.MakefileVariable;
import name.kropp.intellij.makefile.psi.MakefileVariableAssignment;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;

public class MakefileVariableAssignmentImpl extends ASTWrapperPsiElement implements MakefileVariableAssignment {

  public MakefileVariableAssignmentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitVariableAssignment(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileVariable getVariable() {
    return findNotNullChildByClass(MakefileVariable.class);
  }

}
