// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.BndFileType;

abstract class BndRunConfigurationProducer extends LazyRunConfigurationProducer<BndRunConfigurationBase> {
  @Override
  protected boolean setupConfigurationFromContext(@NotNull BndRunConfigurationBase configuration, @NotNull ConfigurationContext context, @NotNull Ref<PsiElement> source) {
    Location location = context.getLocation();
    if (location != null) {
      VirtualFile file = location.getVirtualFile();
      if (file != null && !file.isDirectory()) {
        String extension = file.getExtension();
        if (BndFileType.BND_RUN_EXT.equals(extension) || BndFileType.BND_EXT.equals(extension)) {
          Boolean hasTestCases = BndLaunchUtil.hasTestCases(file.getPath());
          if (hasTestCases == Boolean.FALSE && configuration instanceof BndRunConfigurationBase.Launch ||
              hasTestCases == Boolean.TRUE && configuration instanceof BndRunConfigurationBase.Test) {
            configuration.setName(context.getModule().getName());
            configuration.getOptions().setBndRunFile(file.getPath());
            return true;
          }
        }
      }
    }

    return false;
  }

  @Override
  public boolean isConfigurationFromContext(@NotNull BndRunConfigurationBase configuration, @NotNull ConfigurationContext context) {
    if (getConfigurationFactory() == configuration.getFactory()) {
      Location location = context.getLocation();
      if (location != null) {
        VirtualFile file = location.getVirtualFile();
        return file != null && !file.isDirectory() && configuration.getOptions().getBndRunFile() != null && VfsUtilCore
          .pathEqualsTo(file, configuration.getOptions().getBndRunFile());
      }
    }

    return false;
  }

  static final class Launch extends BndRunConfigurationProducer {
    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
      return BndRunConfigurationType.getInstance().getConfigurationFactories()[0];
    }
  }

  static final class Test extends BndRunConfigurationProducer {
    @Override
    public @NotNull ConfigurationFactory getConfigurationFactory() {
      return BndRunConfigurationType.getInstance().getConfigurationFactories()[1];
    }
  }
}
