package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * @author Irina.Chernushina on 3/22/2016.
 */
public class AngularJSModuleReferencesProvider extends PsiReferenceProvider {
  public static final String ANGULAR = "angular";

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new AngularJSModuleReference((JSLiteralExpression)element)};
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
      if (!isAngularModuleReferenceAccurate()) return ResolveResult.EMPTY_ARRAY;
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

    @Override
    public boolean isSoft() {
      return true;
    }

    private boolean isAngularModuleReferenceAccurate() {
      final PsiElement parent = myElement.getParent();
      if (parent instanceof JSArgumentList && parent.getParent() instanceof JSCallExpression
          && ((JSArgumentList)parent).getArguments().length == 1) {
        if (PsiTreeUtil.isAncestor(((JSArgumentList)parent).getArguments()[0], myElement, false)) {
          final JSExpression methodExpression = ((JSCallExpression)parent.getParent()).getMethodExpression();
          if (methodExpression instanceof JSReferenceExpression &&
              JSSymbolUtil
                .isAccurateReferenceExpressionName((JSReferenceExpression)methodExpression, ANGULAR, AngularJSIndexingHandler.MODULE)) {
            return true;
          }
          if (AngularJSReferencesContributor.looksLikeAngularModuleReference(methodExpression)) {
            //noinspection ConstantConditions
            final JSExpression qualifier = ((JSReferenceExpression)methodExpression).getQualifier();
            if (qualifier instanceof JSReferenceExpression) {
              final PsiElement resolve = ((JSReferenceExpression)qualifier).resolve();
              if (resolve instanceof JSVariable && ((JSVariable)resolve).getInitializer() instanceof JSReferenceExpression &&
                  JSSymbolUtil.isAccurateReferenceExpressionName((JSReferenceExpression)((JSVariable)resolve).getInitializer(), ANGULAR)) {
                return true;
              }
            }
          }
        }
      }
      return false;
    }
  }
}
