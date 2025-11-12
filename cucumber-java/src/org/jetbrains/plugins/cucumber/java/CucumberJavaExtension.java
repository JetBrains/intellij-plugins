// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaAnnotatedStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NotNullByDefault
public class CucumberJavaExtension extends AbstractCucumberJavaExtension {
  private static final Logger LOG = Logger.getInstance(CucumberJavaExtension.class);

  @Override
  public BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(JavaFileType.INSTANCE);
  }

  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new JavaStepDefinitionCreator();
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    final long stepLoadingStart = System.currentTimeMillis();
    final List<AbstractStepDefinition> stepDefinitions = new ArrayList<>();
    final Collection<PsiClass> allStepAnnotationClasses = CucumberJavaUtil.getAllStepAnnotationClasses(module, dependenciesScope);
    for (PsiClass annotationClass : allStepAnnotationClasses) {
      String annotationClassName = annotationClass.getQualifiedName();
      if (annotationClass.isAnnotationType() && annotationClassName != null) {
        final Query<PsiMethod> javaStepDefinitions = AnnotatedElementsSearch.searchPsiMethods(annotationClass, dependenciesScope);
        stepDefinitions.addAll(javaStepDefinitions
                                 .allowParallelProcessing()
                                 .transforming(
                                   stepDefMethod -> CucumberJavaUtil.getCucumberStepAnnotations(stepDefMethod, annotationClassName))
                                 .mapping(annotation -> JavaAnnotatedStepDefinition.create(annotation, module))
                                 .findAll());
      }
    }
    final long stepLoadingEnd = System.currentTimeMillis();
    LOG.trace("loaded " + stepDefinitions.size() + " step definitions in " + (stepLoadingEnd - stepLoadingStart) + "ms");
    return stepDefinitions;
  }

  @Override
  public boolean isGherkin6Supported(Module module) {
    return CucumberJavaVersionUtil.isCucumber60orMore(module);
  }
}
