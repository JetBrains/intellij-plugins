// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.RunConfigurationExtension;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunnerSettings;
import com.intellij.execution.junit.JUnitConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbService;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.PathUtil;
import org.jetbrains.annotations.NotNull;

import static com.intellij.junit4.JUnitTestTreeNodeManager.JUNIT_TEST_TREE_NODE_MANAGER_ARGUMENT;

public class CucumberJUnitRunConfigurationExtension extends RunConfigurationExtension {
  private static final String CUCUMBER_JUNIT_RUNNER_CLASS_MARKER = "Cucumber.class";
  private static final String JUNIT_RUN_WITH_ANNOTATION_CLASS = "org.junit.runner.RunWith";

  @Override
  public <T extends RunConfigurationBase> void updateJavaParameters(@NotNull T configuration,
                                                                    @NotNull JavaParameters params,
                                                                    RunnerSettings runnerSettings) {
    if (!(configuration instanceof JUnitConfiguration)) {
      return;
    }
    Module module = ((JUnitConfiguration)configuration).getConfigurationModule().getModule();
    GlobalSearchScope scope;
    if (module == null) {
      return;
    }
    scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);

    String mainClassName = ((JUnitConfiguration)configuration).getPersistentData().MAIN_CLASS_NAME;
    DumbService.getInstance(module.getProject()).runWithAlternativeResolveEnabled(() -> {
      PsiClass mainClass =  mainClassName != null
                            ? JavaPsiFacade.getInstance(configuration.getProject()).findClass(mainClassName, scope)
                            : null;
      if (mainClass == null) {
        return;
      }

      PsiAnnotation runWithAnnotation = mainClass.getAnnotation(JUNIT_RUN_WITH_ANNOTATION_CLASS);
      if (runWithAnnotation == null) {
        return;
      }
      PsiNameValuePair[] annotationParameters = runWithAnnotation.getParameterList().getAttributes();
      for (PsiNameValuePair parameter : annotationParameters) {
        PsiAnnotationMemberValue value = parameter.getValue();
        if (value != null && value.getText().equalsIgnoreCase(CUCUMBER_JUNIT_RUNNER_CLASS_MARKER)) {
          params.getClassPath().add(PathUtil.getJarPathForClass(CucumberTestTreeNodeManager.class));
          params.getVMParametersList().add("-D" + JUNIT_TEST_TREE_NODE_MANAGER_ARGUMENT + "=" + CucumberTestTreeNodeManager.class.getName());
          return;
        }
      }
    });
  }

  @Override
  public boolean isApplicableFor(@NotNull RunConfigurationBase<?> configuration) {
    return configuration instanceof JUnitConfiguration;
  }
}
