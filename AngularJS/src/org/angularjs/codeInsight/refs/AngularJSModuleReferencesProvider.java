package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Irina.Chernushina on 3/22/2016.
 */
public class AngularJSModuleReferencesProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[] {new AngularJSModuleReference((JSLiteralExpression)element)};
  }

  private static class AngularJSModuleReference extends AngularPolyReferenceBase<JSLiteralExpression> {
    public AngularJSModuleReference(JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getModuleName() {
      return StringUtil.unquoteString(getCanonicalText());
    }

    @NotNull
    @Override
    protected ResolveResult[] resolveInner() {
      final String moduleName = getModuleName();
      if (StringUtil.isEmptyOrSpaces(moduleName)) return ResolveResult.EMPTY_ARRAY;
      final CommonProcessors.CollectProcessor<JSImplicitElement> collectProcessor = new CommonProcessors.CollectProcessor<>();
      AngularIndexUtil.multiResolve(getElement().getProject(), AngularModuleIndex.KEY, moduleName, collectProcessor);

      final Collection<JSImplicitElement> results = collectProcessor.getResults();
      final List<ResolveResult> resolveResults = ContainerUtil.map(results, AngularIndexUtil.JS_IMPLICIT_TO_RESOLVE_RESULT);
      return resolveResults.toArray(ResolveResult.EMPTY_ARRAY);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      return ArrayUtil.EMPTY_OBJECT_ARRAY;
    }
  }
}
