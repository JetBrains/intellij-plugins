// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.flex.build;

import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.model.bc.BuildConfigurationNature;
import com.intellij.flex.model.bc.JpsFlexBuildConfiguration;
import com.intellij.flex.model.bc.JpsFlexBuildConfigurationManager;
import com.intellij.flex.model.module.JpsFlexModuleType;
import com.intellij.flex.model.run.JpsFlashRunConfigurationType;
import com.intellij.flex.model.run.JpsFlashRunnerParameters;
import com.intellij.flex.model.run.JpsFlexUnitRunConfigurationType;
import com.intellij.flex.model.run.JpsFlexUnitRunnerParameters;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Trinity;
import com.intellij.util.containers.ContainerUtil;
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

public final class FlexBuildTargetType extends BuildTargetType<FlexBuildTarget> {
  public static final FlexBuildTargetType INSTANCE = new FlexBuildTargetType();

  private FlexBuildTargetType() {
    super("flex");
  }

  @Override
  @NotNull
  public List<FlexBuildTarget> computeAllTargets(@NotNull final JpsModel model) {
    final List<FlexBuildTarget> result = new ArrayList<>();

    final JpsProject project = model.getProject();

    for (JpsTypedRunConfiguration<JpsFlashRunnerParameters> runConfig : project.getRunConfigurations(JpsFlashRunConfigurationType
                                                                                                       .INSTANCE)) {
      ContainerUtil.addIfNotNull(result,
                                 FlexBuildTarget.create(project, runConfig.getType(), runConfig.getName()));
    }

    for (JpsTypedRunConfiguration<JpsFlexUnitRunnerParameters> runConfig : project.getRunConfigurations(JpsFlexUnitRunConfigurationType
                                                                                                          .INSTANCE)) {
      ContainerUtil.addIfNotNull(result,
                                 FlexBuildTarget.create(project, runConfig.getType(), runConfig.getName()));
    }

    for (JpsTypedModule<JpsFlexBuildConfigurationManager> module : project.getModules(JpsFlexModuleType.INSTANCE)) {
      for (JpsFlexBuildConfiguration bc : module.getProperties().getBuildConfigurations()) {
        result.add(FlexBuildTarget.create(bc, null));

        final BuildConfigurationNature nature = bc.getNature();
        if (nature.isApp() && (nature.isDesktopPlatform() || nature.isMobilePlatform())) {
          result.add(FlexBuildTarget.create(bc, Boolean.TRUE));
          result.add(FlexBuildTarget.create(bc, Boolean.FALSE));
        }
      }
    }

    return result;
  }

  @Override
  @NotNull
  public BuildTargetLoader<FlexBuildTarget> createLoader(@NotNull final JpsModel model) {
    return new FlexBuildTargetLoader(model);
  }

  private static class FlexBuildTargetLoader extends BuildTargetLoader<FlexBuildTarget> {
    private final JpsModel myModel;

    FlexBuildTargetLoader(final JpsModel model) {
      myModel = model;
    }

    @Override
    @Nullable
    public FlexBuildTarget createTarget(@NotNull final String buildTargetId) {
      final JpsProject project = myModel.getProject();

      final Pair<String, String> runConfigTypeIdAndName = FlexCommonUtils.getRunConfigTypeIdAndNameByBuildTargetId(buildTargetId);

      if (runConfigTypeIdAndName != null) {
        final String runConfigTypeId = runConfigTypeIdAndName.first;
        final String runConfigName = runConfigTypeIdAndName.second;

        if (JpsFlashRunConfigurationType.ID.equals(runConfigTypeId)) {
          return FlexBuildTarget.create(project, JpsFlashRunConfigurationType.INSTANCE, runConfigName);
        }
        else if (JpsFlexUnitRunConfigurationType.ID.equals(runConfigTypeId)) {
          return FlexBuildTarget.create(project, JpsFlexUnitRunConfigurationType.INSTANCE, runConfigName);
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
              return bc != null ? FlexBuildTarget.create(bc, forcedDebugStatus) : null;
            }
          }
        }
      }

      return null;
    }
  }
}
