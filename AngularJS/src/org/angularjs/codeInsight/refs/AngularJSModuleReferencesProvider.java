// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.angularjs.codeInsight.refs;

import com.intellij.lang.javascript.index.JSSymbolUtil;
import com.intellij.lang.javascript.inspections.JSUnusedGlobalSymbolsInspection;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.resolve.CachingPolyReferenceBase;
import com.intellij.lang.javascript.psi.resolve.JSResolveResult;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.ProcessingContext;
import com.intellij.util.containers.ContainerUtil;
import org.angularjs.index.AngularGenericModulesIndex;
import org.angularjs.index.AngularIndexUtil;
import org.angularjs.index.AngularJSIndexingHandler;
import org.angularjs.index.AngularModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AngularJSModuleReferencesProvider extends PsiReferenceProvider {
  public static final String ANGULAR = "angular";

  @Override
  public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    return new PsiReference[]{new AngularJSModuleReference((JSLiteralExpression)element)};
  }

  private static class AngularJSModuleReference extends CachingPolyReferenceBase<JSLiteralExpression> {
    AngularJSModuleReference(JSLiteralExpression element) {
      super(element, ElementManipulators.getValueTextRange(element));
    }

    private String getModuleName() {
      return StringUtil.unquoteString(getCanonicalText());
    }

    @Override
    protected ResolveResult @NotNull [] resolveInner() {
      if (!isAngularModuleReferenceAccurate()) return ResolveResult.EMPTY_ARRAY;
      final String moduleName = getModuleName();
      if (StringUtil.isEmptyOrSpaces(moduleName)) return ResolveResult.EMPTY_ARRAY;
      final CommonProcessors.CollectProcessor<JSImplicitElement> collectProcessor = new CommonProcessors.CollectProcessor<>();
      AngularIndexUtil.multiResolve(getElement().getProject(), AngularModuleIndex.KEY, moduleName, collectProcessor);

      final Collection<JSImplicitElement> results = collectProcessor.getResults();
      if (results.isEmpty()) return getGenericResolvedModules(moduleName);
      List<ResolveResult> resolveResults = ContainerUtil.map(results, JSResolveResult::new);
      return resolveResults.toArray(ResolveResult.EMPTY_ARRAY);
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

    private ResolveResult[] getGenericResolvedModules(String moduleName) {
      final Project project = myElement.getProject();
      final Collection<String> allKeys = AngularIndexUtil.getAllKeys(AngularGenericModulesIndex.KEY, project);
      final List<JSImplicitElement> list = new ArrayList<>();
      for (String key : allKeys) {
        AngularIndexUtil.multiResolve(project, AngularGenericModulesIndex.KEY, key, list::add);
      }
      final List<ResolveResult> result = new ArrayList<>();
      final ArrayDeque<Pair<JSNamedElement, Integer>> wrappers = new ArrayDeque<>(getWrappers(list));
      while (!wrappers.isEmpty()) {
        final Pair<JSNamedElement, Integer> pair = wrappers.removeFirst();
        final JSNamedElement wrapper = pair.getFirst();
        SearchScope scope = wrapper.getUseScope();
        if (scope instanceof LocalSearchScope) {
          scope = GlobalSearchScope.filesScope(project, Arrays.asList(((LocalSearchScope)scope).getVirtualFiles()));
        }
        final GlobalSearchScope scopeForSearch = JSUnusedGlobalSymbolsInspection.skipLibraryFiles(project, (GlobalSearchScope)scope);
        final PsiSearchHelper.SearchCostResult cheapEnoughToSearch =
          PsiSearchHelper.getInstance(project).isCheapEnoughToSearch(wrapper.getName(), scopeForSearch, null, null);
        if (cheapEnoughToSearch == PsiSearchHelper.SearchCostResult.TOO_MANY_OCCURRENCES) {
          continue;
        }

        final JSNamedElement namedElement = PsiTreeUtil.getParentOfType(wrapper.getNameIdentifier(), JSNamedElement.class);
        final Collection<PsiReference> references = JSDefaultRenameProcessor.findReferencesForScope(namedElement, false, scopeForSearch);
        for (PsiReference reference : references) {
          if (!(reference instanceof PsiElement)) continue;
          if (((PsiElement)reference).getParent() instanceof JSProperty &&
              ((JSProperty)((PsiElement)reference).getParent()).getValue() != null &&
              ((JSProperty)((PsiElement)reference).getParent()).getValue().equals(reference)) {
            wrappers.add(Pair.create((JSProperty)((PsiElement)reference).getParent(), pair.getSecond()));
            continue;
          }
          final JSCallExpression expression = PsiTreeUtil.getParentOfType((PsiElement)reference, JSCallExpression.class);
          if (expression == null || !PsiTreeUtil.isAncestor(expression.getMethodExpression(), (PsiElement)reference, false)) continue;
          final JSExpression[] arguments = expression.getArguments();
          if (arguments.length > pair.getSecond() && arguments[pair.getSecond()] instanceof JSLiteralExpression literal) {
            if (literal.isQuotedLiteral() && moduleName.equals(StringUtil.unquoteString(literal.getText()))) {
              result.add(new JSResolveResult((PsiElement)reference));
            }
          }
        }
      }
      return result.isEmpty() ? ResolveResult.EMPTY_ARRAY : result.toArray(ResolveResult.EMPTY_ARRAY);
    }

    private static List<Pair<JSNamedElement, Integer>> getWrappers(List<JSImplicitElement> list) {
      final List<Pair<JSNamedElement, Integer>> wrappers = new ArrayList<>();
      for (JSImplicitElement element : list) {
        final Pair<JSNamedElement, Integer> wrapper = CachedValuesManager.getCachedValue(element, new MyCachedValueProvider(element));
        if (wrapper != null) wrappers.add(wrapper);
      }
      return wrappers;
    }

    @Override
    public boolean isSoft() {
      return true;
    }

    private static class MyCachedValueProvider implements CachedValueProvider<Pair<JSNamedElement, Integer>> {
      private final JSImplicitElement myElement;

      MyCachedValueProvider(JSImplicitElement element) {
        myElement = element;
      }

      @Override
      public @Nullable Result<Pair<JSNamedElement, Integer>> compute() {
        final JSCallExpression callExpression = PsiTreeUtil.getParentOfType(myElement, JSCallExpression.class);
        if (callExpression == null) return null;
        final JSExpression methodExpression = callExpression.getMethodExpression();
        if (methodExpression instanceof JSReferenceExpression && JSSymbolUtil
          .isAccurateReferenceExpressionName((JSReferenceExpression)methodExpression, ANGULAR, AngularJSIndexingHandler.MODULE)) {
          if (callExpression.getArgumentList() == null || callExpression.getArgumentList().getArguments().length <= 1) {
            return null;
          }
          final JSExpression firstArgument = callExpression.getArgumentList().getArguments()[0];
          if (firstArgument instanceof JSReferenceExpression) {
            final PsiElement resolve = ((JSReferenceExpression)firstArgument).resolve();
            if (resolve != null && resolve.isValid() && resolve instanceof JSParameter) {
              final JSFunction function = ((JSParameter)resolve).getDeclaringFunction();
              if (function == null) return null;
              final JSParameter[] variables = function.getParameterVariables();
              int index = 0;
              for (; index < variables.length; index++) {
                JSParameter variable = variables[index];
                if (variable == resolve) break;
              }
              if (index >= variables.length) return null;
              if (function.getName() != null) {
                return Result.create(Pair.create(function, index), myElement.getContainingFile());
              }
            }
          }
        }
        return null;
      }
    }
  }
}
