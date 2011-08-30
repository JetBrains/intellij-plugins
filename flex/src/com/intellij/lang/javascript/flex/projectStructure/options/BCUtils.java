package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public class BCUtils {

  private static LinkageType[] LIB_LINKAGES = {LinkageType.Default, LinkageType.External, LinkageType.Merged};
  private static LinkageType[] FLEX_APP_LINKAGES = {LinkageType.Default, LinkageType.Merged, LinkageType.RSL};
  private static LinkageType[] AS_APP_LINKAGES = {LinkageType.Default, LinkageType.Merged};

  public static LinkageType[] getSuitableFrameworkLinkages(final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                                           final boolean pureAS,
                                                           final FlexIdeBuildConfiguration.OutputType outputType) {
    if (outputType == FlexIdeBuildConfiguration.OutputType.Library) {
      return LIB_LINKAGES;
    }
    else if (pureAS) {
      return AS_APP_LINKAGES;
    }
    else {
      return FLEX_APP_LINKAGES;
    }
  }

  public static LinkageType getDefaultFrameworkLinkage(final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                                       final boolean pureAS,
                                                       final FlexIdeBuildConfiguration.OutputType outputType) {
    if (outputType == FlexIdeBuildConfiguration.OutputType.Library) {
      return LinkageType.External;
    }
    else if (pureAS) {
      return LinkageType.Merged;
    }
    else if (targetPlatform == FlexIdeBuildConfiguration.TargetPlatform.Web) {
      return LinkageType.RSL; // Web Flex App
    }
    else {
      return LinkageType.Merged; // Desktop (AIR) Flex App
    }
  }
}
