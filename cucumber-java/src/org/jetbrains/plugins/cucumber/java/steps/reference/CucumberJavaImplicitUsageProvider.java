package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.codeInsight.daemon.ImplicitUsageProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.CommonProcessors;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.psi.GherkinFileType;

@NotNullByDefault
public final class CucumberJavaImplicitUsageProvider implements ImplicitUsageProvider {
  @Override
  public boolean isImplicitUsage(PsiElement element) {
    if (element instanceof PsiClass psiClass) {
      return CucumberJavaUtil.isStepDefinitionClass(psiClass);
    }
    else if (element instanceof PsiMethod method) {
      if (CucumberJavaUtil.isHook(method) || CucumberJavaUtil.isParameterType(method)) return true;
      if (CucumberJavaUtil.isAnnotationStepDefinition(method)) {
        final CommonProcessors.FindFirstProcessor<PsiReference> processor = new CommonProcessors.FindFirstProcessor<>();
        final GlobalSearchScope projectScope = GlobalSearchScope.projectScope(element.getProject());
        final GlobalSearchScope restrictedScope = GlobalSearchScope.getScopeRestrictedByFileTypes(projectScope, GherkinFileType.INSTANCE);
        final ReferencesSearch.SearchParameters searchParameters = new ReferencesSearch.SearchParameters(method, restrictedScope, false);
        ReferencesSearch.search(searchParameters).forEach(processor);
        return processor.isFound();
      }
    }
    return false;
  }

  @Override
  public boolean isImplicitRead(PsiElement element) {
    return false;
  }

  @Override
  public boolean isImplicitWrite(PsiElement element) {
    return false;
  }
}
