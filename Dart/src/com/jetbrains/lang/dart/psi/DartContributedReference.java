package com.jetbrains.lang.dart.psi;

import com.intellij.lang.injection.InjectedLanguageManager;
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
                                  @NotNull final DartServerData.DartNavigationRegion navigationRegion) {
    myElement = element;
    myNavigationRegion = navigationRegion;
    final int startOffset =
      InjectedLanguageManager.getInstance(element.getProject()).injectedToHost(element, element.getTextRange().getStartOffset());
    myRefRange = TextRange.from(navigationRegion.getOffset() - startOffset, navigationRegion.getLength());
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

    return resolveResults.length == 0 ||
           resolveResults.length > 1 ||
           !resolveResults[0].isValidResult() ? null : resolveResults[0].getElement();
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
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
    return false;
  }

  private static class DartContributedReferenceResolver
    implements ResolveCache.AbstractResolver<DartContributedReference, List<? extends PsiElement>> {

    @Override
    public List<? extends PsiElement> resolve(@NotNull final DartContributedReference reference, boolean incompleteCode) {
      return DartResolver.getTargetElements(reference.getElement().getProject(), reference.getNavigationRegion());
    }
  }
}
