package com.intellij.lang.javascript.flex.build;

import com.intellij.compiler.impl.BuildTargetScopeProvider;
import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.flex.FlexCommonUtils;
import com.intellij.flex.build.FlexBuildTargetType;
import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.flex.actions.airpackage.AirPackageProjectParameters;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.*;
import com.intellij.lang.javascript.flex.projectStructure.model.impl.Factory;
import com.intellij.lang.javascript.flex.run.BCBasedRunnerParameters;
import com.intellij.lang.javascript.flex.run.FlashRunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.compiler.CompileScope;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.api.CmdlineProtoUtil;

import java.util.*;

import static org.jetbrains.jps.api.CmdlineRemoteProto.Message.ControllerMessage.ParametersMessage.TargetTypeBuildScope;

public class FlexBuildTargetScopeProvider extends BuildTargetScopeProvider {

  private static final Logger LOG = Logger.getInstance(FlexBuildTargetScopeProvider.class.getName());

  static Collection<Pair<Module, FlexBuildConfiguration>> getModulesAndBCsToCompile(final CompileScope scope)
    throws ConfigurationException {

    final Collection<Pair<Module, FlexBuildConfiguration>> result = new HashSet<>();
    final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCsToCompile = FlexResourceBuildTargetScopeProvider.getBCsToCompileForPackaging(scope);
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(scope);

    if (modulesAndBCsToCompile != null) {
      for (Pair<Module, FlexBuildConfiguration> moduleAndBC : modulesAndBCsToCompile) {
        if (!moduleAndBC.second.isSkipCompile()) {
          final FlexBuildConfiguration bcWithForcedDebugStatus = forceDebugStatus(moduleAndBC.first.getProject(), moduleAndBC.second);
          result.add(Pair.create(moduleAndBC.first, bcWithForcedDebugStatus));
          appendBCDependencies(result, moduleAndBC.first, moduleAndBC.second);
        }
      }
    }
    else if (runConfiguration instanceof FlashRunConfiguration || runConfiguration instanceof FlexUnitRunConfiguration) {
      final BCBasedRunnerParameters params = runConfiguration instanceof FlashRunConfiguration
                                             ? ((FlashRunConfiguration)runConfiguration).getRunnerParameters()
                                             : ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
      final Pair<Module, FlexBuildConfiguration> moduleAndBC;

      final Ref<RuntimeConfigurationError> exceptionRef = new Ref<>();
      moduleAndBC = ApplicationManager.getApplication().runReadAction((NullableComputable<Pair<Module, FlexBuildConfiguration>>)() -> {
        try {
          return params.checkAndGetModuleAndBC(runConfiguration.getProject());
        }
        catch (RuntimeConfigurationError e) {
          exceptionRef.set(e);
          return null;
        }
      });
      if (!exceptionRef.isNull()) {
        throw new ConfigurationException(exceptionRef.get().getMessage(),
                                         FlexBundle.message("run.configuration.0", runConfiguration.getName()));
      }

      if (!moduleAndBC.second.isSkipCompile()) {
        result.add(moduleAndBC);
        appendBCDependencies(result, moduleAndBC.first, moduleAndBC.second);
      }
    }
    else {
      ReadAction.run(() -> {
        for (final Module module : scope.getAffectedModules()) {
          if (module.isDisposed() || ModuleType.get(module) != FlexModuleType.getInstance()) continue;
          for (final FlexBuildConfiguration bc : FlexBuildConfigurationManager.getInstance(module).getBuildConfigurations()) {
            if (!bc.isSkipCompile()) {
              result.add(Pair.create(module, bc));
            }
          }
        }
      });
    }

    return result;
  }

  private static FlexBuildConfiguration forceDebugStatus(final Project project, final FlexBuildConfiguration bc) {
    final boolean debug = getForcedDebugStatus(project, bc);

    // must not use getTemporaryCopyForCompilation() here because additional config file must not be merged with the generated one when compiling swf for release or AIR package
    final ModifiableFlexBuildConfiguration result = Factory.getCopy(bc);
    final String additionalOptions = FlexCommonUtils
      .removeOptions(bc.getCompilerOptions().getAdditionalOptions(), "debug", "compiler.debug");
    result.getCompilerOptions().setAdditionalOptions(additionalOptions + " -debug=" + debug);

    return result;
  }

  public static boolean getForcedDebugStatus(final Project project, final FlexBuildConfiguration bc) {
    final boolean debug;

    if (bc.getTargetPlatform() == TargetPlatform.Mobile) {
      final AirPackageProjectParameters params = AirPackageProjectParameters.getInstance(project);
      if (bc.getAndroidPackagingOptions().isEnabled()) {
        debug = params.androidPackageType != AirPackageProjectParameters.AndroidPackageType.Release;
      }
      else {
        debug = params.iosPackageType == AirPackageProjectParameters.IOSPackageType.DebugOverNetwork;
      }
    }
    else {
      debug = false;
    }

    return debug;
  }

  private static void appendBCDependencies(final Collection<Pair<Module, FlexBuildConfiguration>> modulesAndBCs,
                                           final Module module,
                                           final FlexBuildConfiguration bc) throws ConfigurationException {
    for (final DependencyEntry entry : bc.getDependencies().getEntries()) {
      if (entry instanceof BuildConfigurationEntry) {
        final BuildConfigurationEntry bcEntry = (BuildConfigurationEntry)entry;

        final Module dependencyModule = bcEntry.findModule();
        final FlexBuildConfiguration dependencyBC = dependencyModule == null ? null : bcEntry.findBuildConfiguration();

        if (dependencyModule == null || dependencyBC == null) {
          throw new ConfigurationException(FlexBundle.message("bc.dependency.does.not.exist", bcEntry.getBcName(), bcEntry.getModuleName(),
                                                              bc.getName(), module.getName()));
        }

        final Pair<Module, FlexBuildConfiguration> dependencyModuleAndBC = Pair.create(dependencyModule, dependencyBC);
        if (!dependencyBC.isSkipCompile()) {
          if (modulesAndBCs.add(dependencyModuleAndBC)) {
            appendBCDependencies(modulesAndBCs, dependencyModule, dependencyBC);
          }
        }
      }
    }
  }

  @Override
  @NotNull
  public List<TargetTypeBuildScope> getBuildTargetScopes(@NotNull final CompileScope baseScope,
                                                         @NotNull final Project project,
                                                         boolean forceBuild) {
    final RunConfiguration runConfiguration = CompileStepBeforeRun.getRunConfiguration(baseScope);
    final Collection<Pair<Module, FlexBuildConfiguration>> bcsToCompileForPackaging =
      FlexResourceBuildTargetScopeProvider.getBCsToCompileForPackaging(baseScope);

    List<String> targetIds = new ArrayList<>();
    try {
      for (Pair<Module, FlexBuildConfiguration> moduleAndBC : getModulesAndBCsToCompile(baseScope)) {
        final Module module = moduleAndBC.first;
        final FlexBuildConfiguration bc = moduleAndBC.second;

        if (bcsToCompileForPackaging != null && contains(bcsToCompileForPackaging, module, bc)) {
          final boolean forcedDebugStatus = getForcedDebugStatus(project, bc);
          targetIds.add(FlexCommonUtils.getBuildTargetId(module.getName(), bc.getName(), forcedDebugStatus));
        }
        else if (bc.isTempBCForCompilation()) {
          LOG.assertTrue(runConfiguration instanceof FlashRunConfiguration || runConfiguration instanceof FlexUnitRunConfiguration,
                         bc.getName());
          final BCBasedRunnerParameters params = runConfiguration instanceof FlashRunConfiguration
                                                 ? ((FlashRunConfiguration)runConfiguration).getRunnerParameters()
                                                 : ((FlexUnitRunConfiguration)runConfiguration).getRunnerParameters();
          LOG.assertTrue(params.getModuleName().equals(module.getName()),
                         "Module name in run config: " + params.getModuleName() + ", expected: " + module.getName());
          LOG.assertTrue(params.getBCName().equals(bc.getName()),
                         "BC name in run config: " + params.getBCName() + ", expected: " + bc.getName());
          targetIds.add(FlexCommonUtils.getBuildTargetIdForRunConfig(runConfiguration.getType().getId(), runConfiguration.getName()));
        }
        else {
          targetIds.add(FlexCommonUtils.getBuildTargetId(module.getName(), bc.getName(), null));
        }
      }
    }
    catch (ConfigurationException e) {
      // can't happen because checked in ValidateFlashConfigurationsPrecompileTask
      LOG.error(e);
    }

    if (targetIds.isEmpty()) {
      return Collections.emptyList();
    }
    return Collections.singletonList(CmdlineProtoUtil.createTargetsScope(FlexBuildTargetType.INSTANCE.getTypeId(), targetIds, forceBuild));
  }

  private static boolean contains(final Collection<Pair<Module, FlexBuildConfiguration>> bcs,
                                  final Module module,
                                  final FlexBuildConfiguration bc) {
    for (Pair<Module, FlexBuildConfiguration> pair : bcs) {
      if (pair.first.getName().equals(module.getName()) && pair.second.getName().equals(bc.getName())) {
        return true;
      }
    }
    return false;
  }
}
