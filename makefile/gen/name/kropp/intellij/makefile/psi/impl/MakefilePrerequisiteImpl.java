// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import name.kropp.intellij.makefile.psi.MakefilePrerequisite;
import name.kropp.intellij.makefile.psi.MakefilePsiImplUtil;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;

public class MakefilePrerequisiteImpl extends MakefilePrerequisiteMixin implements MakefilePrerequisite {

  public MakefilePrerequisiteImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitPrerequisite(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  public MakefilePrerequisite updateText(String newText) {
    return MakefilePsiImplUtil.updateText(this, newText);
  }

  public boolean isPhonyTarget() {
    return MakefilePsiImplUtil.isPhonyTarget(this);
  }

}
