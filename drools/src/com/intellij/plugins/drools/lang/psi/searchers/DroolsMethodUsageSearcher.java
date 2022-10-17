// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.searchers;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.plugins.drools.DroolsFileType;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.impl.light.LightVariableBuilder;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.RequestResultProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PropertyUtilBase;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public class DroolsMethodUsageSearcher extends QueryExecutorBase<PsiReference, MethodReferencesSearch.SearchParameters> {

  public DroolsMethodUsageSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull MethodReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiMethod method = queryParameters.getMethod();
    final PsiFile containingFile = method.getContainingFile();
    final PsiClass psiClass = method.getContainingClass();
    if (containingFile == null || psiClass == null) return;

    if (!PropertyUtilBase.isSimplePropertyAccessor(method)) return;

    SearchScope scope = queryParameters.getEffectiveSearchScope();
    if (scope instanceof GlobalSearchScope) {
      scope = GlobalSearchScope.getScopeRestrictedByFileTypes((GlobalSearchScope)scope, DroolsFileType.DROOLS_FILE_TYPE);
    }
    final String propName = PropertyUtilBase.getPropertyNameByGetter(method);
    queryParameters.getOptimizer().searchWord(propName, scope, UsageSearchContext.ANY, true, method, new MethodRequestResultProcessor(method));
  }

  private static class MethodRequestResultProcessor extends RequestResultProcessor {
    private final PsiMethod myMethod;

    MethodRequestResultProcessor(PsiMethod method) {
      myMethod = method;
    }

    @Override
    public boolean processTextOccurrence(@NotNull PsiElement element, int offsetInElement, @NotNull Processor<? super PsiReference> consumer) {
      for (PsiReference ref : PsiReferenceService.getService().getReferences(element,
                                                                             new PsiReferenceService.Hints(element, offsetInElement))) {
        if (ReferenceRange.containsOffsetInElement(ref, offsetInElement) && !processReference(consumer, ref, myMethod)) {
          return false;
        }
      }
      return true;
    }

    private static boolean processReference(Processor<? super PsiReference> consumer, PsiReference ref, PsiMethod method) {
      if (!method.isValid()) return true;

      if (ref instanceof ResolvingHint && !((ResolvingHint)ref).canResolveTo(PsiMethod.class)) {
        return true;
      }
      if (ref.isReferenceTo(method)) {
        return consumer.process(ref);
      }
      PsiElement refElement = ref.resolve();
      if (refElement instanceof LightVariableBuilder && method.equals(refElement.getNavigationElement())) {  // mvel declaration "then f3.value ... "
        return consumer.process(ref);
      }
      if (refElement instanceof BeanPropertyElement && method.equals(((BeanPropertyElement)refElement).getMethod())) {
        return consumer.process(ref);
      }

      return true;
    }
  }
}
