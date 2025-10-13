// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;

public final class CucumberJavaAllFeaturesInFolderRunConfigurationProducer extends CucumberJavaRunConfigurationProducer {
  @Override
  protected CucumberGlueProvider getGlueProvider(@NotNull PsiElement element) {
    if (element instanceof PsiDirectory directory) {
      return new CucumberJavaAllFeaturesInFolderGlueProvider(directory);
    }
    return null;
  }

  @Override
  protected String getConfigurationName(@NotNull ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    return CucumberBundle.message("cucumber.run.all.features", ((PsiDirectory)element).getVirtualFile().getName());
  }

  @Override
  protected @Nullable VirtualFile getFileToRun(ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    if (element instanceof PsiDirectory directory) {
      return directory.getVirtualFile();
    }
    return null;
  }
}
