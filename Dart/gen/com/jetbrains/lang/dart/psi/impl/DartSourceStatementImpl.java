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

public class DartSourceStatementImpl extends DartPsiCompositeElementImpl implements DartSourceStatement {

  public DartSourceStatementImpl(ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public DartPathOrLibraryReference getPathOrLibraryReference() {
    return findChildByClass(DartPathOrLibraryReference.class);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitSourceStatement(this);
    else super.accept(visitor);
  }

  @NotNull
  public String getPath() {
    return DartPsiImplUtil.getPath(this);
  }

}
