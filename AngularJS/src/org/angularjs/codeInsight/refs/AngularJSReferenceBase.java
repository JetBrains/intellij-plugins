package org.angularjs.codeInsight.refs;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Dennis.Ushakov
 */
public abstract class AngularJSReferenceBase<T extends PsiElement> extends PsiReferenceBase<T> {
  public AngularJSReferenceBase(T element, TextRange textRange) {
    super(element, textRange, true);
  }

  @Override
  public PsiElement resolve() {
    return ResolveCache.getInstance(getElement().getProject()).resolveWithCaching(this, MyResolver.INSTANCE, false, false);
  }

  public abstract @Nullable PsiElement resolveInner();

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    AngularJSReferenceBase that = (AngularJSReferenceBase)o;

    if (!myElement.equals(that.myElement)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return myElement.hashCode();
  }

  private static class MyResolver implements ResolveCache.Resolver {
    private static final MyResolver INSTANCE = new MyResolver();

    @Override
    public @Nullable PsiElement resolve(@NotNull PsiReference ref, boolean incompleteCode) {
      return ((AngularJSReferenceBase<?>)ref).resolveInner();
    }
  }
}
