package com.jetbrains.lang.dart.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.impl.DartPsiCompositeElementImpl;
import com.jetbrains.lang.dart.resolve.DartUseScope;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

public class DartFile extends PsiFileBase implements DartExecutionScope {
  public DartFile(@NotNull FileViewProvider viewProvider) {
    super(viewProvider, DartLanguage.INSTANCE);
  }

  @NotNull
  @Override
  public FileType getFileType() {
    return DartFileType.INSTANCE;
  }

  @Override
  public String toString() {
    return "Dart File";
  }

  @NotNull
  @Override
  public SearchScope getUseScope() {
    final VirtualFile file = DartResolveUtil.getRealVirtualFile(getContainingFile());

    if (file == null || !ProjectRootManager.getInstance(getProject()).getFileIndex().isInContent(file)) {
      return super.getUseScope();
    }

    return new DartUseScope(getProject(), file);
  }

  @Override
  public boolean processDeclarations(@NotNull PsiScopeProcessor processor,
                                     @NotNull ResolveState state,
                                     PsiElement lastParent,
                                     @NotNull PsiElement place) {
    return DartPsiCompositeElementImpl.processDeclarationsImpl(this, processor, state, lastParent)
           && super.processDeclarations(processor, state, lastParent, place);
  }

  @Override
  public IElementType getTokenType() {
    return getNode().getElementType();
  }
}
