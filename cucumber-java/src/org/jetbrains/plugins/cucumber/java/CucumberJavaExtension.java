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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.BDDFrameworkType;
import org.jetbrains.plugins.cucumber.StepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.AbstractJavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStepDefinitionCreator;
import org.jetbrains.plugins.cucumber.java.steps.factory.JavaStepDefinitionFactory;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CucumberJavaExtension extends AbstractCucumberJavaExtension {
  public static final String CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "cucumber.runtime.java.StepDefAnnotation";
  public static final String ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION = "org.zuchini.annotations.StepAnnotation";

  @NotNull
  @Override
  public BDDFrameworkType getStepFileType() {
    return new BDDFrameworkType(JavaFileType.INSTANCE);
  }

  @NotNull
  @Override
  public StepDefinitionCreator getStepDefinitionCreator() {
    return new JavaStepDefinitionCreator();
  }

  @Override
  public List<AbstractStepDefinition> loadStepsFor(@Nullable PsiFile featureFile, @NotNull Module module) {
    final GlobalSearchScope dependenciesScope = module.getModuleWithDependenciesAndLibrariesScope(true);

    PsiClass stepDefAnnotationClass = JavaPsiFacade.getInstance(module.getProject()).findClass(CUCUMBER_RUNTIME_JAVA_STEP_DEF_ANNOTATION,
                                                                                               dependenciesScope);
    if (stepDefAnnotationClass == null) {
      stepDefAnnotationClass = JavaPsiFacade.getInstance(module.getProject()).findClass(ZUCHINI_RUNTIME_JAVA_STEP_DEF_ANNOTATION,
                                                                                        dependenciesScope);
    }
    if (stepDefAnnotationClass == null) {
      return Collections.emptyList();
    }

    JavaStepDefinitionFactory stepDefinitionFactory = JavaStepDefinitionFactory.getInstance(module);
    final List<AbstractStepDefinition> result = new ArrayList<>();
    final Query<PsiClass> stepDefAnnotations = AnnotatedElementsSearch.searchPsiClasses(stepDefAnnotationClass, dependenciesScope);
    for (PsiClass annotationClass : stepDefAnnotations) {
      String annotationClassName = annotationClass.getQualifiedName();
      if (annotationClass.isAnnotationType() && annotationClassName != null) {
        final Query<PsiMethod> javaStepDefinitions = AnnotatedElementsSearch.searchPsiMethods(annotationClass, dependenciesScope);
        for (PsiMethod stepDefMethod : javaStepDefinitions) {
          AbstractJavaStepDefinition stepDefinition = stepDefinitionFactory.buildStepDefinition(stepDefMethod, annotationClassName);
          result.add(stepDefinition);
        }
      }
    }
    return result;
  }
}
