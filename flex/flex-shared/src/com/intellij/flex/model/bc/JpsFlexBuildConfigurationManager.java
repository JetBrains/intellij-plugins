package com.intellij.flex.model.bc;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsCompositeElement;

import java.util.List;

public interface JpsFlexBuildConfigurationManager extends JpsCompositeElement {

  List<JpsFlexBuildConfiguration> getBuildConfigurations();

  //JpsFlexBuildConfiguration addBuildConfiguration(String name);

  JpsFlexBuildConfiguration getActiveConfiguration();

  @Nullable
  JpsFlexBuildConfiguration findConfigurationByName(String name);

  JpsFlexModuleOrProjectCompilerOptions getModuleLevelCompilerOptions();

  JpsFlexBuildConfiguration createTemporaryCopyForCompilation(@NotNull JpsFlexBuildConfiguration bc);
}
