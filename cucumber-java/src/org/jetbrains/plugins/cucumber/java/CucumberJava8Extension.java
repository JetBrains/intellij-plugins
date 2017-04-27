package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.Java8StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinition;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;

public class CucumberJava8Extension extends AbstractCucumberJavaExtension {
  @NotNull
  @Override
  public BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(JavaFileType.INSTANCE, "Java 8");
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new Java8StepDefinitionCreator();
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    final List<AbstractStepDefinition> result = new ArrayList<>();

    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);
    final GlobalSearchScope javaFiles = GlobalSearchScope.getScopeRestrictedByFileTypes(dependenciesScope, JavaFileType.INSTANCE);

    String[] keywords = new String[] {"Given", "And", "Then", "But", "When"};
    for (String method : keywords) {
      PsiSearchHelper.SERVICE.getInstance(module.getProject()).processElementsWithWord((element, offsetInElement) -> {
        final PsiElement parent = element.getParent();
        if (parent != null) {
          final PsiReference[] references = parent.getReferences();
          for (PsiReference ref : references) {
            PsiElement resolved = ref.resolve();
            PsiClass psiClass = PsiTreeUtil.getParentOfType(resolved, PsiClass.class);
            if (psiClass != null) {
              final String fqn = psiClass.getQualifiedName();
              if (fqn != null && fqn.startsWith("cucumber.api.java8")) {
                final PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
                result.add(new JavaStepDefinition(methodCallExpression));
              }
            }
          }
        }

        return true;
      }, javaFiles, method, UsageSearchContext.IN_CODE, true);
    }
    return result;
  }
}
