// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class FlexBuildConfigurationManager {
  public abstract FlexBuildConfiguration[] getBuildConfigurations();

  public abstract FlexBuildConfiguration getActiveConfiguration();

  public abstract void setActiveBuildConfiguration(FlexBuildConfiguration buildConfiguration);

  // TODO should be getModifiableModel()!
  public abstract ModuleOrProjectCompilerOptions getModuleLevelCompilerOptions();

  @Nullable
  public abstract FlexBuildConfiguration findConfigurationByName(String name);

  public static FlexBuildConfigurationManager getInstance(@NotNull Module module) {
    assert ModuleType.get(module) == FlexModuleType.getInstance() : ModuleType.get(module).getName() + ", " + module;
    return module.getService(FlexBuildConfigurationManager.class);
  }
}
