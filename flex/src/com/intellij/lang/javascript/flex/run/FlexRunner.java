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
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConnection;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunConfiguration;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.flexunit.SwfPolicyFileConnection;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.options.BCUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.PathUtil;
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
                                                     final FlexIdeBuildConfiguration bc,
                                                     final FlashRunnerParameters params,
                                                     final Executor executor,
                                                     final RunProfileState state,
                                                     final RunContentDescriptor contentToReuse,
                                                     final ExecutionEnvironment environment) throws ExecutionException {
    switch (bc.getTargetPlatform()) {
      case Web:
        final String urlOrPath = params.isLaunchUrl()
                                 ? params.getUrl()
                                 : bc.isUseHtmlWrapper()
                                   ? PathUtil.getParentPath(bc.getOutputFilePath(true)) + "/" + BCUtils.getWrapperFileName(bc)
                                   : bc.getOutputFilePath(true);
        launchWithSelectedApplication(urlOrPath, params.getLauncherParameters());
        break;
      case Desktop:
        return standardLaunch(module.getProject(), executor, state, contentToReuse, environment);
      case Mobile:
        switch (params.getMobileRunTarget()) {
          case Emulator:
            return standardLaunch(module.getProject(), executor, state, contentToReuse, environment);
          case AndroidDevice:
            final String applicationId = getApplicationId(getAirDescriptorPath(bc, bc.getAndroidPackagingOptions()));
            final Sdk sdk = bc.getSdk();
            if (packAndInstallToAndroidDevice(module, sdk, createAndroidPackageParams(bc, params, false), applicationId, false)) {
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

  protected RunContentDescriptor launchWebFlexUnit(final Project project,
                                                   final Executor executor,
                                                   final RunContentDescriptor contentToReuse,
                                                   final ExecutionEnvironment env,
                                                   final FlexUnitRunnerParameters params,
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
                                                   final FlexUnitRunnerParameters params) throws ExecutionException {
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

  public boolean canRun(@NotNull final String executorId, @NotNull final RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof FlashRunConfiguration || profile instanceof FlexUnitRunConfiguration);
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
