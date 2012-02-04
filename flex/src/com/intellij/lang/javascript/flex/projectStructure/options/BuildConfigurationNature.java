package com.intellij.lang.javascript.flex.projectStructure.options;

import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;

/**
 * @author ksafonov
 */
public class BuildConfigurationNature {
  public final TargetPlatform targetPlatform;
  public final boolean pureAS;
  public final OutputType outputType;

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

  public static final BuildConfigurationNature DEFAULT = new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Application);

  public String toString() {
    return targetPlatform.getPresentableText() + " " + outputType.getPresentableText() + (pureAS ? " (pure ActionScript)" : "");
  }

  public boolean equals(final Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;

    final BuildConfigurationNature otherNature = (BuildConfigurationNature)other;

    if (pureAS != otherNature.pureAS) return false;
    if (outputType != otherNature.outputType) return false;
    if (targetPlatform != otherNature.targetPlatform) return false;

    return true;
  }

  public int hashCode() {
    int result = targetPlatform.hashCode();
    result = 31 * result + (pureAS ? 1 : 0);
    result = 31 * result + outputType.hashCode();
    return result;
  }
}
