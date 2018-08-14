package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.flex.build.FlexResourceBuildTargetType;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.api.CmdlineProtoUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

public class FlexResourceBuildTargetScopeProvider extends BuildTargetScopeProvider {

  private static final Key<Collection<Pair<Module, FlexBuildConfiguration>>> MODULES_AND_BCS_TO_COMPILE =
    Key.create("modules.and.bcs.to.compile");

  public static void setBCsToCompileForPackaging(final CompileScope scope, final Collection<Pair<Module, FlexBuildConfiguration>> bcs) {
    scope.putUserData(MODULES_AND_BCS_TO_COMPILE, bcs);
  }

  @Nullable
  public static Collection<Pair<Module, FlexBuildConfiguration>> getBCsToCompileForPackaging(final CompileScope scope) {
    return scope.getUserData(MODULES_AND_BCS_TO_COMPILE);
  }

  @Override
  @NotNull
  public List<TargetTypeBuildScope> getBuildTargetScopes(@NotNull final CompileScope baseScope,
                                                         @NotNull final Project project,
                                                         boolean forceBuild) {
    List<String> moduleNames = new ArrayList<>();
    try {
      for (Pair<Module, FlexBuildConfiguration> moduleAndBC : FlexBuildTargetScopeProvider.getModulesAndBCsToCompile(baseScope)) {
        moduleNames.add(moduleAndBC.first.getName());
      }
    }
    catch (ConfigurationException e) {
      // can't happen because checked in ValidateFlashConfigurationsPrecompileTask
    }

    return Arrays.asList(
      CmdlineProtoUtil.createTargetsScope(FlexResourceBuildTargetType.PRODUCTION.getTypeId(), moduleNames, forceBuild),
      CmdlineProtoUtil.createTargetsScope(FlexResourceBuildTargetType.TEST.getTypeId(), moduleNames, forceBuild)
    );
  }
}
