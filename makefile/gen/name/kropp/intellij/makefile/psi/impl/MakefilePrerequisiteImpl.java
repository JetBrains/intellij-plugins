// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static name.kropp.intellij.makefile.psi.MakefileTypes.*;
import name.kropp.intellij.makefile.psi.*;

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

  public MakefilePrerequisiteImpl updateText(String newText) {
    return MakefilePsiImplUtil.updateText(this, newText);
  }

  public boolean isPhonyTarget() {
    return MakefilePsiImplUtil.isPhonyTarget(this);
  }

}
