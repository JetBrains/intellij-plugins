package com.intellij.flex.model.bc;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public class JpsBuildConfigurationNature {

  private static final Icon ICON_WEB_AS = IconLoader.getIcon("/images/bc-web-as.png");
  private static final Icon ICON_WEB_FLEX = IconLoader.getIcon("/images/bc-web-flex.png");
  private static final Icon ICON_DESKTOP_AS = IconLoader.getIcon("/images/bc-desktop-as.png");
  private static final Icon ICON_DESKTOP_FLEX = IconLoader.getIcon("/images/bc-desktop-flex.png");
  private static final Icon ICON_MOBILE_AS = IconLoader.getIcon("/images/bc-mobile-as.png");
  private static final Icon ICON_MOBILE_FLEX = IconLoader.getIcon("/images/bc-mobile-flex.png");

  public final JpsTargetPlatform targetPlatform;
  public final boolean pureAS;
  public final JpsOutputType outputType;

  public JpsBuildConfigurationNature(JpsTargetPlatform targetPlatform,
                                     boolean pureAS,
                                     JpsOutputType outputType) {
    this.targetPlatform = targetPlatform;
    this.pureAS = pureAS;
    this.outputType = outputType;
  }

  public boolean isWebPlatform() {
    return targetPlatform == JpsTargetPlatform.Web;
  }

  public boolean isDesktopPlatform() {
    return targetPlatform == JpsTargetPlatform.Desktop;
  }

  public boolean isMobilePlatform() {
    return targetPlatform == JpsTargetPlatform.Mobile;
  }

  public boolean isApp() {
    return outputType == JpsOutputType.Application;
  }

  public boolean isLib() {
    return outputType == JpsOutputType.Library;
  }

  public static final JpsBuildConfigurationNature
    DEFAULT = new JpsBuildConfigurationNature(JpsTargetPlatform.Web, false, JpsOutputType.Application);

  public String toString() {
    return targetPlatform.getPresentableText() + " " + outputType.getPresentableText() + (pureAS ? " (pure ActionScript)" : "");
  }

  public boolean equals(final Object other) {
    if (this == other) return true;
    if (other == null || getClass() != other.getClass()) return false;

    final JpsBuildConfigurationNature otherNature = (JpsBuildConfigurationNature)other;

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
        return pureAS ? ICON_WEB_AS : ICON_WEB_FLEX;
      case Desktop:
        return pureAS ? ICON_DESKTOP_AS : ICON_DESKTOP_FLEX;
      case Mobile:
        return pureAS ? ICON_MOBILE_AS : ICON_MOBILE_FLEX;
      default:
        assert false : targetPlatform;
        return ICON_WEB_FLEX;
    }
  }

  public String getPresentableText() {
    return FlexCommonBundle.message("bc.nature.presentable.text",
                                    pureAS ? 1 : 0, outputType.getPresentableText().toLowerCase(), targetPlatform.ordinal());
  }
}
