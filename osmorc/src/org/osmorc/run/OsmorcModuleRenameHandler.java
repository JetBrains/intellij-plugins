// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.run;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Function;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import org.osmorc.run.ui.SelectedBundle;

import java.util.List;

class OsmorcModuleRenameHandler implements ModuleListener {
  @Override
  public void modulesRenamed(@NotNull Project project, @NotNull List<? extends Module> modules, @NotNull Function<? super Module, String> oldNameProvider) {
    List<Pair<SelectedBundle, String>> pairs = new SmartList<>();
    OsgiConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(OsgiConfigurationType.class);
    for (Module module : modules) {
      String oldName = oldNameProvider.fun(module);
      for (RunConfiguration runConfiguration : RunManager.getInstance(project).getConfigurationsList(configurationType)) {
        for (SelectedBundle bundle : ((OsgiRunConfiguration)runConfiguration).getBundlesToDeploy()) {
          if (bundle.isModule() && bundle.getName().equals(oldName)) {
            pairs.add(Pair.create(bundle, module.getName()));
            break;
          }
        }
      }
    }

    if (!pairs.isEmpty()) {
      ApplicationManager.getApplication().runWriteAction(() -> {
        for (Pair<SelectedBundle, String> pair : pairs) {
          pair.first.setName(pair.second);
        }
      });
    }
  }
}