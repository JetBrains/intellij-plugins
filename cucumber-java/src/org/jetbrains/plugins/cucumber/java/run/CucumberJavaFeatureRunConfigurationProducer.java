// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinFile;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;

public class CucumberJavaFeatureRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Nullable
  @Override
  protected CucumberGlueProvider getGlueProvider(@NotNull final PsiElement element) {
    final PsiFile file = element.getContainingFile();
    if (file instanceof GherkinFile) {
      return new CucumberJavaFeatureGlueProvider(element);
    }

    return null;
  }

  @Override
  protected String getConfigurationName(@NotNull ConfigurationContext context) {
    final VirtualFile featureFile = getFileToRun(context);
    assert featureFile != null;
    return "Feature: " + featureFile.getNameWithoutExtension();
  }

  @Nullable
  @Override
  protected VirtualFile getFileToRun(ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    final GherkinStepsHolder scenario = PsiTreeUtil.getParentOfType(element, GherkinScenario.class, GherkinScenarioOutline.class);
    if (element != null && scenario == null && element.getContainingFile() instanceof GherkinFile) {
      return element.getContainingFile().getVirtualFile();
    }

    return null;
  }
}
