// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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

  @NotNull private final PsiElement myElement;
  @NotNull private final DartServerData.DartNavigationRegion myNavigationRegion;
  @NotNull private final TextRange myRefRange;
  @NotNull private final String myRefText;

  DartContributedReference(@NotNull final PsiElement element,
                           final int elementStartOffsetInHost,
                           @NotNull final DartServerData.DartNavigationRegion navigationRegion) {
    myElement = element;
    myNavigationRegion = navigationRegion;
    myRefRange = TextRange.from(navigationRegion.getOffset() - elementStartOffsetInHost, navigationRegion.getLength());
    myRefText = element.getText().substring(myRefRange.getStartOffset(), myRefRange.getEndOffset());
  }

  @NotNull
  @Override
  public PsiElement getElement() {
    return myElement;
  }

  @NotNull
  public DartServerData.DartNavigationRegion getNavigationRegion() {
    return myNavigationRegion;
  }

  @NotNull
  @Override
  public TextRange getRangeInElement() {
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

  @NotNull
  @Override
  public String getCanonicalText() {
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
    public List<? extends PsiElement> resolve(@NotNull final DartContributedReference reference, boolean incompleteCode) {
      return DartResolver.getTargetElements(reference.getElement().getProject(), reference.getNavigationRegion());
    }
  }
}
