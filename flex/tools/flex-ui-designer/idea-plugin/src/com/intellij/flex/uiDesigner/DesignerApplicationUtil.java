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
import com.intellij.flex.uiDesigner.io.IOUtil;
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
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Consumer;
import com.intellij.xdebugger.*;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class DesignerApplicationUtil {
  @NonNls private static final Pattern INFO_PLIST_VERSION_PATTERN =
    Pattern.compile("<key>CFBundleVersion</key>\\s*<string>(.*)</string>");

  private static final Set<String> alreadyMadeExecutable = new THashSet<String>();

  private static final Logger LOG = Logger.getInstance(DesignerApplicationUtil.class.getName());
  private static final String MAC_AIR_RUNTIME_DEFAULT_PATH = "/Library/Frameworks";

  public static AdlRunConfiguration createTestAdlRunConfiguration() throws IOException {
    return new AdlRunConfiguration("/Developer/SDKs/flex_4.5.1/bin/adl", MAC_AIR_RUNTIME_DEFAULT_PATH);
  }
  
  public static @Nullable AdlRunConfiguration findSuitableFlexSdk(String checkDescriptorPath) throws IOException {
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      return createTestAdlRunConfiguration();
    }

    if (!DebugPathManager.IS_DEV) {
      IOUtil.saveStream(FlexUIDesignerApplicationManager.class.getClassLoader().getResource(
        FlexUIDesignerApplicationManager.CHECK_DESCRIPTOR_XML), new File(checkDescriptorPath));
    }

    final List<Sdk> sdks = new ArrayList<Sdk>();
    for (Sdk sdk: ProjectJdkTable.getInstance().getAllJdks()) {
      if (sdk.getSdkType() instanceof IFlexSdkType && StringUtil.compareVersionNumbers(sdk.getVersionString(), "4.5") >= 0) {
        sdks.add(sdk);
      }
    }

    Collections.sort(sdks, new Comparator<Sdk>() {
      @Override
      public int compare(Sdk o1, Sdk o2) {
        return StringUtil.compareVersionNumbers(o2.getVersionString(), o1.getVersionString());
      }
    });

    final String installedRuntime = findInstalledRuntime();
    for (Sdk sdk : sdks) {
      final String adlPath = FlexSdkUtils.getAdlPath(sdk);
      if (StringUtil.isEmpty(adlPath) || !new File(adlPath).exists()) {
        continue;
      }

      final String runtime = FlexSdkUtils.getAirRuntimePath(sdk);
      if (checkRuntime(adlPath, runtime, checkDescriptorPath) || checkRuntime(adlPath, installedRuntime, checkDescriptorPath)) {
        return new AdlRunConfiguration(adlPath, runtime);
      }
    }

    return null;
  }

  private static boolean checkRuntime(String adlPath, String runtimePath, String checkDescriptorPath) throws IOException {
    if (StringUtil.isEmpty(runtimePath)) {
      return false;
    }

    File runtime = new File(runtimePath);
    if (!runtime.isDirectory()) {
      return false;
    }

    if (SystemInfo.isMac && !checkMacRuntimeVersion(runtimePath)) {
      return false;
    }

    final List<String> command = new ArrayList<String>();
    command.add(adlPath);
    command.add("-runtime");
    command.add(runtimePath);
    command.add("-nodebug");
    command.add(checkDescriptorPath);

    final Process checkProcess = new ProcessBuilder(command).start();
    final Integer exitCode;
    try {
      exitCode = ApplicationManager.getApplication().executeOnPooledThread(new Callable<Integer>() {
        @Override
        public Integer call() throws Exception {
          return checkProcess.waitFor();
        }
      }).get(5, TimeUnit.SECONDS);
    }
    catch (InterruptedException e) {
      checkProcess.destroy();
      return false;
    }
    catch (java.util.concurrent.ExecutionException e) {
      LOG.error(e);
      checkProcess.destroy();
      return false;
    }
    catch (TimeoutException e) {
      checkProcess.destroy();
      return false;
    }

    return exitCode == 7;
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

    Matcher m = INFO_PLIST_VERSION_PATTERN.matcher(FileUtil.loadFile(info));
    return m.find() && StringUtil.compareVersionNumbers(m.group(1), "2.6") >= 0;
  }

  public static void runDebugger(final Module module, final AdlRunTask task) throws ExecutionException {
    RunManagerEx runManager = RunManagerEx.getInstanceEx(module.getProject());
    final RunnerAndConfigurationSettings settings = runManager.createConfiguration("FlexUIDesigner", FlexRunConfigurationType.getFactory());
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

  public static ProcessHandler runAdl(AdlRunConfiguration runConfiguration, String descriptor, final @Nullable Consumer<Integer> adlExitHandler) throws IOException {
    return runAdl(runConfiguration, descriptor, null, adlExitHandler);
  }

  public static ProcessHandler runAdl(AdlRunConfiguration runConfiguration, String descriptor, @Nullable String root, final @Nullable Consumer<Integer> adlExitHandler) throws IOException {
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

    if (runConfiguration.arguments != null) {
      command.add("--");
      command.addAll(runConfiguration.arguments);
    }

    OSProcessHandler processHandler = new MyOSProcessHandler(command, adlExitHandler);
    processHandler.startNotify();
    return processHandler;
  }

  private static synchronized void ensureExecutable(String path) throws IOException {
    if (!SystemInfo.isWindows && !alreadyMadeExecutable.contains(path)) {
      File file = new File(path);
      if (!file.exists()) {
        throw new IOException("ADL not found " + file.getPath());
      }
      else if (!file.canExecute() && !file.setExecutable(true)) {
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