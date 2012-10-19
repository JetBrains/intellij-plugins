package com.intellij.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.JpsBuildConfigurationNature;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.module.JpsFlexModuleType;
import com.intellij.flex.model.run.JpsFlashRunConfigurationType;
import com.intellij.flex.model.run.JpsFlashRunnerParameters;
import com.intellij.flex.model.run.JpsFlexUnitRunConfigurationType;
import com.intellij.flex.model.run.JpsFlexUnitRunnerParameters;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.builders.BuildTargetLoader;
import org.jetbrains.jps.builders.BuildTargetType;
import org.jetbrains.jps.model.JpsModel;
import org.jetbrains.jps.model.JpsProject;
import org.jetbrains.jps.model.module.JpsTypedModule;
import org.jetbrains.jps.model.runConfiguration.JpsTypedRunConfiguration;

import java.util.ArrayList;
import java.util.List;

public class FlexBuildTargetType extends BuildTargetType<FlexBuildTarget> {
  public static final FlexBuildTargetType INSTANCE = new FlexBuildTargetType();

  private FlexBuildTargetType() {
    super("flex");
  }

  @NotNull
  public List<FlexBuildTarget> computeAllTargets(@NotNull final JpsModel model) {
    final List<FlexBuildTarget> result = new ArrayList<FlexBuildTarget>();

    final JpsProject project = model.getProject();

    for (JpsTypedRunConfiguration<JpsFlashRunnerParameters> runConfig : project.getRunConfigurations(JpsFlashRunConfigurationType
                                                                                                       .INSTANCE)) {
      final JpsFlexBuildConfiguration bc = runConfig.getProperties().getBC(project);
      if (bc != null) {
        result.add(new FlexBuildTarget(bc, runConfig.getType(), runConfig.getName()));
      }
    }

    for (JpsTypedRunConfiguration<JpsFlexUnitRunnerParameters> runConfig : project.getRunConfigurations(JpsFlexUnitRunConfigurationType
                                                                                                          .INSTANCE)) {
      final JpsFlexBuildConfiguration bc = runConfig.getProperties().getBC(project);
      if (bc != null) {
        result.add(new FlexBuildTarget(bc, runConfig.getType(), runConfig.getName()));
      }
    }

    for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : project.getModules(JpsFlexModuleType.INSTANCE)) {
      for (JpsFlexBuildConfiguration bc : module.getProperties().getBuildConfigurations()) {
        result.add(new FlexBuildTarget(bc, null));

        final JpsBuildConfigurationNature nature = bc.getNature();
        if (nature.isApp() && (nature.isDesktopPlatform() || nature.isMobilePlatform())) {
          result.add(new FlexBuildTarget(bc, Boolean.TRUE));
          result.add(new FlexBuildTarget(bc, Boolean.FALSE));
        }
      }
    }

    return result;
  }

  @NotNull
  public BuildTargetLoader<FlexBuildTarget> createLoader(@NotNull final JpsModel model) {
    return new FlexBuildTargetBuildTargetLoader(model);
  }

  private static class FlexBuildTargetBuildTargetLoader extends BuildTargetLoader<FlexBuildTarget> {
    private final JpsModel myModel;

    public FlexBuildTargetBuildTargetLoader(final JpsModel model) {
      myModel = model;
    }

    @Nullable
    public FlexBuildTarget createTarget(@NotNull final String buildTargetId) {
      final JpsProject project = myModel.getProject();

      final Pair<String, String> runConfigTypeIdAndName = FlexCommonUtils.getRunConfigTypeIdAndNameByBuildTargetId(buildTargetId);

      if (runConfigTypeIdAndName != null) {
        final String runConfigTypeId = runConfigTypeIdAndName.first;
        final String runConfigName = runConfigTypeIdAndName.second;

        if (JpsFlashRunConfigurationType.ID.equals(runConfigTypeId)) {
          for (JpsTypedRunConfiguration<JpsFlashRunnerParameters> runConfig : project.getRunConfigurations(JpsFlashRunConfigurationType
                                                                                                             .INSTANCE)) {
            if (runConfig.getName().equals(runConfigName)) {
              final JpsFlexBuildConfiguration bc = runConfig.getProperties().getBC(project);
              return bc != null ? new FlexBuildTarget(bc, runConfig.getType(), runConfig.getName())
                                : null;
            }
          }
        }
        else if (JpsFlexUnitRunConfigurationType.ID.equals(runConfigTypeId)) {
          for (JpsTypedRunConfiguration<JpsFlexUnitRunnerParameters> runConfig
            : project.getRunConfigurations(JpsFlexUnitRunConfigurationType.INSTANCE)) {

            if (runConfig.getName().equals(runConfigName)) {
              final JpsFlexBuildConfiguration bc = runConfig.getProperties().getBC(project);
              return bc != null ? new FlexBuildTarget(bc, runConfig.getType(), runConfig.getName())
                                : null;
            }
          }
        }
      }
      else {
        final Trinity<String, String, Boolean> moduleAndBCNameAndForcedDebugStatus =
          FlexCommonUtils.getModuleAndBCNameAndForcedDebugStatusByBuildTargetId(buildTargetId);
        if (moduleAndBCNameAndForcedDebugStatus != null) {
          final String moduleName = moduleAndBCNameAndForcedDebugStatus.first;
          final String bcName = moduleAndBCNameAndForcedDebugStatus.second;
          final Boolean forcedDebugStatus = moduleAndBCNameAndForcedDebugStatus.third;

          for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : project.getModules(JpsFlexModuleType.INSTANCE)) {
            if (module.getName().equals(moduleName)) {
              final JpsFlexBuildConfiguration bc = module.getProperties().findConfigurationByName(bcName);
              return bc != null ? new FlexBuildTarget(bc, forcedDebugStatus)
                                : null;
            }
          }
        }
      }

      return null;
    }
  }
}
