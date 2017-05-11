package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

public class CucumberJavaStepDefinitionSearch implements QueryExecutor<PsiReference, ReferencesSearch.SearchParameters> {
  @Override
  public boolean execute(@NotNull final ReferencesSearch.SearchParameters queryParameters,
                         @NotNull final Processor<PsiReference> consumer) {
    final PsiElement myElement = queryParameters.getElementToSearch();
    if (!(myElement instanceof PsiMethod)) {
      return true;
    }
    final PsiMethod method = (PsiMethod)myElement;
    Boolean isStepDefinition = ReadAction.compute(() -> CucumberJavaUtil.isStepDefinition(method));
    if (!isStepDefinition) {
      return true;
    }

    final PsiAnnotation stepAnnotation =
      ReadAction.compute(() -> CucumberJavaUtil.getCucumberStepAnnotation(method));

    final String regexp = CucumberJavaUtil.getPatternFromStepDefinition(stepAnnotation);
    if (regexp == null) {
      return true;
    }
    return CucumberUtil.findGherkinReferencesToElement(myElement, regexp, consumer, queryParameters.getEffectiveSearchScope());
  }
}
