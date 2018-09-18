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
package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.Java8StepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.Java8StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.List;

public class CucumberJava8Extension extends AbstractCucumberJavaExtension {
  private static final String[] KEYWORDS = {"Given", "And", "Then", "But", "When"};
  private static final String CUCUMBER_API_JAVA8_PACKAGE = "cucumber.api.java8";

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

    for (String method : KEYWORDS) {
      CucumberJava8TextOccurenceProcessor occurenceProcessor = new CucumberJava8TextOccurenceProcessor(result);
      PsiSearchHelper.getInstance(module.getProject()).processElementsWithWord(occurenceProcessor, javaFiles, method,
                                                                                       UsageSearchContext.IN_CODE, true);
    }
    return result;
  }

  private static class CucumberJava8TextOccurenceProcessor implements TextOccurenceProcessor {
    private final List<AbstractStepDefinition> myResult;

    CucumberJava8TextOccurenceProcessor(List<AbstractStepDefinition> result) {
      myResult = result;
    }

    @Override
    public boolean execute(@NotNull PsiElement element, int offsetInElement) {
      PsiElement parent = element.getParent();
      if (PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class) == null || parent == null) {
        return true;
      }

      final PsiReference[] references = parent.getReferences();
      for (PsiReference ref : references) {
        PsiElement resolved = ref.resolve();
        PsiClass psiClass = PsiTreeUtil.getParentOfType(resolved, PsiClass.class);
        if (psiClass != null) {
          final String fqn = psiClass.getQualifiedName();
          if (fqn != null && fqn.startsWith(CUCUMBER_API_JAVA8_PACKAGE)) {
            final PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
            if (methodCallExpression != null) {
              myResult.add(new Java8StepDefinition(methodCallExpression));
            }
          }
        }
      }

      return true;
    }
  }
}
