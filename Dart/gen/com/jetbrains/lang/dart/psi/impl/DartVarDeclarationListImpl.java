// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;

public class DartVarDeclarationListImpl extends DartPsiCompositeElementImpl implements DartVarDeclarationList {

  public DartVarDeclarationListImpl(ASTNode node) {
    super(node);
  }

  @Override
  @NotNull
  public DartVarAccessDeclaration getVarAccessDeclaration() {
    return findNotNullChildByClass(DartVarAccessDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartVarDeclarationListPart> getVarDeclarationListPartList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartVarDeclarationListPart.class);
  }

  @Override
  @Nullable
  public DartVarInit getVarInit() {
    return findChildByClass(DartVarInit.class);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitVarDeclarationList(this);
    else super.accept(visitor);
  }

}
