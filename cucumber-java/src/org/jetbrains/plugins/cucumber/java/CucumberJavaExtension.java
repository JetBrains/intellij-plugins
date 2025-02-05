// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.Query;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.factory.JavaStepDefinitionFactory;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CucumberJavaExtension extends AbstractCucumberJavaExtension {
  private static final String CUCUMBER_JAVA_5_STEP_DEFINITION_ANNOTATION_CLASS_NAME = "io.cucumber.java.StepDefinitionAnnotation";
  public static final @NonNls String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";
  public static final @NonNls String ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "org.zuchini.annotations.StepAnnotation";
  private static final String[] CUCUMBER_JAVA_STEP_DEFINITION_ANNOTATION_CLASSES =
    new String[]{CUCUMBER_JAVA_5_STEP_DEFINITION_ANNOTATION_CLASS_NAME, CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION,
      ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION};

  @Override
  public @NotNull BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(JavaFileType.INSTANCE);
  }

  @Override
  public @NotNull StepDefinitionCreator getStepDefinitionCreator() {
    return new JavaStepDefinitionCreator();
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    PsiClass stepDefAnnotationClass = null;
    for (String className : CUCUMBER_JAVA_STEP_DEFINITION_ANNOTATION_CLASSES) {
      stepDefAnnotationClass = JavaPsiFacade.getInstance(module.getProject()).findClass(className, dependenciesScope);
      if (stepDefAnnotationClass != null) {
        break;
      }
    }

    if (stepDefAnnotationClass == null) {
      return Collections.emptyList();
    }

    JavaStepDefinitionFactory stepDefinitionFactory = JavaStepDefinitionFactory.getInstance(module);
    final List<AbstractStepDefinition> result = new ArrayList<>();
    final Query<PsiClass> stepDefAnnotations = AnnotatedElementsSearch.searchPsiClasses(stepDefAnnotationClass, dependenciesScope);
    for (PsiClass annotationClass : stepDefAnnotations.asIterable()) {
      String annotationClassName = annotationClass.getQualifiedName();
      if (annotationClass.isAnnotationType() && annotationClassName != null) {
        final Query<PsiMethod> javaStepDefinitions = AnnotatedElementsSearch.searchPsiMethods(annotationClass, dependenciesScope);
        for (PsiMethod stepDefMethod : javaStepDefinitions.asIterable()) {
          List<String> annotationValues = CucumberJavaUtil.getStepAnnotationValues(stepDefMethod, annotationClassName);
          for (String annotationValue : annotationValues) {
            result.add(stepDefinitionFactory.buildStepDefinition(stepDefMethod, module, annotationValue));
          }
        }
      }
    }
    return result;
  }
}
