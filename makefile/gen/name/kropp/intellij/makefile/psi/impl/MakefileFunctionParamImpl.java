// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.MakefileFunction;
import name.kropp.intellij.makefile.psi.MakefileFunctionName;
import name.kropp.intellij.makefile.psi.MakefileFunctionParam;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileFunctionParamImpl extends ASTWrapperPsiElement implements MakefileFunctionParam {

  public MakefileFunctionParamImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitFunctionParam(this);
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

}
