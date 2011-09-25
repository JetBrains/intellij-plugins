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

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder();
    if (targetPlatform == TargetPlatform.Mobile) {
      b.append("Mobile");
    }
    else if (targetPlatform == TargetPlatform.Desktop) {
      b.append("AIR");
    }
    else {
      if (pureAS) {
        b.append("AS");
      }
      else {
        b.append("Flex");
      }
    }
    b.append(" ");
    if (outputType == OutputType.Application) {
      b.append("App");
    }
    else if (outputType == OutputType.RuntimeLoadedModule) {
      b.append("Runtime module");
    }
    else {
      b.append("Lib");
    }
    return b.toString();
  }
}
