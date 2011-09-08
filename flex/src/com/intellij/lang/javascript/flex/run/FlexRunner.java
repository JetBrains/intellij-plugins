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
import com.intellij.lang.javascript.flex.actions.airmobile.MobileAirUtil;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConnection;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.flexunit.SwfPolicyFileConnection;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
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
        final ExecutionResult executionResult = state.execute(executor, this);
        if (executionResult == null) return null;

        final RunContentBuilder contentBuilder = new RunContentBuilder(module.getProject(), this, executor);
        contentBuilder.setExecutionResult(executionResult);
        contentBuilder.setEnvironment(environment);
        return contentBuilder.showRunContent(contentToReuse);
      case Mobile:
        // todo implement
        break;
    }
    return null;
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
        return launchExistingPackage(project, flexSdk, mobileParams);
      }
      else {
        final Pair<String, String> swfPathAndApplicationId = getSwfPathAndApplicationId(mobileParams);
        return packAndInstallToDevice(project, flexSdk, mobileParams, swfPathAndApplicationId.first,
                                      swfPathAndApplicationId.second, false)
               ? launchOnDevice(project, flexSdk, mobileParams, swfPathAndApplicationId.second, false)
               : null;
      }
    }

    return isRunAsAir(flexRunnerParameters)
           ? launchAir(project, executor, state, contentToReuse, env, flexRunnerParameters)
           : launchFlex(project, executor, state, contentToReuse, env, flexSdk, flexRunnerParameters);
  }

  private static RunContentDescriptor launchExistingPackage(final Project project,
                                                            final Sdk flexSdk,
                                                            final AirMobileRunnerParameters mobileParams)
    throws ExecutionException {
    final String appId = MobileAirUtil.getAppIdFromPackage(mobileParams.getExistingPackagePath());
    if (appId == null) {
      throw new ExecutionException("Failed to get application id for package: " + mobileParams.getExistingPackagePath());
    }
    return installToDevice(project, flexSdk, mobileParams, appId) ? launchOnDevice(project, flexSdk, mobileParams, appId, false) : null;
  }

  @Nullable
  private RunContentDescriptor launchFlex(final Project project,
                                          final Executor executor,
                                          final RunProfileState state,
                                          final RunContentDescriptor contentToReuse,
                                          final ExecutionEnvironment env,
                                          final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    final LauncherParameters launcherParams = new LauncherParameters(flexRunnerParameters.getLauncherType(),
                                                                     flexRunnerParameters.getBrowserFamily(),
                                                                     flexRunnerParameters.getPlayerPath());
    switch (flexRunnerParameters.getRunMode()) {
      case HtmlOrSwfFile:
        if (flexRunnerParameters instanceof FlexUnitRunnerParameters) {
          final SwfPolicyFileConnection policyFileConnection = new SwfPolicyFileConnection();
          final FlexUnitRunnerParameters params = (FlexUnitRunnerParameters)flexRunnerParameters;
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

          launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), launcherParams);

          final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
          contentBuilder.setExecutionResult(new DefaultExecutionResult(console, processHandler));
          contentBuilder.setEnvironment(env);
          Disposer.register(project, contentBuilder);
          return contentBuilder.showRunContent(contentToReuse);
        }

        launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), launcherParams);
        return null;
      case Url:
        launchWithSelectedApplication(flexRunnerParameters.getUrlToLaunch(), launcherParams);
        return null;
      case MainClass:
        // A sort of hack. HtmlOrSwfFilePath field is disabled in UI for MainClass-based run configuration. But it is set correctly in RunMainClassPrecompileTask.execute()
        launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), launcherParams);
        return null;
      case ConnectToRunningFlashPlayer:
        assert false;
    }
    return null;
  }

  @Nullable
  private RunContentDescriptor launchAir(final Project project,
                                         final Executor executor,
                                         final RunProfileState state,
                                         final RunContentDescriptor contentToReuse,
                                         final ExecutionEnvironment env,
                                         final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    final ExecutionResult executionResult;
    if (flexRunnerParameters instanceof FlexUnitRunnerParameters) {
      final SwfPolicyFileConnection policyFileConnection = new SwfPolicyFileConnection();
      final FlexUnitRunnerParameters params = (FlexUnitRunnerParameters)flexRunnerParameters;
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
    }
    else {
      executionResult = state.execute(executor, this);
      if (executionResult == null) return null;
    }

    final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
    contentBuilder.setExecutionResult(executionResult);
    contentBuilder.setEnvironment(env);
    return contentBuilder.showRunContent(contentToReuse);
  }

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlexIdeRunConfiguration ||
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
