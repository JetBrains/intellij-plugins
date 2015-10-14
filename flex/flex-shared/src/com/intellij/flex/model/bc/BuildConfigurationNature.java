package com.intellij.flex.model.bc;

import com.intellij.flex.FlexCommonBundle;
import icons.FlexSharedIcons;

import javax.swing.*;

public class BuildConfigurationNature {

  public final TargetPlatform targetPlatform;
  public final boolean pureAS;
  public final OutputType outputType;

  public BuildConfigurationNature(final TargetPlatform targetPlatform, final boolean pureAS, final OutputType outputType) {
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

  public static final BuildConfigurationNature
    DEFAULT = new BuildConfigurationNature(TargetPlatform.Web, false, OutputType.Application);

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

  public Icon getIcon() {
    switch (targetPlatform) {
      case Web:
        return pureAS ? FlexSharedIcons.Bc_web_as : FlexSharedIcons.Bc_web_flex;
      case Desktop:
        return pureAS ? FlexSharedIcons.Bc_desktop_as : FlexSharedIcons.Bc_desktop_flex;
      case Mobile:
        return pureAS ? FlexSharedIcons.Bc_mobile_as : FlexSharedIcons.Bc_mobile_flex;
      default:
        assert false : targetPlatform;
        return FlexSharedIcons.Bc_web_flex;
    }
  }

  public String getPresentableText() {
    return FlexCommonBundle.message("bc.nature.presentable.text",
                                    pureAS ? 1 : 0, outputType.getPresentableText().toLowerCase(), targetPlatform.ordinal());
  }
}
