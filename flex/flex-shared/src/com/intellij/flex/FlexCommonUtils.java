package com.intellij.flex;

import com.intellij.flex.model.bc.JpsBuildConfigurationNature;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.openapi.util.io.FileUtil;

public class FlexCommonUtils {

  public static final String FLEX_UNIT_LAUNCHER = "____FlexUnitLauncher";

  public static boolean isSourceFile(final String fileName) {
    final String ext = FileUtil.getExtension(fileName);
    return ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg");
  }

  public static boolean canHaveResourceFiles(final JpsBuildConfigurationNature nature) {
    return nature.isApp();
  }

  public static boolean isFlexUnitBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().endsWith(FLEX_UNIT_LAUNCHER);
  }
}
