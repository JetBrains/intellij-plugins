// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.searchers;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.plugins.drools.lang.psi.DroolsFile;
import com.intellij.plugins.drools.lang.psi.DroolsFunctionStatement;
import com.intellij.plugins.drools.lang.psi.DroolsVariable;
import com.intellij.plugins.drools.lang.psi.util.DroolsBeanPropertyLightVariable;
import com.intellij.plugins.drools.lang.psi.util.DroolsLightClass;
import com.intellij.plugins.drools.lang.psi.util.processors.DroolsFunctionsProcessor;
import com.intellij.psi.*;
import com.intellij.psi.impl.beanProperties.BeanProperty;
import com.intellij.psi.impl.beanProperties.BeanPropertyElement;
import com.intellij.psi.search.RequestResultProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public class DroolsFilesSearcher extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

  public DroolsFilesSearcher() {
    super(true);
  }

  @Override
  public void processQuery(@NotNull ReferencesSearch.SearchParameters parameters, @NotNull Processor<? super PsiReference> consumer) {
    final PsiElement search = parameters.getElementToSearch();
    if (search instanceof DroolsLightClass) {
      DroolsLightClass lightClass = (DroolsLightClass)search;
      String lightClassName = lightClass.getName();
      if (lightClassName != null) {
        parameters.getOptimizer()
          .searchWord(lightClassName, parameters.getEffectiveSearchScope(), UsageSearchContext.ANY, false, lightClass.getDelegate());
      }
    }
    if (search instanceof DroolsVariable) {
      String name = ((DroolsVariable)search).getName();
      if (!StringUtil.isEmptyOrSpaces(name)) {
        if (name.startsWith("$") && name.length() > 1) {
          parameters.getOptimizer()
            .searchWord(name.substring(1), parameters.getEffectiveSearchScope(), UsageSearchContext.ANY, false, search);
        }
      }
    }
    else if (search instanceof PsiLocalVariable) {
      final String name = ((PsiLocalVariable)search).getName();
      if (!StringUtil.isEmptyOrSpaces(name)) {
        parameters.getOptimizer().searchWord(name, parameters.getEffectiveSearchScope(), UsageSearchContext.ANY, false, search);
      }
    }
    else if (search instanceof BeanPropertyElement) {
      searchBeanPropertyElement(parameters, (BeanPropertyElement)search);
    }
    else if (search instanceof DroolsFunctionStatement) {
      searchFunction(parameters, (DroolsFunctionStatement)search);
    }
  }

  private static void searchBeanPropertyElement(ReferencesSearch.SearchParameters parameters, BeanPropertyElement search) {
    if (search.getContainingFile() instanceof DroolsFile) {
      final String propertyName = search.getName();
      final PsiMethod method = search.getMethod();

      parameters.getOptimizer().searchWord(propertyName, parameters.getEffectiveSearchScope(), UsageSearchContext.ANY, false, method,
                                           new DroolsBeanPropertyResultProcessor(search));
      parameters.getOptimizer().searchWord(method.getName(), parameters.getEffectiveSearchScope(), UsageSearchContext.ANY, false, method);
    }
  }

  private static void searchFunction(ReferencesSearch.SearchParameters parameters, DroolsFunctionStatement search) {
    final String name = search.getName();
    parameters.getOptimizer().searchWord(name, parameters.getEffectiveSearchScope(), UsageSearchContext.ANY, false,
                                         DroolsFunctionsProcessor.createLightMethodBuilder(search));
  }

  private static class DroolsBeanPropertyResultProcessor extends RequestResultProcessor {
    private final BeanPropertyElement myPropertyElement;

    DroolsBeanPropertyResultProcessor(@NotNull BeanPropertyElement propertyElement) {
      myPropertyElement = propertyElement;
    }

    @Override
    public boolean processTextOccurrence(@NotNull PsiElement element, int offsetInElement, @NotNull Processor<? super PsiReference> consumer) {
      for (PsiReference ref : PsiReferenceService.getService().getReferences(element,
                                                                             new PsiReferenceService.Hints(element, offsetInElement))) {
        if (ReferenceRange.containsOffsetInElement(ref, offsetInElement) && !processReference(consumer, ref, myPropertyElement)) {
          return false;
        }
      }
      return true;
    }

    private static boolean processReference(Processor<? super PsiReference> consumer, PsiReference ref, BeanPropertyElement propertyElement) {
      if (!propertyElement.isValid()) return true;

      if (ref instanceof ResolvingHint && !((ResolvingHint)ref).canResolveTo(PsiMethod.class)) {
        return true;
      }
      if (ref.isReferenceTo(propertyElement)) {
        return consumer.process(ref);
      }
      PsiElement refElement = ref.resolve();
      if (refElement instanceof DroolsBeanPropertyLightVariable &&
          propertyElement.equals(
            ((DroolsBeanPropertyLightVariable)refElement).getBeanProperty().getPsiElement())) {  // mvel declaration "then f3.value ... "
        return consumer.process(ref);
      }
      return true;
    }
  }
}
