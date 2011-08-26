package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.FlexIdeBuildConfigurationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModulePointer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuildConfigurationEntry extends DependencyEntry {

  private final ModulePointer myModulePointer;

  private final String myBcName;

  public BuildConfigurationEntry(ModulePointer modulePointer, @NotNull String bcName) {
    myModulePointer = modulePointer;
    myBcName = bcName;
  }

  public String getModuleName() {
    return myModulePointer.getModuleName();
  }

  public Module getModule() {
    return myModulePointer.getModule();
  }

  @Nullable
  public FlexIdeBuildConfiguration getBuildConfiguration() {
    Module module = myModulePointer.getModule();
    if (module == null) {
      return null;
    }
    for (FlexIdeBuildConfiguration configuration : FlexIdeBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
      if (configuration.NAME.equals(myBcName)) {
        return configuration;
      }
    }
    return null;
  }
}
