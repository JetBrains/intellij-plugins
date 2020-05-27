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

public class MakefileDirectiveImpl extends ASTWrapperPsiElement implements MakefileDirective {

  public MakefileDirectiveImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitDirective(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public MakefileDefine getDefine() {
    return PsiTreeUtil.getChildOfType(this, MakefileDefine.class);
  }

  @Override
  @Nullable
  public MakefileExport getExport() {
    return PsiTreeUtil.getChildOfType(this, MakefileExport.class);
  }

  @Override
  @Nullable
  public MakefileInclude getInclude() {
    return PsiTreeUtil.getChildOfType(this, MakefileInclude.class);
  }

  @Override
  @Nullable
  public MakefileOverride getOverride() {
    return PsiTreeUtil.getChildOfType(this, MakefileOverride.class);
  }

  @Override
  @Nullable
  public MakefilePrivatevar getPrivatevar() {
    return PsiTreeUtil.getChildOfType(this, MakefilePrivatevar.class);
  }

  @Override
  @Nullable
  public MakefileUndefine getUndefine() {
    return PsiTreeUtil.getChildOfType(this, MakefileUndefine.class);
  }

  @Override
  @Nullable
  public MakefileVpath getVpath() {
    return PsiTreeUtil.getChildOfType(this, MakefileVpath.class);
  }

}
