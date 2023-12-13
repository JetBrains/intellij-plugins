// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
  protected CucumberGlueProvider getGlueProvider(@NotNull final PsiElement element) {
    if (element instanceof PsiDirectory) {
      return new CucumberJavaAllFeaturesInFolderGlueProvider((PsiDirectory) element);
    }
    return null;
  }

  @Override
  protected String getConfigurationName(@NotNull final ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    return CucumberBundle.message("cucumber.run.all.features", ((PsiDirectory)element).getVirtualFile().getName());
  }

  @Nullable
  @Override
  protected VirtualFile getFileToRun(ConfigurationContext context) {
    final PsiElement element = context.getPsiLocation();
    if (element instanceof PsiDirectory) {
      return ((PsiDirectory) element).getVirtualFile();
    }
    return null;
  }
}
