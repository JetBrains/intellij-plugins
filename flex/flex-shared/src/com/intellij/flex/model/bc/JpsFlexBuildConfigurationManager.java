package com.intellij.flex.model.bc;

import org.jetbrains.jps.model.JpsCompositeElement;

import java.util.List;

public interface JpsFlexBuildConfigurationManager extends JpsCompositeElement {

  List<JpsFlexBuildConfiguration> getBuildConfigurations();

  //JpsFlexBuildConfiguration addBuildConfiguration(String name);

  JpsFlexBuildConfiguration getActiveConfiguration();

  JpsFlexModuleOrProjectCompilerOptions getModuleLevelCompilerOptions();
}
