/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.RunConfigurationProducer;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.osgi.bnd.BndFileType;

public class BndRunConfigurationProducer extends RunConfigurationProducer<BndRunConfiguration> {
  public BndRunConfigurationProducer() {
    super(BndRunConfigurationType.getInstance());
  }

  @Override
  protected boolean setupConfigurationFromContext(BndRunConfiguration configuration, ConfigurationContext context, Ref<PsiElement> source) {
    Location location = context.getLocation();
    if (location != null) {
      VirtualFile file = location.getVirtualFile();
      if (file != null && BndFileType.BND_RUN_EXT.equals(file.getExtension())) {
        configuration.setName(context.getModule().getName());
        configuration.bndRunFile = file.getPath();
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean isConfigurationFromContext(BndRunConfiguration configuration, ConfigurationContext context) {
    Location location = context.getLocation();
    if (location != null) {
      VirtualFile file = location.getVirtualFile();
      if (file != null && BndFileType.BND_RUN_EXT.equals(file.getExtension())) {
        return FileUtil.pathsEqual(file.getPath(), configuration.bndRunFile);
      }
    }

    return false;
  }
}
