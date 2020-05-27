// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static name.kropp.intellij.makefile.psi.MakefileTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import name.kropp.intellij.makefile.psi.*;

public class MakefileTargetLineImpl extends ASTWrapperPsiElement implements MakefileTargetLine {

  public MakefileTargetLineImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitTargetLine(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileInlineCommand getInlineCommand() {
    return PsiTreeUtil.getChildOfType(this, MakefileInlineCommand.class);
  }

  @Override
  @Nullable
  public MakefileOverride getOverride() {
    return PsiTreeUtil.getChildOfType(this, MakefileOverride.class);
  }

  @Override
  @Nullable
  public MakefilePrerequisites getPrerequisites() {
    return PsiTreeUtil.getChildOfType(this, MakefilePrerequisites.class);
  }

  @Override
  @Nullable
  public MakefilePrivatevar getPrivatevar() {
    return PsiTreeUtil.getChildOfType(this, MakefilePrivatevar.class);
  }

  @Override
  @Nullable
  public MakefileTargetPattern getTargetPattern() {
    return PsiTreeUtil.getChildOfType(this, MakefileTargetPattern.class);
  }

  @Override
  @NotNull
  public MakefileTargets getTargets() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileTargets.class));
  }

  @Override
  @Nullable
  public MakefileVariableAssignment getVariableAssignment() {
    return PsiTreeUtil.getChildOfType(this, MakefileVariableAssignment.class);
  }

  @Override
  @Nullable
  public String getTargetName() {
    return MakefilePsiImplUtil.getTargetName(this);
  }

}
