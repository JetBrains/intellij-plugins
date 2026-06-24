// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.junit4.JUnitTestTreeNodeManager.JUNIT_TEST_TREE_NODE_MANAGER_ARGUMENT;

final class CucumberJUnitRunConfigurationExtension extends RunConfigurationExtension {
  private static final String CUCUMBER_JUNIT_RUNNER_CLASS_MARKER = "Cucumber.class";
  private static final String JUNIT_RUN_WITH_ANNOTATION_CLASS = "org.junit.runner.RunWith";

  @Override
  public <T extends RunConfigurationBase<?>> void updateJavaParameters(@NotNull T configuration,
                                                                       @NotNull JavaParameters params,
                                                                       RunnerSettings runnerSettings) {
    if (!(configuration instanceof JUnitConfiguration unitConfiguration)) {
      return;
    }

    String mainClassName = unitConfiguration.getPersistentData().MAIN_CLASS_NAME;
    if (mainClassName == null) {
      return;
    }

    if (shouldUseCucumberTestTreeNodeManager(unitConfiguration, mainClassName)) {
      params.getClassPath().add(PathUtil.getJarPathForClass(CucumberTestTreeNodeManager.class));
      params.getVMParametersList().add("-D" + JUNIT_TEST_TREE_NODE_MANAGER_ARGUMENT + "=" + CucumberTestTreeNodeManager.class.getName());
    }
  }

  private static boolean shouldUseCucumberTestTreeNodeManager(@NotNull JUnitConfiguration junitConfiguration,
                                                             @NotNull String mainClassName) {
    return ReadAction.nonBlocking(() -> {
      Module module = junitConfiguration.getConfigurationModule().getModule();
      if (module == null) {
        return false;
      }
      GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
      return DumbService.getInstance(module.getProject()).computeWithAlternativeResolveEnabled(
        () -> isCucumberJUnitRunnerClass(junitConfiguration.getProject(), mainClassName, scope));
    }).executeSynchronously();
  }

  private static boolean isCucumberJUnitRunnerClass(@NotNull Project project,
                                                   @NotNull String mainClassName,
                                                   @NotNull GlobalSearchScope scope) {
    PsiClass mainClass = JavaPsiFacade.getInstance(project).findClass(mainClassName, scope);
    if (mainClass == null) {
      return false;
    }

    PsiAnnotation runWithAnnotation = mainClass.getAnnotation(JUNIT_RUN_WITH_ANNOTATION_CLASS);
    if (runWithAnnotation == null) {
      return false;
    }

    PsiNameValuePair[] annotationParameters = runWithAnnotation.getParameterList().getAttributes();
    for (PsiNameValuePair parameter : annotationParameters) {
      PsiAnnotationMemberValue value = parameter.getValue();
      if (value != null && value.getText().equalsIgnoreCase(CUCUMBER_JUNIT_RUNNER_CLASS_MARKER)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
    return configuration instanceof JUnitConfiguration;
  }
}
