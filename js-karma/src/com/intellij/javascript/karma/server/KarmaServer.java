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

  private final AtomicBoolean myOnReadyFired = new AtomicBoolean(false);
  private boolean myOnReadyExecuted = false;
  private Integer myExitCode = null;
  private final List<KarmaServerReadyListener> myDoListWhenReady = Lists.newCopyOnWriteArrayList();
  private final List<KarmaServerTerminatedListener> myDoListWhenTerminated = Lists.newCopyOnWriteArrayList();
  private final List<Runnable> myDoListWhenReadyWithCapturedBrowser = Lists.newCopyOnWriteArrayList();

  private final Map<String, StreamEventHandler> myHandlers = ContainerUtil.newConcurrentMap();

  public KarmaServer(@NotNull File nodeInterpreter,
                     @NotNull File karmaPackageDir,
                     @NotNull File configurationFile) throws IOException {
    /* 'nodeInterpreter', 'karmaPackageDir' and 'configurationFile'
        are already checked in KarmaRunConfiguration.checkConfiguration */
    myKarmaJsSourcesLocator = new KarmaJsSourcesLocator(karmaPackageDir);
    final ProcessHandler processHandler = startServer(nodeInterpreter, configurationFile);
    myState = new KarmaServerState(this, processHandler);
    myProcessOutputArchive = new ProcessOutputArchive(processHandler);
    myWatcher = new KarmaWatcher(this);
    registerStreamEventHandlers();
    myProcessOutputArchive.startNotify();

    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      @Override
      public void dispose() {
        if (!processHandler.isProcessTerminated()) {
          ScriptRunnerUtil.terminateProcessHandler(processHandler, 500, null);
        }
      }
    });
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
        FileUtil.asyncDelete(myCoveragePeer.getCoverageTempDir());
        UIUtil.invokeLaterIfNeeded(new Runnable() {
          @Override
          public void run() {
            myDoListWhenReady.clear();
            myDoListWhenReadyWithCapturedBrowser.clear();
            fireOnTerminated(event.getExitCode());
          }
        });
      }
    });
    ProcessTerminatedListener.attach(processHandler);
    processHandler.setShouldDestroyProcessRecursively(true);
    return processHandler;
  }

  @NotNull
  public ProcessOutputArchive getProcessOutputArchive() {
    return myProcessOutputArchive;
  }

  void fireOnReady(final int webServerPort) {
    if (myOnReadyFired.compareAndSet(false, true)) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          myOnReadyExecuted = true;
          List<KarmaServerReadyListener> listeners = ContainerUtil.newArrayList(myDoListWhenReady);
          myDoListWhenReady.clear();
          for (KarmaServerReadyListener listener : listeners) {
            listener.onReady(webServerPort);
          }
          processWhenReadyWithCapturedBrowserQueue();
        }
      });
    }
  }

  private void fireOnTerminated(final int exitCode) {
    myExitCode = exitCode;
    for (KarmaServerTerminatedListener listener : myDoListWhenTerminated) {
      listener.onTerminated(exitCode);
    }
  }

  /**
   * Executes {@code} task in EDT when the server is ready
   */
  public void doWhenReady(@NotNull final KarmaServerReadyListener readyCallback) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myOnReadyExecuted) {
          readyCallback.onReady(myState.getServerPort());
        }
        else {
          myDoListWhenReady.add(readyCallback);
        }
      }
    });
  }

  /**
   * Executes {@code} task in EDT when the server is ready
   */
  public void doWhenTerminated(@NotNull final KarmaServerTerminatedListener terminationCallback) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myExitCode != null) {
          terminationCallback.onTerminated(myExitCode);
        }
        else {
          myDoListWhenTerminated.add(terminationCallback);
        }
      }
    });
  }

  /**
   * Executes {@code} task in EDT when the server is ready and has at least one captured browser.
   */
  public void doWhenReadyWithCapturedBrowser(@NotNull final Runnable task) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        if (myOnReadyFired.get() && hasCapturedBrowsers()) {
          task.run();
        }
        else {
          myDoListWhenReadyWithCapturedBrowser.add(task);
        }
      }
    });
  }

  public boolean isReady() {
    return myOnReadyFired.get();
  }

  @Nullable
  public KarmaConfig getKarmaConfig() {
    return myState.getKarmaConfig();
  }

  @NotNull
  public String formatUrlWithoutUrlRoot(@NotNull String path) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    KarmaConfig config = myState.getKarmaConfig();
    if (config != null) {
      return "http://" + config.getHostname() + ":" + getServerPort() + path;
    }
    LOG.error("Karma config isn't ready yet.");
    return "http://localhost:" + getServerPort() + path;
  }

  @NotNull
  public String formatUrl(@NotNull String path) {
    if (!path.startsWith("/")) {
      path = "/" + path;
    }
    KarmaConfig config = myState.getKarmaConfig();
    if (config != null) {
      return "http://" + config.getHostname() + ":" + getServerPort() + config.getUrlRoot() + path;
    }
    LOG.error("Karma config isn't ready yet.");
    return "http://localhost:" + getServerPort() + path;
  }

  public int getServerPort() {
    return myState.getServerPort();
  }

  public boolean hasCapturedBrowsers() {
    return myState.hasCapturedBrowser();
  }

  void onBrowserCaptured() {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        processWhenReadyWithCapturedBrowserQueue();
      }
    });
  }

  private void processWhenReadyWithCapturedBrowserQueue() {
    if (myDoListWhenReadyWithCapturedBrowser.isEmpty()) {
      return;
    }
    if (myOnReadyFired.get() && hasCapturedBrowsers()) {
      List<Runnable> tasks = ContainerUtil.newArrayList(myDoListWhenReadyWithCapturedBrowser);
      myDoListWhenReadyWithCapturedBrowser.clear();
      for (Runnable task : tasks) {
        task.run();
      }
    }
  }

  @NotNull
  public Collection<String> getCapturedBrowsers() {
    return myState.getCapturedBrowsers();
  }

}
