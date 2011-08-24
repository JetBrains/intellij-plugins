package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public class BCUtils {

  public static FlexIdeBuildConfiguration.FrameworkLinkage getDefaultFrameworkLinkage(final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                                                                      final boolean pureAS,
                                                                                      final FlexIdeBuildConfiguration.OutputType outputType) {
    // todo check
    if (outputType == FlexIdeBuildConfiguration.OutputType.Library) {
      return FlexIdeBuildConfiguration.FrameworkLinkage.External;
    }

    switch (targetPlatform) {
      case Web:
        return FlexIdeBuildConfiguration.FrameworkLinkage.RSL;
      case Desktop:
      case Mobile:
        return FlexIdeBuildConfiguration.FrameworkLinkage.Merged;
      default:
        assert false;
        return null;
    }
  }

  public static FlexIdeBuildConfiguration.FrameworkLinkage[] getSuitableFrameworkLinkages(final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                                                                          final boolean isPureAS,
                                                                                          final FlexIdeBuildConfiguration.OutputType outputType) {
    // todo implement
    final boolean isLib = outputType == FlexIdeBuildConfiguration.OutputType.Library;
    switch (targetPlatform) {
      case Web:
        break;
      case Desktop:
        break;
      case Mobile:
        break;
    }

    return FlexIdeBuildConfiguration.FrameworkLinkage.values();
  }
}
