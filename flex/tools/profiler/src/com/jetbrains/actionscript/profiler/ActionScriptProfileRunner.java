package com.jetbrains.actionscript.profiler;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.lang.javascript.flex.build.FlexBuildConfiguration;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexIdeBuildConfiguration;
import com.intellij.lang.javascript.flex.run.FlashPlayerTrustUtil;
import com.intellij.lang.javascript.flex.run.FlexIdeRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunConfiguration;
import com.intellij.lang.javascript.flex.run.FlexRunner;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.PlatformUtils;
import com.intellij.util.SystemProperties;
import com.jetbrains.actionscript.profiler.model.ProfileSettings;
import com.jetbrains.actionscript.profiler.ui.ActionScriptProfileControlPanel;
import com.jetbrains.actionscript.profiler.ui.ActionScriptProfileSettings;
import com.jetbrains.profiler.DefaultProfilerExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * User: Maxim
 * Date: 13.07.2010
 * Time: 13:32:04
 */
public class ActionScriptProfileRunner implements ProgramRunner<ProfileSettings> {
  private static final Logger LOG = Logger.getInstance(ActionScriptProfileRunner.class.getName());
  private static final String TOOLWINDOW_ID = ProfilerBundle.message("window.name");

  private static final String PROFILE = "Profile";
  private static final String PRELOAD_SWF_OPTION = "PreloadSwf";
  private final FlexRunner myFlexRunner = new FlexRunner();
  private static final char DELIMITER = '=';

  @NotNull
  public String getRunnerId() {
    return PROFILE;
  }

  public boolean canRun(@NotNull String executorId, @NotNull RunProfile runProfile) {
    return executorId.equals(DefaultProfilerExecutor.EXECUTOR_ID) &&
           (runProfile instanceof FlexRunConfiguration || runProfile instanceof FlexIdeRunConfiguration);
  }

  public ProfileSettings createConfigurationData(ConfigurationInfoProvider configurationInfoProvider) {
    return new ProfileSettings();
  }

  public void checkConfiguration(RunnerSettings runnerSettings, ConfigurationPerRunnerSettings configurationPerRunnerSettings)
    throws RuntimeConfigurationException {
    myFlexRunner.checkConfiguration(runnerSettings, configurationPerRunnerSettings);
  }

  public void onProcessStarted(RunnerSettings runnerSettings, ExecutionResult executionResult) {
    myFlexRunner.onProcessStarted(runnerSettings, executionResult);
  }

  public AnAction[] createActions(ExecutionResult executionResult) {
    return myFlexRunner.createActions(executionResult);
  }

  public SettingsEditor<ProfileSettings> getSettingsEditor(Executor executor, RunConfiguration runConfiguration) {
    return new ActionScriptProfileSettings();
  }

  public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment) throws ExecutionException {
    execute(executor, executionEnvironment, null);
  }

  public void execute(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment, @Nullable Callback callback)
    throws ExecutionException {
    final RunnerSettings runnerSettings = executionEnvironment.getRunnerSettings();
    if (runnerSettings == null) {
      return; // TODO: what does this mean?
    }
    RunProfile runProfile = executionEnvironment.getRunProfile();
    boolean started;
    if (runProfile instanceof FlexRunConfiguration) {
      started = startProfiling((FlexRunConfiguration)runProfile, (ProfileSettings)runnerSettings.getData());
    }
    else {
      started = startProfiling((FlexIdeRunConfiguration)runProfile, (ProfileSettings)runnerSettings.getData());
    }
    if (!started) {
      return;
    }

    Executor executorById = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
    myFlexRunner.execute(executorById, executionEnvironment, callback);
  }

  private boolean startProfiling(RunProfileWithCompileBeforeLaunchOption state, ProfileSettings profileSettings) {
    Module[] modules = state.getModules();
    if (modules.length > 0) {
      startProfiling(state.getName(), modules[0], profileSettings);
      return true;
    }
    else {
      // TODO error message
      return false;
    }
  }

  private void startProfiling(final String runConfigurationName, final Module module, final ProfileSettings profileSettings) {
    if (!initProfilingAgent(module, profileSettings.getHost(), profileSettings.getPort())) {
      return;
    }

    final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(module.getProject());
    if (toolWindowManager == null) {
      return;
    }
    ApplicationManager.getApplication().invokeLater(new Runnable() {
      @Override
      public void run() {
        ToolWindow toolWindow = toolWindowManager.getToolWindow(TOOLWINDOW_ID);
        if (toolWindow == null) {
          toolWindow = toolWindowManager.registerToolWindow(TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM, module.getProject(), true);
          toolWindow.setToHideOnEmptyContent(true);
        }
        final ActionScriptProfileControlPanel profileControlPanel = new ActionScriptProfileControlPanel(runConfigurationName, module, profileSettings.getHost(), profileSettings.getPort());

        final SimpleToolWindowPanel toolWindowPanel = new SimpleToolWindowPanel(false, true);
        toolWindowPanel.setContent(profileControlPanel.getMainPanel());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, profileControlPanel.createProfilerActionGroup(), false);
        toolbar.setTargetComponent(toolWindowPanel);
        toolWindowPanel.setToolbar(toolbar.getComponent());

        final Content content = ContentFactory.SERVICE.getInstance().createContent(toolWindowPanel, runConfigurationName, false);
        toolWindow.getContentManager().addContent(content);
        toolWindow.getContentManager().setSelectedContent(content);
        content.setDisposer(profileControlPanel);

        final ToolWindow finalToolWindow = toolWindow;
        profileControlPanel.setConnectionCallback(new Runnable() {
          @Override
          public void run() {
            finalToolWindow.show(null);
            removePreloadingOfProfilerSwf();
          }
        });

        toolWindow.hide(null);
        profileControlPanel.startProfiling();
      }
    });
  }

  private static boolean initProfilingAgent(Module module, String host, int port) {
    try {
      String agentName = detectSuitableAgentNameForSdkUsedToLaunch(module);

      URL resource = ActionScriptProfileRunner.class.getResource("/" + agentName);
      File agentFile = null;
      if (resource != null) {
        agentFile = new File(transformEncodedSymbols(resource));
      }
      else {
        resource = ActionScriptProfileRunner.class.getResource("ActionScriptProfileRunner.class");
        if ("jar".equals(resource.getProtocol())) {
          String filePath = resource.getFile();
          filePath = filePath.substring(0, filePath.indexOf("!/"));

          // skip file:
          filePath = transformEncodedSymbols(new URL(filePath));
          agentFile = new File(new File(filePath).getParentFile().getPath() + File.separator + agentName);
        }
      }

      assert agentFile != null && agentFile.exists() : "Have not found " + agentName;
      String pathToAgent = agentFile.getAbsolutePath();

      pathToAgent += "?port=" + port + "&host=" + host;
      FlashPlayerTrustUtil.updateTrustedStatus(module.getProject(), true, false, pathToAgent);
      begFlashPlayerToPreloadProfilerSwf(pathToAgent);
    }
    catch (IOException e) {
      LOG.warn(e);
      return false;
    }
    return true;
  }

  private static String transformEncodedSymbols(URL url) {
    String filePath;
    try { // care of encoded spaces
      filePath = url.toURI().getSchemeSpecificPart();
    }
    catch (URISyntaxException ex) {
      filePath = url.getPath();
    }
    return filePath;
  }

  private static String detectSuitableAgentNameForSdkUsedToLaunch(Module module) {
    boolean isPlayer9;
    if (PlatformUtils.isFlexIde()) {
      FlexIdeBuildConfiguration bc = FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration();
      isPlayer9 = bc.getDependencies().getTargetPlayer().startsWith("9");
    }
    else {
      FlexBuildConfiguration bc = FlexBuildConfiguration.getConfigForFlexModuleOrItsFlexFacets(module).iterator().next();
      isPlayer9 = bc.TARGET_PLAYER_VERSION.startsWith("9");
    }
    return "profiler_agent" + (isPlayer9 ? "_9" : "_10") + ".swf";
  }

  private static void begFlashPlayerToPreloadProfilerSwf(final String pathToAgent) throws IOException {
    processMmCfg(new ProfilerPathMmCfgFixer() {
      public String additionalOptions(String lineEnd) {
        LOG.debug("Added profiler swf reference to mm.cfg");
        return PRELOAD_SWF_OPTION + "=" + pathToAgent;
      }
    });
  }

  private static void removePreloadingOfProfilerSwf() {
    try {
      processMmCfg(new ProfilerPathMmCfgFixer() {
        @Nullable
        public String additionalOptions(String lineEnd) {
          LOG.debug("Removed profiler swf reference from mm.cfg");
          return null;
        }
      });
    }
    catch (IOException e) {
      LOG.error(e);
    }
  }


  interface MmCfgFixer {
    String processOption(String option, String value, String line);

    String additionalOptions(String lineEnd);
  }

  static abstract class ProfilerPathMmCfgFixer implements MmCfgFixer {
    @Nullable
    public String processOption(String option, String value, String line) {
      if (!PRELOAD_SWF_OPTION.equals(option)) return line;
      return null;
    }
  }

  private static void processMmCfg(MmCfgFixer mmCfgFixer) throws IOException {
    StringBuilder mmCfgContent = new StringBuilder();
    String mmCfgPath = SystemProperties.getUserHome() + File.separator + "mm.cfg";
    final File file = new File(mmCfgPath);
    String lineEnd = System.getProperty("line.separator");

    if (file.exists()) {
      final LineNumberReader lineNumberReader =
        new LineNumberReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(file))));

      while (true) {
        final String s = lineNumberReader.readLine();
        if (s == null) break;
        final int i = s.indexOf(DELIMITER);
        if (i != -1) {
          String value = s.substring(i + 1);
          String name = s.substring(0, i);
          final String result = mmCfgFixer.processOption(name, value, s);
          if (result != null) mmCfgContent.append(result).append(lineEnd);
        }
        else {
          mmCfgContent.append(s).append(lineEnd); // TODO
        }
      }
    }

    final String options = mmCfgFixer.additionalOptions(lineEnd);
    if (options != null) mmCfgContent.insert(0, options + lineEnd);

    FileUtil.writeToFile(file, mmCfgContent.toString().getBytes());
  }
}
