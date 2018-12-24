package com.intellij.javascript.karma.server;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.coverage.KarmaCoveragePeer;
import com.intellij.javascript.karma.execution.KarmaServerSettings;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.javascript.karma.util.StreamEventListener;
import com.intellij.javascript.nodejs.NodeCommandLineUtil;
import com.intellij.javascript.nodejs.interpreter.NodeCommandLineConfigurator;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.lang.javascript.ConsoleCommandLineFolder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.execution.ParametersListUtil;
import com.intellij.util.text.SemVer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class KarmaServer {

  private static final Logger LOG = Logger.getInstance(KarmaServer.class);

  private final KarmaProcessOutputManager myProcessOutputManager;
  private final KarmaServerState myState;
  private final KarmaCoveragePeer myCoveragePeer;
  private final KarmaServerSettings myServerSettings;
  private final ConsoleCommandLineFolder myCommandLineFolder = new ConsoleCommandLineFolder();

  private List<Runnable> myOnPortBoundCallbacks = Lists.newCopyOnWriteArrayList();
  private List<Runnable> myOnBrowsersReadyCallbacks = Lists.newCopyOnWriteArrayList();

  private Integer myExitCode = null;
  private final List<KarmaServerTerminatedListener> myTerminationCallbacks = Lists.newCopyOnWriteArrayList();

  private final Map<String, StreamEventHandler> myHandlers = ContainerUtil.newConcurrentMap();
  private final MyDisposable myDisposable;
  private final KarmaServerRestarter myRestarter;
  private final int myProcessHashCode;

  public KarmaServer(@NotNull Project project, @NotNull KarmaServerSettings serverSettings) throws IOException {
    myServerSettings = serverSettings;
    myCoveragePeer = serverSettings.isWithCoverage() ? new KarmaCoveragePeer() : null;
    KillableColoredProcessHandler processHandler = startServer(serverSettings, myCoveragePeer, myCommandLineFolder);
    myProcessHashCode = System.identityHashCode(processHandler.getProcess());
    File configurationFile = myServerSettings.getConfigurationFile();
    myState = new KarmaServerState(this, configurationFile);
    myProcessOutputManager = new KarmaProcessOutputManager(processHandler, myState::onStandardOutputLineAvailable);
    registerStreamEventHandlers();
    myProcessOutputManager.startNotify();

    myDisposable = new MyDisposable();
    Disposer.register(project, myDisposable);
    myRestarter = new KarmaServerRestarter(configurationFile, myDisposable);

    final int processHashCode = System.identityHashCode(processHandler.getProcess());
    LOG.info("Karma server " + processHashCode + " started successfully: " + processHandler.getCommandLine());
    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(@NotNull final ProcessEvent event) {
        LOG.info("Karma server " + processHashCode + " terminated with exit code " + event.getExitCode());
        Disposer.dispose(myDisposable);
        fireOnTerminated(event.getExitCode());
      }
    });
  }

  private void registerStreamEventHandlers() {
    if (myCoveragePeer != null) {
      myCoveragePeer.registerEventHandlers(this);
    }

    myProcessOutputManager.addStreamEventListener(new StreamEventListener() {
      @Override
      public void on(@NotNull String eventType, @NotNull String eventBody) {
        LOG.info("Processing Karma event " + eventType + " " + eventBody);
        JsonElement jsonElement;
        try {
          JsonParser jsonParser = new JsonParser();
          jsonElement = jsonParser.parse(eventBody);
        }
        catch (Exception e) {
          LOG.warn("Cannot parse message from karma server:" +
                   " (eventType: " + eventType + ", eventBody: " + eventBody + ")");
          return;
        }
        StreamEventHandler handler = myHandlers.get(eventType);
        if (handler != null) {
          handler.handle(jsonElement);
        }
        else {
          LOG.warn("Cannot find handler for " + eventType);
        }
      }
    });
  }

  @NotNull
  public KarmaServerSettings getServerSettings() {
    return myServerSettings;
  }

  @NotNull
  public KarmaServerRestarter getRestarter() {
    return myRestarter;
  }

  @Nullable
  public KarmaCoveragePeer getCoveragePeer() {
    return myCoveragePeer;
  }

  public void registerStreamEventHandler(@NotNull StreamEventHandler handler) {
    myHandlers.put(handler.getEventType(), handler);
  }

  @NotNull
  public ConsoleCommandLineFolder getCommandLineFolder() {
    return myCommandLineFolder;
  }

  @NotNull
  private static KillableColoredProcessHandler startServer(@NotNull KarmaServerSettings serverSettings,
                                                           @Nullable KarmaCoveragePeer coveragePeer,
                                                           @NotNull ConsoleCommandLineFolder commandLineFolder) throws IOException {
    GeneralCommandLine commandLine = ReadAction.compute(() -> {
      try {
        return createCommandLine(serverSettings, coveragePeer, commandLineFolder);
      }
      catch (ExecutionException e) {
        throw new IOException("Can not create command line", e);
      }
    });
    KillableColoredProcessHandler processHandler;
    try {
      processHandler = NodeCommandLineUtil.createKillableColoredProcessHandler(commandLine, true);
    }
    catch (ExecutionException e) {
      throw new IOException("Can not start Karma server: " + commandLine.getCommandLineString(), e);
    }
    processHandler.setShouldDestroyProcessRecursively(true);
    ProcessTerminatedListener.attach(processHandler);
    return processHandler;
  }

  @NotNull
  private static GeneralCommandLine createCommandLine(@NotNull KarmaServerSettings serverSettings,
                                                      @Nullable KarmaCoveragePeer coveragePeer,
                                                      @NotNull ConsoleCommandLineFolder commandLineFolder) throws IOException,
                                                                                                                  ExecutionException {
    NodeCommandLineConfigurator configurator = NodeCommandLineConfigurator.find(serverSettings.getNodeInterpreter());
    GeneralCommandLine commandLine = new GeneralCommandLine();
    serverSettings.getEnvData().configureCommandLine(commandLine, true);
    NodeCommandLineUtil.configureUsefulEnvironment(commandLine);
    commandLine.withWorkDirectory(serverSettings.getWorkingDirectorySystemDependent());
    commandLine.setRedirectErrorStream(true);
    List<String> nodeOptionList = ParametersListUtil.parse(serverSettings.getNodeOptions().trim());
    commandLine.addParameters(nodeOptionList);
    if (Boolean.parseBoolean(commandLine.getEnvironment().get("KARMA_SERVER_WITH_INSPECT_BRK"))) {
      try {
        NodeCommandLineUtil.addNodeOptionsForDebugging(commandLine, Collections.emptyList(), 34598, true,
                                                       serverSettings.getNodeInterpreter(), true);
      }
      catch (ExecutionException e) {
        throw new IOException(e);
      }
    }
    NodePackage pkg = serverSettings.getKarmaPackage();
    String userConfigFileName = PathUtil.getFileName(serverSettings.getConfigurationFilePath());
    boolean angularCli = KarmaUtil.isAngularCliPkg(pkg);
    if (angularCli) {
      commandLine.addParameter(pkg.getSystemDependentPath() + File.separator + "bin" + File.separator + "ng");
      commandLine.addParameter("test");
      commandLineFolder.addPlaceholderTexts("ng", "test");
      File configFile = KarmaJsSourcesLocator.getInstance().getIntellijConfigFile();
      File workingDir = new File(serverSettings.getWorkingDirectorySystemDependent());
      SemVer version = pkg.getVersion();
      if (version == null || version.isGreaterOrEqualThan(6, 0, 0)) {
        AngularCliConfig config = AngularCliConfig.findProjectConfig(workingDir);
        VirtualFile karmaConfFile = LocalFileSystem.getInstance().findFileByPath(serverSettings.getConfigurationFilePath());
        String defaultProject = config != null ? config.getProjectContainingFileOrDefault(karmaConfFile) : null;
        if (defaultProject != null) {
          commandLine.addParameter(defaultProject);
          commandLineFolder.addPlaceholderText(defaultProject);
        }
        commandLine.addParameters("--karma-config", configFile.getAbsolutePath());
        commandLineFolder.addPlaceholderText("--karma-config=" + userConfigFileName);

        commandLine.addParameter("--source-map");
      }
      else {
        String configPath = FileUtil.getRelativePath(workingDir, configFile);
        if (configPath == null) {
          configPath = configFile.getAbsolutePath();
        }
        commandLine.addParameters("--config", configPath);
        commandLineFolder.addPlaceholderText("--config=" + userConfigFileName);
      }
    }
    else {
      commandLine.addParameter(pkg.getSystemDependentPath() + File.separator + "bin" + File.separator + "karma");
      commandLine.addParameter("start");
      commandLine.addParameter(KarmaJsSourcesLocator.getInstance().getIntellijConfigFile().getAbsolutePath());
      commandLineFolder.addPlaceholderTexts("karma", "start", userConfigFileName);
    }
    String browsers = serverSettings.getBrowsers();
    if (!StringUtil.isEmptyOrSpaces(browsers)) {
      commandLine.addParameter("--browsers=" + browsers);
      commandLineFolder.addLastParameterFrom(commandLine);
    }
    setIntellijParameter(commandLine, "user_config", configurator.convertLocalPathToRemote(serverSettings.getConfigurationFilePath()));
    if (coveragePeer != null) {
      String coverageDir = configurator.convertLocalPathToRemote(coveragePeer.getCoverageTempDir().getAbsolutePath());
      setIntellijParameter(commandLine, "coverage_temp_dir", coverageDir);
      if (angularCli) {
        commandLine.addParameter("--code-coverage");
        commandLineFolder.addLastParameterFrom(commandLine);
      }
    }
    if (serverSettings.isDebug()) {
      setIntellijParameter(commandLine, "debug", "true");
    }
    commandLine.setCharset(CharsetToolkit.UTF8_CHARSET);
    configurator.configure(commandLine);
    return commandLine;
  }

  private static void setIntellijParameter(@NotNull GeneralCommandLine commandLine,
                                           @NotNull String name,
                                           @NotNull String value) {
    commandLine.getEnvironment().put("_INTELLIJ_KARMA_INTERNAL_PARAMETER_" + name, value);
  }

  public void shutdownAsync() {
    LOG.info("Shutting down asynchronously Karma server " + myProcessHashCode);
    ApplicationManager.getApplication().executeOnPooledThread(this::shutdown);
  }

  private void shutdown() {
    ProcessHandler processHandler = myProcessOutputManager.getProcessHandler();
    if (!processHandler.isProcessTerminated()) {
      ScriptRunnerUtil.terminateProcessHandler(processHandler, 1000, null);
    }
  }

  @NotNull
  public KarmaProcessOutputManager getProcessOutputManager() {
    return myProcessOutputManager;
  }

  public boolean isPortBound() {
    return myState.getServerPort() != -1;
  }

  public int getServerPort() {
    return myState.getServerPort();
  }

  /**
   * Executes {@code callback} in EDT when the server port is bound.
   */
  public void onPortBound(@NotNull final Runnable callback) {
    UIUtil.invokeLaterIfNeeded(() -> {
      if (myOnPortBoundCallbacks != null) {
        myOnPortBoundCallbacks.add(callback);
      }
      else {
        callback.run();
      }
    });
  }

  void fireOnPortBound() {
    UIUtil.invokeLaterIfNeeded(() -> {
      List<Runnable> callbacks = ContainerUtil.newArrayList(myOnPortBoundCallbacks);
      myOnPortBoundCallbacks.clear();
      myOnPortBoundCallbacks = null;
      for (Runnable callback : callbacks) {
        callback.run();
      }
    });
  }

  public boolean areBrowsersReady() {
    return myState.areBrowsersReady();
  }

  /**
   * Executes {@code callback} in EDT when at least one browser is captured and all config.browsers are captured.
   */
  public void onBrowsersReady(@NotNull final Runnable callback) {
    UIUtil.invokeLaterIfNeeded(() -> {
      if (myOnBrowsersReadyCallbacks != null) {
        myOnBrowsersReadyCallbacks.add(callback);
      }
      else {
        callback.run();
      }
    });
  }

  void fireOnBrowsersReady(boolean browsersReady) {
    UIUtil.invokeLaterIfNeeded(() -> {
      if (browsersReady) {
        List<Runnable> callbacks = ContainerUtil.newArrayList(myOnBrowsersReadyCallbacks);
        myOnBrowsersReadyCallbacks.clear();
        myOnBrowsersReadyCallbacks = null;
        for (Runnable callback : callbacks) {
          callback.run();
        }
      }
      else {
        myOnBrowsersReadyCallbacks = Lists.newCopyOnWriteArrayList();
      }
    });
  }

  /**
   * Executes {@code terminationCallback} in EDT when the server is shut down.
   */
  public void onTerminated(@NotNull final KarmaServerTerminatedListener terminationCallback) {
    UIUtil.invokeLaterIfNeeded(() -> {
      if (myExitCode != null) {
        terminationCallback.onTerminated(myExitCode);
      }
      else {
        myTerminationCallbacks.add(terminationCallback);
      }
    });
  }

  public void removeTerminatedListener(@NotNull final KarmaServerTerminatedListener listener) {
    UIUtil.invokeLaterIfNeeded(() -> myTerminationCallbacks.remove(listener));
  }

  private void fireOnTerminated(final int exitCode) {
    UIUtil.invokeLaterIfNeeded(() -> {
      myExitCode = exitCode;
      List<KarmaServerTerminatedListener> listeners = ContainerUtil.newArrayList(myTerminationCallbacks);
      myTerminationCallbacks.clear();
      for (KarmaServerTerminatedListener listener : listeners) {
        listener.onTerminated(exitCode);
      }
    });
  }

  @Nullable
  public KarmaConfig getKarmaConfig() {
    return myState.getKarmaConfig();
  }

  @NotNull
  public String formatUrlWithoutUrlRoot(@NotNull String path) {
    return formatUrl(path, false);
  }

  @SuppressWarnings("SameParameterValue")
  @NotNull
  public String formatUrl(@NotNull String path) {
    return formatUrl(path, true);
  }

  @NotNull
  private String formatUrl(@NotNull String path, boolean withUrlRoot) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    KarmaConfig config = myState.getKarmaConfig();
    if (config != null) {
      String baseUrl = config.getProtocol() + "//" + config.getHostname() + ":" + getServerPort();
      String urlRoot = config.getUrlRoot();
      if (!withUrlRoot || "/".equals(urlRoot)) {
        return baseUrl + path;
      }
      return baseUrl + config.getUrlRoot() + path;
    }
    LOG.error("Karma config not ready");
    return "http://localhost:" + getServerPort() + path;
  }

  @NotNull
  public ProcessHandler getProcessHandler() {
    return myProcessOutputManager.getProcessHandler();
  }

  private class MyDisposable implements Disposable {

    private final Runnable myRunnable = ConcurrencyUtil.once(() -> {
      LOG.info("Disposing Karma server " + myProcessHashCode);
      if (myCoveragePeer != null) {
        FileUtil.asyncDelete(myCoveragePeer.getCoverageTempDir());
      }
      UIUtil.invokeLaterIfNeeded(() -> {
        if (myOnPortBoundCallbacks != null) {
          myOnPortBoundCallbacks.clear();
        }
        if (myOnBrowsersReadyCallbacks != null) {
          myOnBrowsersReadyCallbacks.clear();
        }
      });
      shutdown();
    });

    @Override
    public void dispose() {
      myRunnable.run();
    }
  }
}
