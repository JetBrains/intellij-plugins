package com.intellij.lang.javascript.flex.projectStructure.options;

/**
 * @author ksafonov
 */
public class BCUtils {

  public static LinkageType getDefaultFrameworkLinkage(final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
                                                                                      final boolean pureAS,
                                                                                      final FlexIdeBuildConfiguration.OutputType outputType) {
    // todo check
    if (outputType == FlexIdeBuildConfiguration.OutputType.Library) {
      return LinkageType.External;
    }

    switch (targetPlatform) {
      case Web:
        return LinkageType.RSL;
      case Desktop:
      case Mobile:
        return LinkageType.Merged;
      default:
        assert false;
        return null;
    }
  }

  public static LinkageType[] getSuitableFrameworkLinkages(final FlexIdeBuildConfiguration.TargetPlatform targetPlatform,
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

    return LinkageType.frameworkLinkageValues();
  }
}
