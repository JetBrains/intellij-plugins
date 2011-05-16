package com.intellij.flex.uiDesigner;

import com.intellij.compiler.options.CompileStepBeforeRun;
import com.intellij.execution.*;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.lang.javascript.flex.IFlexSdkType;
import com.intellij.lang.javascript.flex.debug.FlexDebugProcess;
import com.intellij.lang.javascript.flex.run.FlexBaseRunner;
import com.intellij.lang.javascript.flex.run.FlexRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunConfigurationType;
import com.intellij.lang.javascript.flex.run.FlexRunnerParameters;
import com.intellij.lang.javascript.flex.sdk.FlexSdkUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class DesignerApplicationUtil {
  private static final String versionKey = "CFBundleVersion";
  private static final Set<String> alreadyMadeExecutable = new THashSet<String>();

  private static final Logger LOG = Logger.getInstance(DesignerApplicationUtil.class.getName());
  private static final String MAC_AIR_RUNTIME_DEFAULT_PATH = "/Library/Frameworks";

  public static AdlRunConfiguration createTestAdlRunConfiguration() throws IOException {
    return new AdlRunConfiguration("/Developer/SDKs/flex_sdk_4.5/bin/adl", MAC_AIR_RUNTIME_DEFAULT_PATH);
  }
  
  public static @Nullable AdlRunConfiguration findSuitableFlexSdk() throws IOException {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return createTestAdlRunConfiguration();
    }

    String adlPath;
    String runtime = findInstalledRuntime();
    for (Sdk sdk : ProjectJdkTable.getInstance().getAllJdks()) {
      SdkType sdkType = sdk.getSdkType();
      if (!(sdkType instanceof IFlexSdkType)) {
        continue;
      }

      String version = sdk.getVersionString();
      // at least 4.1
      if (version == null || !(version.length() >= 3 &&
                               (version.charAt(0) > '4' || (version.charAt(0) == '4' && version.charAt(2) >= '5')))) {
        continue;
      }

      adlPath = FlexSdkUtils.getAdlPath(sdk);
      if (StringUtil.isEmpty(adlPath) || !new File(adlPath).exists()) {
        continue;
      }

      if (runtime == null) {
        runtime = FlexSdkUtils.getAirRuntimePath(sdk);
        if (!checkRuntime(runtime)) {
          runtime = null;
          continue;
        }
      }

      return new AdlRunConfiguration(adlPath, runtime);
    }

    return null;
  }

  private static boolean checkRuntime(String runtimePath) throws IOException {
    if (StringUtil.isEmpty(runtimePath)) {
      return false;
    }

    File runtime = new File(runtimePath);
    if (!runtime.isDirectory()) {
      return false;
    }

    //noinspection SimplifiableIfStatement
    if (SystemInfo.isMac) {
      return checkMacRuntimeVersion(runtimePath);
    }

    return true;
  }

  // http://kb2.adobe.com/cps/407/kb407625.html
  private static String findInstalledRuntime() throws IOException {
    if (SystemInfo.isMac) {
      String runtime = MAC_AIR_RUNTIME_DEFAULT_PATH;
      if (checkMacRuntimeVersion(runtime)) {
        return runtime;
      }
    }

    return null;
  }

  private static boolean checkMacRuntimeVersion(String runtime) throws IOException {
    File info = new File(runtime, "Adobe AIR.framework/Resources/Info.plist");
    if (!info.exists()) {
      return false;
    }

    char[] chars = FileUtil.loadFileText(info);
    ol: for (int i = 16; i < chars.length; i++) {
      if (chars[i] == 'k' && chars[i - 1] == '<') {
        i += 4;
        for (int j = 0; j < versionKey.length() && i < chars.length; ) {
          if (versionKey.charAt(j++) != chars[i++]) {
            i += 8;
            continue ol;
          }
        }

        i += 6;
        while (i < chars.length) {
          if (chars[i++] == '<') {
            if (chars[i + 7] >= '2') {
              return true;
            }
          }
        }
      }
    }
    
    return false;
  }

  public static void runDebugger(final Module module, final AdlRunTask task) throws ExecutionException {
    RunManagerEx runManager = RunManagerEx.getInstanceEx(module.getProject());
    final RunnerAndConfigurationSettings settings = runManager.createConfiguration("FlexUIDesigner", 
    FlexRunConfigurationType.getFactory());
    final FlexRunnerParameters runnerParameters = ((FlexRunConfiguration)settings.getConfiguration()).getRunnerParameters();
    runnerParameters.setRunMode(FlexRunnerParameters.RunMode.ConnectToRunningFlashPlayer);
    runnerParameters.setModuleName(module.getName());

    final CompileStepBeforeRun.MakeBeforeRunTask runTask =
      runManager.getBeforeRunTask(settings.getConfiguration(), CompileStepBeforeRun.ID);
    if (runTask != null) {
      runTask.setEnabled(false);
    }

    // we need SILENTLY_DETACH_ON_CLOSE, but RunContentManagerImpl provides only ProcessHandler.SILENTLY_DESTROY_ON_CLOSE, so, 
    // we override destroyProcess as detachProcess
    final FlexBaseRunner runner = new FlexBaseRunner() {
      @Override
      protected RunContentDescriptor doLaunch(final Project project, final Executor executor, RunProfileState state,
                                              RunContentDescriptor contentToReuse, final ExecutionEnvironment env, final Sdk flexSdk,
                                              final FlexRunnerParameters flexRunnerParameters) throws ExecutionException {
        return XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
          @NotNull
          public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
            try {
              return new FlexDebugProcess(session, flexSdk, flexRunnerParameters) {
                @Override
                public void stop() {
                  if (DebugPathManager.IS_DEV) {
                    super.stop();
                  }
                }

                @Override
                protected ProcessHandler doGetProcessHandler() {
                  return new MyDefaultDebugProcessHandler();
                }
              };
            }
            catch (IOException e) {
              throw new ExecutionException(e.getMessage(), e);
            }
          }
        }).getRunContentDescriptor();
      }

      @Override
      @NotNull
      public String getRunnerId() {
        return "FlexDebugRunnerForDesignView";
      }

      @Override
      public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return true;
      }
    };

    final DefaultDebugExecutor executor = new DefaultDebugExecutor();
    runner.execute(executor, new ExecutionEnvironment(runner, settings, module.getProject()), new ProgramRunner.Callback() {
      @Override
      public void processStarted(final RunContentDescriptor descriptor) {
        final MyDefaultDebugProcessHandler processHandler = (MyDefaultDebugProcessHandler)descriptor.getProcessHandler();
        assert processHandler != null;
        //noinspection deprecation
        processHandler.putUserData(ProcessHandler.SILENTLY_DESTROY_ON_CLOSE, true);
        task.onAdlExit(new Runnable() {
          @Override
          public void run() {
            Project project = module.getProject();
            if (!project.isDisposed()) {
              ExecutionManager.getInstance(project).getContentManager().removeRunContent(executor, descriptor);
            }
            
            processHandler.myDestroyProcess();
          }
        }
        );
        task.run();
      }
    });
  }
  
  private static class MyDefaultDebugProcessHandler extends DefaultDebugProcessHandler {
    @Override
    public void destroyProcess() {
      if (DebugPathManager.IS_DEV) {
        super.destroyProcess();
      }
      else {
        detachProcess();
      }
    }
    
    private void myDestroyProcess() {
      super.destroyProcess();
    }
  }

  public static ProcessHandler runAdl(AdlRunConfiguration adlRunConfiguration, String descriptor, int port,
                               final @Nullable Consumer<Integer> adlExitHandler) throws IOException {
    return runAdl(adlRunConfiguration, descriptor, port, null, adlExitHandler);
  }

  public static ProcessHandler runAdl(AdlRunConfiguration runConfiguration, String descriptor, int port, @Nullable String root,
                               final @Nullable Consumer<Integer> adlExitHandler) throws IOException {
    ensureExecutable(runConfiguration.adlPath);

    List<String> command = new ArrayList<String>();
    command.add(runConfiguration.adlPath);
    if (runConfiguration.runtime != null) {
      command.add("-runtime");
      command.add(runConfiguration.runtime);
    }

    // see http://confluence.jetbrains.net/display/IDEA/Flex+UI+Designer about nodebug
    //if (!runConfiguration.debug) {
    //  command.add("-nodebug");
    //}

    command.add(descriptor);
    if (root != null) {
      command.add(root);
    }

    command.add("--");
    command.add(String.valueOf(port));
    if (runConfiguration.arguments != null) {
      command.addAll(runConfiguration.arguments);
    }

    OSProcessHandler processHandler = new MyOSProcessHandler(command, adlExitHandler);
    processHandler.startNotify();
    return processHandler;
  }

  private static synchronized void ensureExecutable(String path) throws IOException {
    if (!SystemInfo.isWindows && !alreadyMadeExecutable.contains(path)) {
      File file = new File(path);
      if (!file.canExecute() && !file.setExecutable(true)) {
        throw new IOException("ADL is not executable");
      }
      alreadyMadeExecutable.add(path);
    }
  }

  public static class AdlRunConfiguration {
    private final String adlPath;
    private final @Nullable String runtime;

    public boolean debug;
    public @Nullable List<String> arguments;

    public AdlRunConfiguration(String adlPath, @Nullable String runtime) {
      this.adlPath = adlPath;
      this.runtime = runtime;
    }
  }

  public static abstract class AdlRunTask implements Runnable {
    protected Runnable onAdlExit;
    protected final DesignerApplicationUtil.AdlRunConfiguration runConfiguration;

    public AdlRunTask(AdlRunConfiguration runConfiguration) {
      this.runConfiguration = runConfiguration;
    }

    public void onAdlExit(Runnable runnable) {
      onAdlExit = runnable;
    }
  }

  private static class MyOSProcessHandler extends OSProcessHandler {
    private Consumer<Integer> adlExitHandler;

    public MyOSProcessHandler(List<String> command, Consumer<Integer> adlExitHandler) throws IOException {
      super(new ProcessBuilder(command).start(), null);
      this.adlExitHandler = adlExitHandler;
    }

    @Override
    public void notifyTextAvailable(String text, Key outputType) {
      LOG.debug("[adl output/error stream]: " + text);
    }

    @Override
    protected void destroyProcessImpl() {
      super.destroyProcessImpl();
      callExitHandler(0);
    }

    @Override
    protected void onOSProcessTerminated(int exitCode) {
      callExitHandler(exitCode);
    }

    private void callExitHandler(int exitCode) {
      if (adlExitHandler != null) {
        adlExitHandler.consume(exitCode);
        
        adlExitHandler = null;
      }
    }
  }
}