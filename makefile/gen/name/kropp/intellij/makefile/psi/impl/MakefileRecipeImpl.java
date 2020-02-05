// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import name.kropp.intellij.makefile.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MakefileRecipeImpl extends MakefileRecipeElementImpl implements MakefileRecipe {

  public MakefileRecipeImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitRecipe(this);
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
  public boolean isEmpty() {
    return MakefilePsiImplUtil.isEmpty(this);
  }

}
