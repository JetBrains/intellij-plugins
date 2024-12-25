// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.groovy;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinition;
import org.jetbrains.plugins.cucumber.groovy.steps.GrStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.AbstractCucumberJavaExtension;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.groovy.GroovyFileType;
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrMethodCall;

import java.util.ArrayList;
import java.util.List;

public final class GrCucumberExtension extends AbstractCucumberJavaExtension {
  @Override
  public boolean isStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return child instanceof GroovyFile && ((GroovyFile)child).getName().endsWith(".groovy");
  }

  @Override
  public boolean isWritableStepLikeFile(@NotNull PsiElement child, @NotNull PsiElement parent) {
    return isStepLikeFile(child, parent);
  }

  @Override
  public @NotNull BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(GroovyFileType.GROOVY_FILE_TYPE);
  }

  @Override
  public @NotNull StepDefinitionCreator getStepDefinitionCreator() {
    return new GrStepDefinitionCreator();
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    final List<AbstractStepDefinition> result = new ArrayList<>();
    final FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
    GlobalSearchScope scope = featureFile != null ? featureFile.getResolveScope() : module.getModuleWithDependenciesAndLibrariesScope(true);

    Project project = module.getProject();
    fileBasedIndex.processValues(GrCucumberStepIndex.INDEX_ID, true, null,
                                 (file, value) -> {
                                   PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                                   if (psiFile == null) {
                                     return true;
                                   }

                                   for (Integer offset : value) {
                                     PsiElement element = psiFile.findElementAt(offset + 1);
                                     GrMethodCall methodCallExpression = PsiTreeUtil.getParentOfType(element, GrMethodCall.class);
                                     if (methodCallExpression != null &&
                                         GrCucumberUtil.getStepDefinitionPattern(methodCallExpression) != null) {
                                       result.add(GrStepDefinition.getStepDefinition(methodCallExpression));
                                     }
                                   }
                                   return true;
                                 }, scope);
    return result;
  }
}
