// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.model.bc;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.openapi.util.text.StringUtil;
import icons.FlexSharedIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class BuildConfigurationNature {
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

  @NotNull
  public Icon getIcon() {
    switch (targetPlatform) {
      case Web:
        return pureAS ? FlexSharedIcons.BcWebAs : FlexSharedIcons.BcWebFlex;
      case Desktop:
        return pureAS ? FlexSharedIcons.BcDesktopAs : FlexSharedIcons.BcDesktopFlex;
      case Mobile:
        return pureAS ? FlexSharedIcons.BcMobileAs : FlexSharedIcons.BcMobileFlex;
      default:
        assert false : targetPlatform;
        return FlexSharedIcons.BcWebFlex;
    }
  }

  public String getPresentableText() {
    return FlexCommonBundle.message("bc.nature.presentable.text",
                                    pureAS ? 1 : 0, StringUtil.toLowerCase(outputType.getPresentableText()), targetPlatform.ordinal());
  }
}
