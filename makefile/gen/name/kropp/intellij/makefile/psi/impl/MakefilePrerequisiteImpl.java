// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefilePrerequisiteImpl extends MakefilePrerequisiteMixin implements MakefilePrerequisite {

  public MakefilePrerequisiteImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitPrerequisite(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileFunction getFunction() {
    return PsiTreeUtil.getChildOfType(this, MakefileFunction.class);
  }

  @Override
  @Nullable
  public MakefileFunctionName getFunctionName() {
    return PsiTreeUtil.getChildOfType(this, MakefileFunctionName.class);
  }

  @Override
  @Nullable
  public MakefileVariableUsage getVariableUsage() {
    return PsiTreeUtil.getChildOfType(this, MakefileVariableUsage.class);
  }

  @Override
  @NotNull
  public MakefilePrerequisiteImpl updateText(@NotNull String newText) {
    return MakefilePsiImplUtil.updateText(this, newText);
  }

  @Override
  public boolean isPhonyTarget() {
    return MakefilePsiImplUtil.isPhonyTarget(this);
  }

}
