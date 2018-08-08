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

public class MakefileBlockImpl extends ASTWrapperPsiElement implements MakefileBlock {

  public MakefileBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitBlock(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<MakefileCommand> getCommandList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileCommand.class);
  }

  @Override
  @NotNull
  public List<MakefileConditional> getConditionalList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileConditional.class);
  }

  @Override
  @NotNull
  public List<MakefileDefine> getDefineList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileDefine.class);
  }

  @Override
  @Nullable
  public MakefileEmptyCommand getEmptyCommand() {
    return PsiTreeUtil.getChildOfType(this, MakefileEmptyCommand.class);
  }

  @Override
  @NotNull
  public List<MakefileExport> getExportList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileExport.class);
  }

  @Override
  @NotNull
  public List<MakefileFunction> getFunctionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileFunction.class);
  }

  @Override
  @NotNull
  public List<MakefileInclude> getIncludeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileInclude.class);
  }

  @Override
  @NotNull
  public List<MakefileOverride> getOverrideList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileOverride.class);
  }

  @Override
  @NotNull
  public List<MakefilePrivatevar> getPrivatevarList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefilePrivatevar.class);
  }

  @Override
  @NotNull
  public List<MakefileRule> getRuleList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileRule.class);
  }

  @Override
  @NotNull
  public List<MakefileUndefine> getUndefineList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileUndefine.class);
  }

  @Override
  @NotNull
  public List<MakefileVariableAssignment> getVariableAssignmentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileVariableAssignment.class);
  }

  @Override
  @NotNull
  public List<MakefileVpath> getVpathList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileVpath.class);
  }

}
