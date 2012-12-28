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

public class DartLibraryStatementImpl extends DartPsiCompositeElementImpl implements DartLibraryStatement {

  public DartLibraryStatementImpl(ASTNode node) {
    super(node);
  }

  @Override
  @Nullable
  public DartPathOrLibraryReference getPathOrLibraryReference() {
    return findChildByClass(DartPathOrLibraryReference.class);
  }

  @Override
  @Nullable
  public DartQualifiedComponentName getQualifiedComponentName() {
    return findChildByClass(DartQualifiedComponentName.class);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitLibraryStatement(this);
    else super.accept(visitor);
  }

  @NotNull
  public String getLibraryName() {
    return DartPsiImplUtil.getLibraryName(this);
  }

}
