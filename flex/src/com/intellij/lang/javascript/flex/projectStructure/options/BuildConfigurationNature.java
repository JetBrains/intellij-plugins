package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;

/**
 * @author ksafonov
 */
public class BuildConfigurationNature {
  private final TargetPlatform targetPlatform;
  public final boolean pureAS;
  private final OutputType outputType;

  public BuildConfigurationNature(TargetPlatform targetPlatform,
                                  boolean pureAS,
                                  OutputType outputType) {
    this.targetPlatform = targetPlatform;
    this.pureAS = pureAS;
    this.outputType = outputType;
  }

  public boolean isWebPlatform() {
    return targetPlatform == TargetPlatform.Web;
  }

  public boolean isDesktopPlatform() {
    return targetPlatform == TargetPlatform.Desktop;
  }

  public boolean isMobilePlatform() {
    return targetPlatform == TargetPlatform.Mobile;
  }

  public boolean isApp() {
    return outputType == OutputType.Application;
  }

  public boolean isLib() {
    return outputType == OutputType.Library;
  }
}
