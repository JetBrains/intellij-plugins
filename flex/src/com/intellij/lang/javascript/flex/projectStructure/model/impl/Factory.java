package com.intellij.lang.javascript.flex.projectStructure.model.impl;

import com.intellij.lang.javascript.flex.projectStructure.model.*;
import org.jetbrains.annotations.NotNull;

/**
 * User: ksafonov
 */
public class Factory {

  public static ModifiableCompilerOptions createCompilerOptions() {
    return new CompilerOptionsImpl();
  }

  public static ModifiableFlexIdeBuildConfiguration createBuildConfiguration() {
    return new FlexIdeBuildConfigurationImpl();
  }

  public static ModifiableDependencyType createDependencyTypeInstance() {
    return new DependencyTypeImpl();
  }

  public static SdkEntry createSdkEntry(@NotNull String name) {
    return new SdkEntryImpl(name);
  }

  public static FlexIdeBuildConfiguration getCopy(@NotNull FlexIdeBuildConfiguration bc) {
    return ((FlexIdeBuildConfigurationImpl)bc).getCopy();
  }

  public static ModifiableFlexIdeBuildConfiguration getTemporaryCopyForCompilation(@NotNull FlexIdeBuildConfiguration bc) {
    final FlexIdeBuildConfigurationImpl copy = ((FlexIdeBuildConfigurationImpl)bc).getCopy();
    copy.setTempBCForCompilation(true);
    return copy;
  }
}
