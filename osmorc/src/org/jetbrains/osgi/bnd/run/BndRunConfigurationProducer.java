// Copyright 2000-2017 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.BndFileType;

public abstract class BndRunConfigurationProducer extends RunConfigurationProducer<BndRunConfigurationBase> {
  protected BndRunConfigurationProducer(@NotNull ConfigurationFactory factory) {
    super(factory);
  }

  @Override
  protected boolean setupConfigurationFromContext(BndRunConfigurationBase configuration, ConfigurationContext context, Ref<PsiElement> source) {
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
  public boolean isConfigurationFromContext(BndRunConfigurationBase configuration, ConfigurationContext context) {
    if (getConfigurationFactory() == configuration.getFactory()) {
      Location location = context.getLocation();
      if (location != null) {
        VirtualFile file = location.getVirtualFile();
        return file != null && !file.isDirectory() && FileUtil.pathsEqual(file.getPath(), configuration.getOptions().getBndRunFile());
      }
    }

    return false;
  }

  public static class Launch extends BndRunConfigurationProducer {
    public Launch() {
      super(BndRunConfigurationType.getInstance().getConfigurationFactories()[0]);
    }
  }

  public static class Test extends BndRunConfigurationProducer {
    public Test() {
      super(BndRunConfigurationType.getInstance().getConfigurationFactories()[1]);
    }
  }
}
