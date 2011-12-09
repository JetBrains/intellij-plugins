package com.intellij.lang.javascript.flex.run;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirPackageParameters;
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.flexunit.*;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public class FlexRunner extends FlexBaseRunner {

  protected RunContentDescriptor launchFlexIdeConfig(final Module module,
                                                     final FlexIdeBuildConfiguration config,
                                                     final FlexIdeRunnerParameters params,
                                                     final Executor executor,
                                                     final RunProfileState state,
                                                     final RunContentDescriptor contentToReuse,
                                                     final ExecutionEnvironment environment) throws ExecutionException {
    switch (config.getTargetPlatform()) {
      case Web:
        // todo handle html wrapper!
        final String urlOrPath = params.isLaunchUrl() ? params.getUrl() : config.getOutputFilePath();
        launchWithSelectedApplication(urlOrPath, params.getLauncherParameters());
        break;
      case Desktop:
        return standardLaunch(module.getProject(), executor, state, contentToReuse, environment);
      case Mobile:
        switch (params.getMobileRunTarget()) {
          case Emulator:
            return standardLaunch(module.getProject(), executor, state, contentToReuse, environment);
          case AndroidDevice:
            final String applicationId = getApplicationId(getAirDescriptorPath(config, config.getAndroidPackagingOptions()));
            final Sdk sdk = FlexUtils.createFlexSdkWrapper(config);
            if (packAndInstallToAndroidDevice(module, sdk, createAndroidPackageParams(sdk, config, params, false), applicationId, false)) {
              launchOnAndroidDevice(module.getProject(), sdk, applicationId, false);
            }
            return null;
        }
        break;
    }
    return null;
  }

  @Nullable
  private RunContentDescriptor standardLaunch(final Project project,
                                              final Executor executor,
                                              final RunProfileState state,
                                              final RunContentDescriptor contentToReuse, final ExecutionEnvironment environment)
    throws ExecutionException {
    final ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) return null;

    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
    contentBuilder.setExecutionResult(executionResult);
    contentBuilder.setEnvironment(environment);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @Nullable
  protected RunContentDescriptor doLaunch(final Project project,
                                          final Executor executor,
                                          final RunProfileState state,
                                          final RunContentDescriptor contentToReuse,
                                          final ExecutionEnvironment env,
                                          final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    if (isRunOnDevice(flexRunnerParameters)) {
      final AirMobileRunnerParameters mobileParams = (AirMobileRunnerParameters)flexRunnerParameters;
      if (mobileParams.getAirMobileRunMode() == AirMobileRunnerParameters.AirMobileRunMode.ExistingPackage) {
        launchExistingPackage(project, flexSdk, mobileParams);
        return null;
      }
      else {
        final Pair<String, String> swfPathAndApplicationId = getSwfPathAndApplicationId(mobileParams);
        final Module module = ModuleManager.getInstance(project).findModuleByName(mobileParams.getModuleName());
        final MobileAirPackageParameters packageParameters =
          createAndroidPackageParams(flexSdk, swfPathAndApplicationId.first, mobileParams, false);

        if (packAndInstallToAndroidDevice(module, flexSdk, packageParameters, swfPathAndApplicationId.second, false)) {
          launchOnAndroidDevice(project, flexSdk, swfPathAndApplicationId.second, false);
        }
        return null;
      }
    }

    return isRunAsAir(flexRunnerParameters)
           ? launchAir(project, executor, state, contentToReuse, env, flexRunnerParameters)
           : launchFlex(project, executor, state, contentToReuse, env, flexSdk, flexRunnerParameters);
  }

  private static void launchExistingPackage(final Project project, final Sdk flexSdk, final AirMobileRunnerParameters mobileParams)
    throws ExecutionException {

    final String appId = MobileAirUtil.getAppIdFromPackage(mobileParams.getExistingPackagePath());
    if (appId == null) {
      throw new ExecutionException("Failed to get application id for package: " + mobileParams.getExistingPackagePath());
    }

    if (installToDevice(project, flexSdk, mobileParams, appId)) {
      launchOnAndroidDevice(project, flexSdk, appId, false);
    }
  }

  @Nullable
  private RunContentDescriptor launchFlex(final Project project,
                                          final Executor executor,
                                          final RunProfileState state,
                                          final RunContentDescriptor contentToReuse,
                                          final ExecutionEnvironment env,
                                          final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    switch (flexRunnerParameters.getRunMode()) {
      case HtmlOrSwfFile:
        if (flexRunnerParameters instanceof FlexUnitRunnerParameters) {
          return launchWebFlexUnit(project, executor, contentToReuse, env, (FlexUnitCommonParameters)flexRunnerParameters,
                                   flexRunnerParameters.getHtmlOrSwfFilePath());
        }

        launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), flexRunnerParameters.getLauncherParameters());
        return null;
      case Url:
        launchWithSelectedApplication(flexRunnerParameters.getUrlToLaunch(), flexRunnerParameters.getLauncherParameters());
        return null;
      case MainClass:
        // A sort of hack. HtmlOrSwfFilePath field is disabled in UI for MainClass-based run configuration. But it is set correctly in RunMainClassPrecompileTask.execute()
        launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), flexRunnerParameters.getLauncherParameters());
        return null;
      case ConnectToRunningFlashPlayer:
        assert false;
    }
    return null;
  }

  protected RunContentDescriptor launchWebFlexUnit(final Project project,
                                                   final Executor executor,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitCommonParameters params,
                                                   final String swfFilePath) throws ExecutionException {
    final SwfPolicyFileConnection policyFileConnection = new SwfPolicyFileConnection();
    policyFileConnection.open(params.getSocketPolicyPort());

    final FlexUnitConnection flexUnitConnection = new FlexUnitConnection();
    flexUnitConnection.open(params.getPort());
    final ProcessHandler processHandler = new DefaultDebugProcessHandler() {
      @Override
      protected void destroyProcessImpl() {
        flexUnitConnection.write("Finish");
        flexUnitConnection.close();

        policyFileConnection.close();
        super.destroyProcessImpl();
      }

      @Override
      public boolean detachIsDefault() {
        return false;
      }
    };

    final ExecutionConsole console = createFlexUnitRunnerConsole(project, env, processHandler, executor);
    flexUnitConnection.addListener(new FlexUnitListener(processHandler));

    launchWithSelectedApplication(swfFilePath, params.getLauncherParameters());

    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
    contentBuilder.setExecutionResult(new DefaultExecutionResult(console, processHandler));
    contentBuilder.setEnvironment(env);
    Disposer.register(project, contentBuilder);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @Nullable
  protected RunContentDescriptor launchAirFlexUnit(final Project project,
                                                   final Executor executor,
                                                   final RunProfileState state,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitCommonParameters params) throws ExecutionException {
    final ExecutionResult executionResult;
    final SwfPolicyFileConnection policyFileConnection = new SwfPolicyFileConnection();
    policyFileConnection.open(params.getSocketPolicyPort());

    final FlexUnitConnection flexUnitConnection = new FlexUnitConnection();
    flexUnitConnection.open(params.getPort());

    executionResult = state.execute(executor, this);
    if (executionResult == null) {
      flexUnitConnection.close();
      policyFileConnection.close();
      return null;
    }
    flexUnitConnection.addListener(new FlexUnitListener(executionResult.getProcessHandler()));
    executionResult.getProcessHandler().addProcessListener(new ProcessAdapter() {
      @Override
      public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
        flexUnitConnection.write("Finish");
      }

      @Override
      public void processTerminated(ProcessEvent event) {
        flexUnitConnection.close();
        policyFileConnection.close();
      }
    });

    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
    contentBuilder.setExecutionResult(executionResult);
    contentBuilder.setEnvironment(env);
    return contentBuilder.showRunContent(contentToReuse);
  }

  @Nullable
  private RunContentDescriptor launchAir(final Project project,
                                         final Executor executor,
                                         final RunProfileState state,
                                         final RunContentDescriptor contentToReuse,
                                         final ExecutionEnvironment env,
                                         final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    if (flexRunnerParameters instanceof FlexUnitRunnerParameters) {
      return launchAirFlexUnit(project, executor, state, contentToReuse, env, (FlexUnitCommonParameters)flexRunnerParameters);
    }

    final ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) return null;

    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
    contentBuilder.setExecutionResult(executionResult);
    contentBuilder.setEnvironment(env);
    return contentBuilder.showRunContent(contentToReuse);
  }

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlexIdeRunConfiguration ||
            profile instanceof NewFlexUnitRunConfiguration ||
            (profile instanceof FlexRunConfiguration &&
             ((FlexRunConfiguration)profile).getRunnerParameters().getRunMode() !=
             FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer));
  }

  @NotNull
  public String getRunnerId() {
    return "FlexRunner";
  }

  private static class FlexUnitListener implements FlexUnitConnection.Listener {
    private final ProcessHandler myProcessHandler;

    public FlexUnitListener(ProcessHandler processHandler) {
      myProcessHandler = processHandler;
    }

    public void statusChanged(FlexUnitConnection.ConnectionStatus status) {
      if (status == FlexUnitConnection.ConnectionStatus.CONNECTION_FAILED || status == FlexUnitConnection.ConnectionStatus.DISCONNECTED) {
        myProcessHandler.destroyProcess();
      }
    }

    public void onData(final String line) {
      Runnable runnable = new Runnable() {
        public void run() {
          myProcessHandler.notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT);
        }
      };
      if (ApplicationManager.getApplication().isUnitTestMode()) {
        try {
          SwingUtilities.invokeAndWait(runnable);
        }
        catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
        catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
      else {
        runnable.run();
      }
    }

    public void onFinish() {
      // ignore
    }
  }
}
