// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.run;

import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.module.JpsFlexModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.ex.JpsElementBase;
import org.jetbrains.jps.model.module.JpsTypedModule;

public abstract class JpsBCBasedRunnerParameters<Self extends JpsBCBasedRunnerParameters<Self>> extends JpsElementBase<Self> {

  protected @NotNull String myModuleName = "";
  protected @NotNull String myBCName = "";

  protected JpsBCBasedRunnerParameters() {
  }

  protected JpsBCBasedRunnerParameters(final Self original) {
    myModuleName = original.myModuleName;
    myBCName = original.myBCName;
  }

  public @NotNull String getModuleName() {
    return myModuleName;
  }

  public void setModuleName(final @NotNull String moduleName) {
    myModuleName = moduleName;
  }

  public @NotNull String getBCName() {
    return myBCName;
  }

  public void setBCName(final @NotNull String BCName) {
    myBCName = BCName;
  }

  public @Nullable JpsFlexBuildConfiguration getBC(final JpsProject project) {
    if (!myModuleName.isEmpty() && !myBCName.isEmpty()) {
      for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : project.getModules(JpsFlexModuleType.INSTANCE)) {
        if (module.getName().equals(myModuleName)) {
          return module.getProperties().findConfigurationByName(myBCName);
        }
      }
    }

    return null;
  }
}
