// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.psi.DartLibraryNameElement;
import com.jetbrains.lang.dart.psi.DartReference;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartClassResolveResult;
import com.jetbrains.lang.dart.util.DartElementGenerator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DartLibraryIdBase extends DartExpressionImpl implements DartReference, PsiPolyVariantReference {
  public DartLibraryIdBase(ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull PsiElement getElement() {
    return this;
  }

  @Override
  public PsiReference getReference() {
    return this;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
    final TextRange textRange = getTextRange();
    return new TextRange(0, textRange.getEndOffset() - textRange.getStartOffset());
  }

  @Override
  public @NotNull String getCanonicalText() {
    return getText();
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newLibraryName) throws IncorrectOperationException {
    final DartLibraryNameElement libraryNameElement = DartElementGenerator.createLibraryNameElementFromText(getProject(), newLibraryName);
    if (libraryNameElement != null) {
      getNode().replaceAllChildrenToChildrenOf(libraryNameElement.getNode());
    }
    return this;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return this;
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    return resolve() == element;
  }

  @Override
  public boolean isSoft() {
    return false;
  }

  @Override
  public PsiElement resolve() {
    final ResolveResult[] resolveResults = multiResolve(true);

    return resolveResults.length != 1 ||
           !resolveResults[0].isValidResult() ? null : resolveResults[0].getElement();
  }

  @Override
  public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
    final List<? extends PsiElement> elements =
      ResolveCache.getInstance(getProject()).resolveWithCaching(this, DartResolver.INSTANCE, true, incompleteCode);
    return DartResolveUtil.toCandidateInfoArray(elements);
  }

  @Override
  public @NotNull DartClassResolveResult resolveDartClass() {
    return DartClassResolveResult.EMPTY;
  }

  @Override
  public Object @NotNull [] getVariants() {
    return PsiReference.EMPTY_ARRAY; // completion comes from DAS
    /*
    final VirtualFile vFile = DartResolveUtil.getRealVirtualFile(getContainingFile());
    if (vFile == null) return PsiElement.EMPTY_ARRAY;

    final ProjectFileIndex index = ProjectRootManager.getInstance(getProject()).getFileIndex();
    VirtualFile scopeFolder = vFile.getParent();

    if (scopeFolder != null && scopeFolder.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null) {
      VirtualFile parentFolder = scopeFolder.getParent();
      while (parentFolder != null && index.isInContent(parentFolder) && parentFolder.findChild(PubspecYamlUtil.PUBSPEC_YAML) == null) {
        scopeFolder = parentFolder;
        parentFolder = scopeFolder.getParent();
      }
    }

    if (scopeFolder == null) return PsiElement.EMPTY_ARRAY;

    // scopeFolder is either:
    // - pubspec.yaml file parent if current dart file is at the same level as pubspec.yaml
    // - direct subfolder of dart project root like 'bin' or 'web' if curennt dart file is inside at any level
    // - module content root if there's no pubspec.yaml file
    final GlobalSearchScope scope = GlobalSearchScopesCore.directoryScope(getProject(), scopeFolder, true);
    final CommonProcessors.CollectProcessor<String> processor = new CommonProcessors.CollectProcessor<String>();
    FileBasedIndex.getInstance().processAllKeys(DartLibraryIndex.DART_LIBRARY_INDEX, processor, scope, null);

    return processor.toArray(new String[processor.getResults().size()]);
    */
  }
}
