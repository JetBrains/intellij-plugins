// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import name.kropp.intellij.makefile.psi.MakefileCommands;
import name.kropp.intellij.makefile.psi.MakefileConditional;
import name.kropp.intellij.makefile.psi.MakefileRecipe;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileRecipeImpl extends ASTWrapperPsiElement implements MakefileRecipe {

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
  @Nullable
  public MakefileCommands getCommands() {
    return findChildByClass(MakefileCommands.class);
  }

  @Override
  @Nullable
  public MakefileConditional getConditional() {
    return findChildByClass(MakefileConditional.class);
  }

}
