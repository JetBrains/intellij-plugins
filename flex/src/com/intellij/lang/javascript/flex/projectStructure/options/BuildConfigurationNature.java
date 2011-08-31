package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public class BuildConfigurationNature {
  public final FlexIdeBuildConfiguration.TargetPlatform targetPlatform;
  public final boolean pureAS;
  public final FlexIdeBuildConfiguration.OutputType outputType;

  public BuildConfigurationNature(FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                  boolean pureAS,
                                  FlexIdeBuildConfiguration.OutputType outputType) {
    this.targetPlatform = targetPlatform;
    this.pureAS = pureAS;
    this.outputType = outputType;
  }
}
