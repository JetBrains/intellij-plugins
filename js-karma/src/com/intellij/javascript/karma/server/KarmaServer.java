package com.intellij.javascript.karma.server;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.coverage.KarmaCoveragePeer;
import com.intellij.javascript.karma.util.ProcessOutputArchive;
import com.intellij.javascript.karma.util.StreamEventListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Sergey Simonchik
 */
public class KarmaServer {

  private static final Logger LOG = Logger.getInstance(KarmaServer.class);

  private final ProcessOutputArchive myProcessOutputArchive;
  private final KarmaJsSourcesLocator myKarmaJsSourcesLocator;
  private final KarmaServerState myState;
  private final KarmaCoveragePeer myCoveragePeer = new KarmaCoveragePeer();
  private final KarmaWatcher myWatcher;

  private final AtomicBoolean myOnPortBoundFired = new AtomicBoolean(false);
  private final File myKarmaPackageDir;

  private List<Runnable> myOnPortBoundCallbacks = Lists.newCopyOnWriteArrayList();
  private List<Runnable> myOnBrowsersReadyCallbacks = Lists.newCopyOnWriteArrayList();

  private Integer myExitCode = null;
  private final List<KarmaServerTerminatedListener> myTerminationCallbacks = Lists.newCopyOnWriteArrayList();

  private final Map<String, StreamEventHandler> myHandlers = ContainerUtil.newConcurrentMap();
  private final MyDisposable myDisposable;
  private final KarmaServerRestarter myRestarter;

  public KarmaServer(@NotNull Project project,
                     @NotNull File nodeInterpreter,
                     @NotNull File karmaPackageDir,
                     @NotNull File configurationFile) throws IOException {
    /* 'nodeInterpreter', 'karmaPackageDir' and 'configurationFile'
        are already checked in KarmaRunConfiguration.checkConfiguration */
    myKarmaPackageDir = karmaPackageDir;
    myKarmaJsSourcesLocator = new KarmaJsSourcesLocator(karmaPackageDir);
    final ProcessHandler processHandler = startServer(nodeInterpreter, configurationFile);
    myState = new KarmaServerState(this, processHandler, configurationFile);
    myProcessOutputArchive = new ProcessOutputArchive(processHandler);
    myWatcher = new KarmaWatcher(this);
    registerStreamEventHandlers();
    myProcessOutputArchive.startNotify();

    myDisposable = new MyDisposable();
    Disposer.register(project, myDisposable);
    myRestarter = new KarmaServerRestarter(configurationFile, myDisposable);
  }

  private void registerStreamEventHandlers() {
    myCoveragePeer.registerEventHandlers(this);

    registerStreamEventHandler(myWatcher.getEventHandler());

    myProcessOutputArchive.addStreamEventListener(new StreamEventListener() {
      @Override
      public void on(@NotNull String eventType, @NotNull String eventBody) {
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
  public KarmaServerRestarter getRestarter() {
    return myRestarter;
  }

  @NotNull
  public File getKarmaPackageDir() {
    return myKarmaPackageDir;
  }

  @NotNull
  public KarmaCoveragePeer getCoveragePeer() {
    return myCoveragePeer;
  }

  @NotNull
  public KarmaWatcher getWatcher() {
    return myWatcher;
  }

  @NotNull
  public KarmaJsSourcesLocator getKarmaJsSourcesLocator() {
    return myKarmaJsSourcesLocator;
  }

  public void registerStreamEventHandler(@NotNull StreamEventHandler handler) {
    myHandlers.put(handler.getEventType(), handler);
  }

  private KillableColoredProcessHandler startServer(@NotNull File nodeInterpreter,
                                                    @NotNull File configurationFile) throws IOException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setPassParentEnvironment(true);
    commandLine.setWorkDirectory(configurationFile.getParentFile());
    commandLine.setExePath(nodeInterpreter.getAbsolutePath());
    File serverFile = myKarmaJsSourcesLocator.getServerAppFile();
    //commandLine.addParameter("--debug=34598");
    commandLine.addParameter(serverFile.getAbsolutePath());
    commandLine.addParameter("--karmaPackageDir=" + myKarmaJsSourcesLocator.getKarmaPackageDir().getAbsolutePath());
    commandLine.addParameter("--configFile=" + configurationFile.getAbsolutePath());
    commandLine.addParameter("--coverageTempDir=" + myCoveragePeer.getCoverageTempDir());

    LOG.info("Starting karma server: " + commandLine.getCommandLineString());
    final Process process;
    try {
      process = commandLine.createProcess();
    }
    catch (ExecutionException e) {
      throw new IOException("Can not launch process " + commandLine.getCommandLineString() + "\"", e);
    }
    KillableColoredProcessHandler processHandler = new KillableColoredProcessHandler(
      process,
      commandLine.getCommandLineString(),
      CharsetToolkit.UTF8_CHARSET
    );

    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(final ProcessEvent event) {
        Disposer.dispose(myDisposable);
        fireOnTerminated(event.getExitCode());
      }
    });
    ProcessTerminatedListener.attach(processHandler);
    processHandler.setShouldDestroyProcessRecursively(true);
    return processHandler;
  }

  public void shutdownAsync() {
    ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
      @Override
      public void run() {
        shutdown();
      }
    });
  }

  private void shutdown() {
    ProcessHandler processHandler = myProcessOutputArchive.getProcessHandler();
    if (!processHandler.isProcessTerminated()) {
      ScriptRunnerUtil.terminateProcessHandler(processHandler, 500, null);
    }
  }

  @NotNull
  public ProcessOutputArchive getProcessOutputArchive() {
    return myProcessOutputArchive;
  }

  public boolean isPortBound() {
    return myOnPortBoundFired.get();
  }

  public int getServerPort() {
    return myState.getServerPort();
  }

  /**
   * Executes {@code callback} in EDT when the server port is bound.
   */
  public void onPortBound(@NotNull final Runnable callback) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myOnPortBoundCallbacks != null) {
          myOnPortBoundCallbacks.add(callback);
        }
        else {
          callback.run();
        }
      }
    });
  }

  void fireOnPortBound() {
    if (myOnPortBoundFired.compareAndSet(false, true)) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          List<Runnable> callbacks = ContainerUtil.newArrayList(myOnPortBoundCallbacks);
          myOnPortBoundCallbacks.clear();
          myOnPortBoundCallbacks = null;
          for (Runnable callback : callbacks) {
            callback.run();
          }
        }
      });
    }
  }

  public boolean areBrowsersReady() {
    return myState.areBrowsersReady();
  }

  @NotNull
  public Collection<CapturedBrowser> getCapturedBrowsers() {
    return myState.getCapturedBrowsers();
  }

  /**
   * Executes {@code callback} in EDT when at least one browser is captured and all config.browsers are captured.
   */
  public void onBrowsersReady(@NotNull final Runnable callback) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myOnBrowsersReadyCallbacks != null) {
          myOnBrowsersReadyCallbacks.add(callback);
        }
        else {
          callback.run();
        }
      }
    });
  }

  void fireOnBrowsersReady() {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        List<Runnable> callbacks = ContainerUtil.newArrayList(myOnBrowsersReadyCallbacks);
        myOnBrowsersReadyCallbacks.clear();
        myOnBrowsersReadyCallbacks = null;
        for (Runnable callback : callbacks) {
          callback.run();
        }
      }
    });
  }

  /**
   * Executes {@code terminationCallback} in EDT when the server is shut down.
   */
  public void onTerminated(@NotNull final KarmaServerTerminatedListener terminationCallback) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myExitCode != null) {
          terminationCallback.onTerminated(myExitCode);
        }
        else {
          myTerminationCallbacks.add(terminationCallback);
        }
      }
    });
  }

  private void fireOnTerminated(final int exitCode) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        myExitCode = exitCode;
        List<KarmaServerTerminatedListener> listeners = ContainerUtil.newArrayList(myTerminationCallbacks);
        myTerminationCallbacks.clear();
        for (KarmaServerTerminatedListener listener : listeners) {
          listener.onTerminated(exitCode);
        }
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
      String baseUrl = "http://" + config.getHostname() + ":" + getServerPort();
      String urlRoot = config.getUrlRoot();
      if (!withUrlRoot || "/".equals(urlRoot)) {
        return baseUrl + path;
      }
      return baseUrl + config.getUrlRoot() + path;
    }
    LOG.error("Karma config isn't ready yet.");
    return "http://localhost:" + getServerPort() + path;
  }

  private class MyDisposable implements Disposable {

    @Override
    public void dispose() {
      myWatcher.stop();
      FileUtil.asyncDelete(myCoveragePeer.getCoverageTempDir());
      if (myOnPortBoundCallbacks != null) {
        myOnPortBoundCallbacks.clear();
      }
      if (myOnBrowsersReadyCallbacks != null) {
        myOnBrowsersReadyCallbacks.clear();
      }
      shutdown();
    }
  }

}
