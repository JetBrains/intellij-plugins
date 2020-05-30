// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.run;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.osmorc.frameworkintegration.FrameworkInstanceDefinition;
import org.osmorc.settings.FrameworkDefinitionListener;

import java.util.List;

class OsmorcFrameworkDefinitionListener implements FrameworkDefinitionListener {
  private final Project myProject;

  OsmorcFrameworkDefinitionListener(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public void definitionsChanged(@NotNull List<Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition>> changes) {
    OsgiConfigurationType configurationType = ConfigurationTypeUtil.findConfigurationType(OsgiConfigurationType.class);
    for (Pair<FrameworkInstanceDefinition, FrameworkInstanceDefinition> pair : changes) {
      if (pair.first == null) continue;
      for (RunConfiguration runConfiguration : RunManager.getInstance(myProject).getConfigurationsList(configurationType)) {
        OsgiRunConfiguration osgiRunConfiguration = (OsgiRunConfiguration)runConfiguration;
        if (pair.first.equals(osgiRunConfiguration.getInstanceToUse())) {
          osgiRunConfiguration.setInstanceToUse(pair.second);
        }
      }
    }
  }
}