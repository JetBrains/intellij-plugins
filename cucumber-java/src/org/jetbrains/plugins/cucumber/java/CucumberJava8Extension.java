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
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
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
    final FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    GlobalSearchScope scope = featureFile != null ? featureFile.getResolveScope() : module.getModuleWithDependenciesAndLibrariesScope(true);

    Project project = module.getProject();
    fileBasedIndex.processValues(CucumberJava8StepIndex.INDEX_ID, true, null,
                                 (file, value) -> {
                                   ProgressManager.checkCanceled();

                                   PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                                   if (psiFile == null) {
                                     return true;
                                   }

                                   for (Integer offset : value) {
                                     PsiElement element = psiFile.findElementAt(offset + 1);
                                     final PsiMethodCallExpression methodCallExpression =
                                       PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
                                     if (methodCallExpression != null) {
                                       result.add(new Java8StepDefinition(methodCallExpression));
                                     }
                                   }
                                   return true;
                                 }, scope);
    return result;
  }
}
