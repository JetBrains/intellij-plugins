// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.resolve.DartResolver;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class DartContributedReference implements PsiPolyVariantReference {

  private static final DartContributedReferenceResolver RESOLVER = new DartContributedReferenceResolver();

  private final @NotNull PsiElement myElement;
  private final @NotNull DartServerData.DartNavigationRegion myNavigationRegion;
  private final @NotNull TextRange myRefRange;
  private final @NotNull String myRefText;

  DartContributedReference(final @NotNull PsiElement element,
                           final int elementStartOffsetInHost,
                           final @NotNull DartServerData.DartNavigationRegion navigationRegion) {
    myElement = element;
    myNavigationRegion = navigationRegion;
    myRefRange = TextRange.from(navigationRegion.getOffset() - elementStartOffsetInHost, navigationRegion.getLength());
    myRefText = myRefRange.substring(element.getText());
  }

  @Override
  public @NotNull PsiElement getElement() {
    return myElement;
  }

  public @NotNull DartServerData.DartNavigationRegion getNavigationRegion() {
    return myNavigationRegion;
  }

  @Override
  public @NotNull TextRange getRangeInElement() {
    return myRefRange;
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
      ResolveCache.getInstance(myElement.getProject()).resolveWithCaching(this, RESOLVER, true, incompleteCode);
    return DartResolveUtil.toCandidateInfoArray(elements);
  }

  @Override
  public @NotNull String getCanonicalText() {
    return myRefText;
  }

  @Override
  public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
    return getElement();
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return getElement();
  }

  @Override
  public boolean isReferenceTo(@NotNull PsiElement element) {
    return resolve() == element;
  }

  @Override
  public boolean isSoft() {
    return true;
  }

  private static class DartContributedReferenceResolver
    implements ResolveCache.AbstractResolver<DartContributedReference, List<? extends PsiElement>> {

    @Override
    public List<? extends PsiElement> resolve(final @NotNull DartContributedReference reference, boolean incompleteCode) {
      return DartResolver.getTargetElements(reference.getElement().getProject(), reference.getNavigationRegion());
    }
  }
}
