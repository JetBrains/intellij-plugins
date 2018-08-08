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
import com.intellij.navigation.ItemPresentation;
import name.kropp.intellij.makefile.stub.MakefileTargetStubElement;
import com.intellij.psi.stubs.IStubElementType;

public class MakefileTargetImpl extends MakefileNamedElementImpl implements MakefileTarget {

  public MakefileTargetImpl(@NotNull ASTNode node) {
    super(node);
  }

  public MakefileTargetImpl(@NotNull MakefileTargetStubElement stub, @NotNull IStubElementType type) {
    super(stub, type);
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

  public boolean isSpecialTarget() {
    return MakefilePsiImplUtil.isSpecialTarget(this);
  }

  public boolean isPatternTarget() {
    return MakefilePsiImplUtil.isPatternTarget(this);
  }

  public boolean matches(String prerequisite) {
    return MakefilePsiImplUtil.matches(this, prerequisite);
  }

  @Nullable
  public String getDocComment() {
    return MakefilePsiImplUtil.getDocComment(this);
  }

}
