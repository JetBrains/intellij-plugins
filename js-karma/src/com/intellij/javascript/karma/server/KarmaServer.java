// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.server;

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
import com.intellij.javascript.nodejs.library.yarn.pnp.YarnPnpNodePackage;
import com.intellij.javascript.nodejs.util.NodePackage;
import com.intellij.javascript.testing.AngularCliConfig;
import com.intellij.lang.javascript.ConsoleCommandLineFolder;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class KarmaServer {
  private static final Logger LOG = Logger.getInstance(KarmaServer.class);

  private final KarmaProcessOutputManager myProcessOutputManager;
  private final KarmaServerState myState;
  private final KarmaCoveragePeer myCoveragePeer;
  private final KarmaServerSettings myServerSettings;
  private final ConsoleCommandLineFolder myCommandLineFolder = new ConsoleCommandLineFolder();

  private List<Runnable> myOnPortBoundCallbacks = ContainerUtil.createLockFreeCopyOnWriteList();
  private List<Runnable> myOnBrowsersReadyCallbacks = ContainerUtil.createLockFreeCopyOnWriteList();

  private Integer myExitCode = null;
  private final List<KarmaServerTerminatedListener> myTerminationCallbacks = ContainerUtil.createLockFreeCopyOnWriteList();

  private final Map<String, StreamEventHandler> myHandlers = new ConcurrentHashMap<>();
  private final MyDisposable myDisposable;
  private final KarmaServerRestarter myRestarter;
  private final int myProcessHashCode;

  public KarmaServer(@NotNull Project project, @NotNull KarmaServerSettings serverSettings) throws IOException {
    myServerSettings = serverSettings;
    myCoveragePeer = serverSettings.isWithCoverage() ? new KarmaCoveragePeer() : null;
    KillableColoredProcessHandler processHandler = startServer(project, serverSettings, myCoveragePeer, myCommandLineFolder);
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
      public void processTerminated(final @NotNull ProcessEvent event) {
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
          jsonElement = JsonParser.parseString(eventBody);
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

  public @NotNull KarmaServerSettings getServerSettings() {
    return myServerSettings;
  }

  public @NotNull KarmaServerRestarter getRestarter() {
    return myRestarter;
  }

  public @Nullable KarmaCoveragePeer getCoveragePeer() {
    return myCoveragePeer;
  }

  public void registerStreamEventHandler(@NotNull StreamEventHandler handler) {
    myHandlers.put(handler.getEventType(), handler);
  }

  public @NotNull ConsoleCommandLineFolder getCommandLineFolder() {
    return myCommandLineFolder;
  }

  private static @NotNull KillableColoredProcessHandler startServer(@NotNull Project project,
                                                                    @NotNull KarmaServerSettings serverSettings,
                                                                    @Nullable KarmaCoveragePeer coveragePeer,
                                                                    @NotNull ConsoleCommandLineFolder commandLineFolder) throws IOException {
    GeneralCommandLine commandLine = ReadAction.compute(() -> {
      try {
        return createCommandLine(project, serverSettings, coveragePeer, commandLineFolder);
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

  private static @NotNull GeneralCommandLine createCommandLine(@NotNull Project project,
                                                               @NotNull KarmaServerSettings serverSettings,
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
      if (pkg instanceof YarnPnpNodePackage) {
        ((YarnPnpNodePackage)pkg).addYarnRunToCommandLine(commandLine, project, serverSettings.getNodeInterpreter(), null);
      }
      else {
        commandLine.addParameter(pkg.getSystemDependentPath() + File.separator + "bin" + File.separator + "ng");
      }
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
        commandLine.addParameters("--config", configurator.convertLocalPathToRemote(configPath));
        commandLineFolder.addPlaceholderText("--config=" + userConfigFileName);
      }
    }
    else {
      if (pkg instanceof YarnPnpNodePackage) {
        ((YarnPnpNodePackage)pkg).addYarnRunToCommandLine(commandLine, project, serverSettings.getNodeInterpreter(), null);
      }
      else {
        commandLine.addParameter(pkg.getSystemDependentPath() + File.separator + "bin" + File.separator + "karma");
      }
      commandLine.addParameter("start");
      commandLine.addParameter(KarmaJsSourcesLocator.getInstance().getIntellijConfigFile().getAbsolutePath());
      commandLineFolder.addPlaceholderTexts("karma", "start", userConfigFileName);
    }
    List<String> karmaOptions = ParametersListUtil.parse(serverSettings.getKarmaOptions());
    commandLine.addParameters(karmaOptions);
    commandLineFolder.addPlaceholderTexts(karmaOptions);
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
    commandLine.setCharset(StandardCharsets.UTF_8);
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

  public @NotNull KarmaProcessOutputManager getProcessOutputManager() {
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
  public void onPortBound(final @NotNull Runnable callback) {
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
      List<Runnable> callbacks = new ArrayList<>(myOnPortBoundCallbacks);
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
  public void onBrowsersReady(final @NotNull Runnable callback) {
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
        List<Runnable> callbacks = new ArrayList<>(myOnBrowsersReadyCallbacks);
        myOnBrowsersReadyCallbacks.clear();
        myOnBrowsersReadyCallbacks = null;
        for (Runnable callback : callbacks) {
          callback.run();
        }
      }
      else {
        myOnBrowsersReadyCallbacks = ContainerUtil.createLockFreeCopyOnWriteList();
      }
    });
  }

  /**
   * Executes {@code terminationCallback} in EDT when the server is shut down.
   */
  public void onTerminated(final @NotNull KarmaServerTerminatedListener terminationCallback) {
    UIUtil.invokeLaterIfNeeded(() -> {
      if (myExitCode != null) {
        terminationCallback.onTerminated(myExitCode);
      }
      else {
        myTerminationCallbacks.add(terminationCallback);
      }
    });
  }

  public void removeTerminatedListener(final @NotNull KarmaServerTerminatedListener listener) {
    UIUtil.invokeLaterIfNeeded(() -> myTerminationCallbacks.remove(listener));
  }

  private void fireOnTerminated(final int exitCode) {
    UIUtil.invokeLaterIfNeeded(() -> {
      myExitCode = exitCode;
      List<KarmaServerTerminatedListener> listeners = new ArrayList<>(myTerminationCallbacks);
      myTerminationCallbacks.clear();
      for (KarmaServerTerminatedListener listener : listeners) {
        listener.onTerminated(exitCode);
      }
    });
  }

  public @Nullable KarmaConfig getKarmaConfig() {
    return myState.getKarmaConfig();
  }

  public @NotNull String formatUrlWithoutUrlRoot(@NotNull String path) {
    return formatUrl(path, false);
  }

  @SuppressWarnings("SameParameterValue")
  public @NotNull String formatUrl(@NotNull String path) {
    return formatUrl(path, true);
  }

  private @NotNull String formatUrl(@NotNull String path, boolean withUrlRoot) {
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

  public @NotNull ProcessHandler getProcessHandler() {
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
      shutdownAsync();
    });

    @Override
    public void dispose() {
      myRunnable.run();
    }
  }
}
