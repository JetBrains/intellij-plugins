package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSParameter;
import com.intellij.lang.javascript.psi.resolve.BaseJSSymbolProcessor;
import com.intellij.lang.javascript.psi.resolve.JSReferenceExpressionResolver;
import com.intellij.lang.javascript.psi.resolve.WalkUpResolveProcessor;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularSymbolIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Dennis.Ushakov
*/
public class AngularJSDIReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[] { new AngularJSDIReference((JSParameter)element) };
  }

  public static class AngularJSDIReference extends AngularJSReferenceBase<JSParameter> {
    public AngularJSDIReference(JSParameter element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    @Nullable
    @Override
    public PsiElement resolveInner() {
      final JSImplicitElement resolve = AngularIndexUtil.resolve(getElement().getProject(), AngularSymbolIndex.KEY, getCanonicalText());
      if (resolve != null) return resolve;

      final String name = getCanonicalText();
      final PsiFile psiFile = getElement().getContainingFile();
      final WalkUpResolveProcessor processor = new WalkUpResolveProcessor(
        name,
        psiFile,
        getElement(),
        BaseJSSymbolProcessor.MatchMode.Any
      );
      processor.allowPartialResults();

      JSReferenceExpressionResolver.processAllSymbols(processor);

      final ResolveResult[] results = processor.getResults();
      return results.length == 1 ? results[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }

    @Override
    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
      return getElement().setName(newElementName);
    }
  }
}