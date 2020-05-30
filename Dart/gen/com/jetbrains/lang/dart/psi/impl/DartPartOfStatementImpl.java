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
import com.intellij.openapi.vfs.VirtualFile;

public class DartPartOfStatementImpl extends DartPsiCompositeElementImpl implements DartPartOfStatement {

  public DartPartOfStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitPartOfStatement(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartLibraryId getLibraryId() {
    return findChildByClass(DartLibraryId.class);
  }

  @Override
  @NotNull
  public List<DartMetadata> getMetadataList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMetadata.class);
  }

  @Override
  @Nullable
  public DartUriElement getUriElement() {
    return findChildByClass(DartUriElement.class);
  }

  @Override
  @NotNull
  public String getLibraryName() {
    return DartPsiImplUtil.getLibraryName(this);
  }

  @Override
  @NotNull
  public List<VirtualFile> getLibraryFiles() {
    return DartPsiImplUtil.getLibraryFiles(this);
  }

}
