package com.intellij.flex;

import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.JpsElement;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.runConfiguration.JpsRunConfigurationType;
import org.jetbrains.jps.model.runConfiguration.JpsTypedRunConfiguration;

public class FlexCommonUtils {

  public static final String FLEX_UNIT_LAUNCHER = "____FlexUnitLauncher";

  private static final String MODULE_PREFIX = "Module: ";
  private static final String BC_PREFIX = "\tBC: ";
  private static final String RUN_CONFIG_TYPE_PREFIX = "Run config type: ";
  private static final String RUN_CONFIG_NAME_PREFIX = "\tName: ";
  private static final String FORCED_DEBUG_STATUS = "\tForced debug status: ";

  public static boolean isSourceFile(final String fileName) {
    final String ext = FileUtil.getExtension(fileName);
    return ext.equalsIgnoreCase("as") || ext.equalsIgnoreCase("mxml") || ext.equalsIgnoreCase("fxg");
  }

  public static boolean canHaveResourceFiles(final BuildConfigurationNature nature) {
    return nature.isApp();
  }

  public static boolean isFlexUnitBC(final JpsFlexBuildConfiguration bc) {
    return bc.isTempBCForCompilation() && bc.getMainClass().endsWith(FLEX_UNIT_LAUNCHER);
  }

  /**
   * @param forcedDebugStatus <code>true</code> or <code>false</code> means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   *                          <code>null</code> means that bc is compiled as is (i.e. as configured) without any modifications
   */
  public static String getBuildTargetId(final String moduleName, final String bcName, final @Nullable Boolean forcedDebugStatus) {
    return MODULE_PREFIX + moduleName + BC_PREFIX + bcName + FORCED_DEBUG_STATUS + forcedDebugStatus;
  }

  public static String getBuildTargetIdForRunConfig(final String runConfigTypeId, final String runConfigName) {
    return RUN_CONFIG_TYPE_PREFIX + runConfigTypeId + RUN_CONFIG_NAME_PREFIX + runConfigName;
  }

  @Nullable
  public static Pair<String, String> getRunConfigTypeIdAndNameByBuildTargetId(final String buildTargetId) {
    if (buildTargetId.startsWith(RUN_CONFIG_TYPE_PREFIX)) {
      final int index = buildTargetId.indexOf(RUN_CONFIG_NAME_PREFIX);
      assert index > 0 : buildTargetId;
      return Pair.create(buildTargetId.substring(RUN_CONFIG_TYPE_PREFIX.length(), index),
                         buildTargetId.substring(index + RUN_CONFIG_NAME_PREFIX.length()));
    }
    return null;
  }

  /**
   * @return <code>Trinity.first</code> - module name<br/>
   *         <code>Trinity.second</code> - BC name<br/>
   *         <code>Trinity.third</code> - forced debug status: <code>true</code> or <code>false</code> means that this bc is compiled for further packaging and we need swf to have corresponding debug status;
   *         <code>null</code> means that bc is compiled as is (i.e. as configured) without any modifications
   */
  @Nullable
  public static Trinity<String, String, Boolean> getModuleAndBCNameAndForcedDebugStatusByBuildTargetId(final String buildTargetId) {
    if (buildTargetId.startsWith(MODULE_PREFIX)) {
      final int bcIndex = buildTargetId.indexOf(BC_PREFIX);
      final int forceDebugIndex = buildTargetId.indexOf(FORCED_DEBUG_STATUS);
      assert bcIndex > 0 && forceDebugIndex > bcIndex : buildTargetId;

      final String moduleName = buildTargetId.substring(MODULE_PREFIX.length(), bcIndex);
      final String bcName = buildTargetId.substring(bcIndex + BC_PREFIX.length(), forceDebugIndex);

      final String forcedDebugText = buildTargetId.substring(forceDebugIndex + FORCED_DEBUG_STATUS.length());
      final Boolean forcedDebugStatus = forcedDebugText.equalsIgnoreCase("true")
                                        ? Boolean.TRUE
                                        : forcedDebugText.equalsIgnoreCase("false")
                                          ? Boolean.FALSE
                                          : null;
      return Trinity.create(moduleName, bcName, forcedDebugStatus);
    }
    return null;
  }


  @Nullable
  public static <P extends JpsElement> JpsTypedRunConfiguration<P> findRunConfiguration(final @NotNull JpsProject project,
                                                                                        final @NotNull JpsRunConfigurationType<P> runConfigType,
                                                                                        final @NotNull String runConfigName) {
    for (JpsTypedRunConfiguration<P> runConfig : project.getRunConfigurations(runConfigType)) {
      if (runConfigName.equals(runConfig.getName())) {
        return runConfig;
      }
    }

    return null;
  }
}
