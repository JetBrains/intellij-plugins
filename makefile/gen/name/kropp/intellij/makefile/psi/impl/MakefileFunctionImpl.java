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

import java.util.List;

public class MakefileFunctionImpl extends ASTWrapperPsiElement implements MakefileFunction {

  public MakefileFunctionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitFunction(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public MakefileFunctionName getFunctionName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, MakefileFunctionName.class));
  }

  @Override
  @NotNull
  public List<MakefileFunctionParam> getFunctionParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileFunctionParam.class);
  }

}
