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

public class MakefileRecipeImpl extends ASTWrapperPsiElement implements MakefileRecipe {

  public MakefileRecipeImpl(@NotNull ASTNode node) {
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
  @NotNull
  public List<MakefileEmptyCommand> getEmptyCommandList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, MakefileEmptyCommand.class);
  }

  public boolean isEmpty() {
    return MakefilePsiImplUtil.isEmpty(this);
  }

}
