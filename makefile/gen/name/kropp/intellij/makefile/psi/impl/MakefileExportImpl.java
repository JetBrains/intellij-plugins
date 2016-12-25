// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import name.kropp.intellij.makefile.psi.MakefileExport;
import name.kropp.intellij.makefile.psi.MakefileVariable;
import name.kropp.intellij.makefile.psi.MakefileVariableAssignment;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileExportImpl extends ASTWrapperPsiElement implements MakefileExport {

  public MakefileExportImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitExport(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileVariable getVariable() {
    return findChildByClass(MakefileVariable.class);
  }

  @Override
  @Nullable
  public MakefileVariableAssignment getVariableAssignment() {
    return findChildByClass(MakefileVariableAssignment.class);
  }

}
