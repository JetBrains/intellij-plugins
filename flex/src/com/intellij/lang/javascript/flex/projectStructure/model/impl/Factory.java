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

  public static ModifiableFlexBuildConfiguration createBuildConfiguration() {
    return new FlexBuildConfigurationImpl();
  }

  public static ModifiableDependencyType createDependencyTypeInstance() {
    return new DependencyTypeImpl();
  }

  public static SdkEntry createSdkEntry(@NotNull String name) {
    return new SdkEntryImpl(name);
  }

  public static ModifiableFlexBuildConfiguration getCopy(@NotNull FlexBuildConfiguration bc) {
    return ((FlexBuildConfigurationImpl)bc).getCopy();
  }

  public static ModifiableFlexBuildConfiguration getTemporaryCopyForCompilation(@NotNull FlexBuildConfiguration bc) {
    final FlexBuildConfigurationImpl copy = ((FlexBuildConfigurationImpl)bc).getCopy();
    copy.setTempBCForCompilation(true);
    return copy;
  }
}
