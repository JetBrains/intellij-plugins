package org.angularjs.codeInsight.refs;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Irina.Chernushina on 2/12/2016.
 */
public abstract class AngularPolyReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> implements PsiPolyVariantReference {
  public AngularPolyReferenceBase(T element,
                                  TextRange rangeInElement,
                                  boolean soft) {
    super(element, rangeInElement, soft);
  }

  public AngularPolyReferenceBase(T element, TextRange rangeInElement) {
    super(element, rangeInElement);
  }

  public AngularPolyReferenceBase(T element, boolean soft) {
    super(element, soft);
  }

  public AngularPolyReferenceBase(@NotNull T element) {
    super(element);
  }

  @NotNull
  @Override
  public ResolveResult[] multiResolve(boolean incompleteCode) {
    return ResolveCache.getInstance(getElement().getProject()).resolveWithCaching(this, MyResolver.INSTANCE, false, false);
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    final ResolveResult[] results = multiResolve(false);
    for (ResolveResult result : results) {
      if (getElement().getManager().areElementsEquivalent(result.getElement(), element)) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    ResolveResult[] resolveResults = multiResolve(false);
    return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
  }

  @NotNull
  protected abstract ResolveResult[] resolveInner();


  private static class MyResolver implements ResolveCache.PolyVariantResolver<PsiPolyVariantReference> {
    private final static MyResolver INSTANCE = new MyResolver();

    @NotNull
    @Override
    public ResolveResult[] resolve(@NotNull PsiPolyVariantReference reference, boolean incompleteCode) {
      return ((AngularPolyReferenceBase) reference).resolveInner();
    }
  }
}
