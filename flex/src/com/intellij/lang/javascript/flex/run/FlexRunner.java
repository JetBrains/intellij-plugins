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
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.actions.airinstaller.AirInstallerParametersBase;
import com.intellij.lang.javascript.flex.actions.airinstaller.AndroidAirPackageParameters;
import com.intellij.lang.javascript.flex.actions.airinstaller.MobileAirTools;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitConnection;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitRunnerParameters;
import com.intellij.lang.javascript.flex.flexunit.SwfPolicyFileConnection;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Maxim.Mossienko
 * Date: Mar 11, 2008
 * Time: 8:16:33 PM
 */
public class FlexRunner extends FlexBaseRunner {

  @Nullable
  protected RunContentDescriptor doLaunch(final Project project,
                                          final Executor executor,
                                          final RunProfileState state,
                                          final RunContentDescriptor contentToReuse,
                                          final ExecutionEnvironment env,
                                          final Sdk flexSdk,
                                          final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
    return isRunOnDevice(flexRunnerParameters)
           ? packAndInstallToDevice(project, flexSdk, (AirMobileRunnerParameters)flexRunnerParameters, false)
             ? launchOnDevice(project, flexSdk, (AirMobileRunnerParameters)flexRunnerParameters, false)
             : null
           : isRunAsAir(flexRunnerParameters)
             ? launchAir(project, executor, state, contentToReuse, env, flexSdk, flexRunnerParameters)
             : launchFlex(project, executor, state, contentToReuse, env, flexSdk, flexRunnerParameters);
  }

  public static boolean packAndInstallToDevice(final Project project,
                                               final Sdk flexSdk,
                                               final AirMobileRunnerParameters params,
                                               final boolean isDebug) {
    final Pair<String, String> swfPathAndApplicationId = getSwfPathAndApplicationId(params);

    if (params.getAirMobileRunTarget() == AirMobileRunnerParameters.AirMobileRunTarget.AndroidDevice) {
      final Module module = ModuleManager.getInstance(project).findModuleByName(params.getModuleName());
      assert module != null;
      final AndroidAirPackageParameters packageParameters =
        createAndroidPackageParams(project, flexSdk, swfPathAndApplicationId, params, isDebug);
      final String apkPath = packageParameters.INSTALLER_FILE_LOCATION + "/" + packageParameters.INSTALLER_FILE_NAME;

      return MobileAirTools.checkAdtVersion(module, flexSdk)
             && MobileAirTools.ensureCertificateExists(project, flexSdk)
             && MobileAirTools.packageApk(project, packageParameters)
             && MobileAirTools.installApk(project, flexSdk, apkPath, swfPathAndApplicationId.second);
    }
    else {
      assert false;
    }

    return false;
  }

  @Nullable
  public static RunContentDescriptor launchOnDevice(final Project project,
                                                    final Sdk flexSdk,
                                                    final AirMobileRunnerParameters params,
                                                    final boolean isDebug) {
    final Pair<String, String> swfPathAndApplicationId = getSwfPathAndApplicationId(params);

    if (params.getAirMobileRunTarget() == AirMobileRunnerParameters.AirMobileRunTarget.AndroidDevice) {
      if (MobileAirTools.launchAndroidApplication(project, flexSdk, swfPathAndApplicationId.second)) {
        ToolWindowManager.getInstance(project).notifyByBalloon(isDebug ? ToolWindowId.DEBUG : ToolWindowId.RUN, MessageType.INFO,
                                                               FlexBundle.message("android.application.launched"));
      }
    }
    else {
      assert false;
    }

    return null;
  }

  private static Pair<String, String> getSwfPathAndApplicationId(final AirRunnerParameters params) {
    final VirtualFile descriptorFile = LocalFileSystem.getInstance().findFileByPath(params.getAirDescriptorPath());
    if (descriptorFile != null) {
      try {
        final String swfPath = FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><initialWindow><content>");
        final String applicationId = FlexUtils.findXMLElement(descriptorFile.getInputStream(), "<application><id>");
        return Pair.create(swfPath, applicationId);
      }
      catch (IOException e) {/*ignore*/}
    }
    return Pair.create(null, null);
  }

  private static AndroidAirPackageParameters createAndroidPackageParams(final Project project,
                                                                        final Sdk flexSdk,
                                                                        final Pair<String, String> swfPathAndApplicationId,
                                                                        final AirMobileRunnerParameters params,
                                                                        boolean isDebug) {
    final String swfPath = swfPathAndApplicationId.first;
    String swfName = "";
    String apkName = "";
    String outputDirPath = "";
    final int lastSlashIndex = FileUtil.toSystemIndependentName(swfPath).lastIndexOf('/');
    final String suffix = ".swf";
    if (swfPath.toLowerCase().endsWith(suffix) && lastSlashIndex < swfPath.length() - suffix.length()) {
      swfName = swfPath.substring(lastSlashIndex + 1);
      apkName = swfName.substring(0, swfName.length() - suffix.length()) + ".apk";
      outputDirPath = params.getAirRootDirPath() + (lastSlashIndex == -1 ? "" : "/" + swfPath.substring(0, lastSlashIndex));
    }

    final List<AirInstallerParametersBase.FilePathAndPathInPackage> files =
      new ArrayList<AirInstallerParametersBase.FilePathAndPathInPackage>();
    files.add(new AirInstallerParametersBase.FilePathAndPathInPackage(params.getAirRootDirPath() + "/" + swfPath, swfName));

    return new AndroidAirPackageParameters(flexSdk,
                                           params.getAirDescriptorPath(),
                                           apkName,
                                           outputDirPath,
                                           files,
                                           isDebug,
                                           isDebug,
                                           isDebug ? getHostName() : "",
                                           false,
                                           -1,
                                           "",
                                           MobileAirTools.getTempKeystorePath(),
                                           MobileAirTools.TEMP_KEYSTORE_TYPE,
                                           MobileAirTools.TEMP_KEYSTORE_PASSWORD,
                                           "",
                                           "",
                                           "",
                                           "");
  }

  private static String getHostName() {
    try {
      return InetAddress.getLocalHost().getHostAddress();
    }
    catch (UnknownHostException e) {
      return "127.0.0.1";
    }
  }

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

          launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), flexRunnerParameters);

          final RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor);
          contentBuilder.setExecutionResult(new DefaultExecutionResult(console, processHandler));
          contentBuilder.setEnvironment(env);
          Disposer.register(project, contentBuilder);
          return contentBuilder.showRunContent(contentToReuse);
        }

        launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), flexRunnerParameters);
        return null;
      case Url:
        launchWithSelectedApplication(flexRunnerParameters.getUrlToLaunch(), flexRunnerParameters);
        return null;
      case MainClass:
        // A sort of hack. HtmlOrSwfFilePath field is disabled in UI for MainClass-based run configuration. But it is set correctly in RunMainClassPrecompileTask.execute()
        launchWithSelectedApplication(flexRunnerParameters.getHtmlOrSwfFilePath(), flexRunnerParameters);
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
                                         final Sdk flexSdk,
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
           profile instanceof FlexRunConfiguration &&
           ((FlexRunConfiguration)profile).getRunnerParameters().getRunMode() != FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer;
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
