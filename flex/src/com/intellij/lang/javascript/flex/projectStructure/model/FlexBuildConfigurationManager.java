package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User: ksafonov
 */
public abstract class FlexBuildConfigurationManager {

  public abstract void setBuildConfigurations(FlexIdeBuildConfiguration[] flexIdeBuildConfigurations);

  public abstract FlexIdeBuildConfiguration[] getBuildConfigurations();

  public abstract FlexIdeBuildConfiguration getActiveConfiguration();

  public abstract void setActiveBuildConfiguration(FlexIdeBuildConfiguration buildConfiguration);

  // TODO should be getModifiableModel()!
  public abstract ModifiableCompilerOptions getModuleLevelCompilerOptions();

  @Nullable
  public abstract FlexIdeBuildConfiguration findConfigurationByName(String name);

  public static FlexBuildConfigurationManager getInstance(final @NotNull Module module) {
    assert ModuleType.get(module) == FlexModuleType.getInstance() : ModuleType.get(module).getName() + ", " + module.toString();
    return (FlexBuildConfigurationManager)module.getPicoContainer().getComponentInstance(FlexBuildConfigurationManager.class.getName());
  }
}
