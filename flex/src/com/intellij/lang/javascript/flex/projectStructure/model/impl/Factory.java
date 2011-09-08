package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class Factory {

  public static ModifiableCompilerOptions createCompilerOptions() {
    return new CompilerOptionsImpl();
  }

  public static ModifiableFlexIdeBuildConfiguration getCopy(FlexIdeBuildConfiguration configuration) {
    return ((FlexIdeBuildConfigurationImpl)configuration).getCopy();
  }

  public static ModifiableFlexIdeBuildConfiguration createBuildConfiguration() {
    return new FlexIdeBuildConfigurationImpl();
  }

  public static ModifiableBuildConfigurationEntry createBuildConfigurationEntry(@NotNull Module module, @NotNull String bcName) {
    return new BuildConfigurationEntryImpl(module, bcName);
  }

  public static ModifiableBuildConfigurationEntry createBuildConfigurationEntry(@NotNull Project project,
                                                                                @NotNull String moduleName,
                                                                                @NotNull String bcName) {
    return new BuildConfigurationEntryImpl(project, moduleName, bcName);
  }

  public static ModifiableModuleLibraryEntry createModuleLibraryEntry(@NotNull String libraryId) {
    return new ModuleLibraryEntryImpl(libraryId);
  }

  public static ModifiableDependencyType createDependencyTypeInstance() {
    return new DependencyTypeImpl();
  }

  public static SdkEntry createSdkEntry(@NotNull String libraryId, @NotNull String homePath) {
    return new SdkEntryImpl(libraryId, homePath);
  }
}
