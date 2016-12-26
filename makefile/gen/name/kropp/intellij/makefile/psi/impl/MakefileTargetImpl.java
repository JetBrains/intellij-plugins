// This is a generated file. Not intended for manual editing.
package name.kropp.intellij.makefile.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import name.kropp.intellij.makefile.psi.MakefilePsiImplUtil;
import name.kropp.intellij.makefile.psi.MakefileTarget;
import name.kropp.intellij.makefile.psi.MakefileVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MakefileTargetImpl extends MakefileNamedElementImpl implements MakefileTarget {

  public MakefileTargetImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull MakefileVisitor visitor) {
    visitor.visitTarget(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof MakefileVisitor) accept((MakefileVisitor)visitor);
    else super.accept(visitor);
  }

  @Nullable
  public String getName() {
    return MakefilePsiImplUtil.getName(this);
  }

  public PsiElement setName(String newName) {
    return MakefilePsiImplUtil.setName(this, newName);
  }

  @Nullable
  public PsiElement getNameIdentifier() {
    return MakefilePsiImplUtil.getNameIdentifier(this);
  }

  public ItemPresentation getPresentation() {
    return MakefilePsiImplUtil.getPresentation(this);
  }

}
