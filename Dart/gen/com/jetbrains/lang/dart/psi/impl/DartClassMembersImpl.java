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

public class DartClassMembersImpl extends DartPsiCompositeElementImpl implements DartClassMembers {

  public DartClassMembersImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitClassMembers(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DartFactoryConstructorDeclaration> getFactoryConstructorDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartFactoryConstructorDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartGetterDeclaration> getGetterDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartGetterDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartIncompleteDeclaration> getIncompleteDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartIncompleteDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartMethodDeclaration> getMethodDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMethodDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartNamedConstructorDeclaration> getNamedConstructorDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartNamedConstructorDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartSetterDeclaration> getSetterDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartSetterDeclaration.class);
  }

  @Override
  @NotNull
  public List<DartVarDeclarationList> getVarDeclarationListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartVarDeclarationList.class);
  }

}
