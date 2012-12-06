package com.jetbrains.lang.dart.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.jetbrains.lang.dart.DartFileType;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.psi.impl.DartPsiCompositeElementImpl;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
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
