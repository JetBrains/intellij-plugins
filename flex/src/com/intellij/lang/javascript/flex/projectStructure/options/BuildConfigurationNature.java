package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public class BuildConfigurationNature {
  private final FlexIdeBuildConfiguration.TargetPlatform targetPlatform;
  public final boolean pureAS;
  private final FlexIdeBuildConfiguration.OutputType outputType;

  public BuildConfigurationNature(FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                  boolean pureAS,
                                  FlexIdeBuildConfiguration.OutputType outputType) {
    this.targetPlatform = targetPlatform;
    this.pureAS = pureAS;
    this.outputType = outputType;
  }

  public boolean isWebPlatform() {
    return targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Web;
  }

  public boolean isDesktopPlatform() {
    return targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Desktop;
  }

  public boolean isMobilePlatform() {
    return targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Mobile;
  }

  public boolean isApp() {
    return outputType == FlexIdeBuildConfiguration.OutputType.Application;
  }

  public boolean isLib() {
    return outputType == FlexIdeBuildConfiguration.OutputType.Library;
  }
}
