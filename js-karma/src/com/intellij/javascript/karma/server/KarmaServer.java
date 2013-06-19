package com.intellij.javascript.karma.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.coverage.KarmaCoverageSession;
import com.intellij.javascript.karma.util.ProcessEventStore;
import com.intellij.javascript.karma.util.StreamEventListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.ConcurrentHashMap;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaServer {

  private static final Logger LOG = Logger.getInstance(KarmaServer.class);

  private final List<KarmaServerListener> myListeners = ContainerUtil.createEmptyCOWList();
  private final File myConfigurationFile;
  private final KillableColoredProcessHandler myProcessHandler;
  private final ProcessEventStore myProcessEventStore;
  private final KarmaJsSourcesLocator myKarmaJsSourcesLocator;
  private final KarmaServerState myState;

  private final AtomicBoolean myOnReadyFired = new AtomicBoolean(false);
  private boolean myOnReadyExecuted = false;
  private volatile KarmaCoverageSession myActiveCoverageSession;
  private final File myCoverageTempDir;

  // accessed in EDT only
  private final List<Runnable> myDoListWhenReadyWithCapturedBrowser = new CopyOnWriteArrayList<Runnable>();

  private volatile KarmaConfig myKarmaConfig;

  public KarmaServer(@NotNull File nodeInterpreter,
                     @NotNull File karmaPackageDir,
                     @NotNull File configurationFile) throws IOException {
    myCoverageTempDir = FileUtil.createTempDirectory("karma-intellij-coverage-", null);
    /* 'nodeInterpreter', 'karmaPackageDir' and 'configurationFile'
        are already checked in KarmaRunConfiguration.checkConfiguration */
    myConfigurationFile = configurationFile;
    myKarmaJsSourcesLocator = new KarmaJsSourcesLocator(karmaPackageDir);
    myState = new KarmaServerState(this);
    try {
      myProcessHandler = startServer(nodeInterpreter, configurationFile);
    }
    catch (ExecutionException e) {
      throw new IOException("Can not create karma server process", e);
    }
    myProcessEventStore = new ProcessEventStore(myProcessHandler);
    myProcessEventStore.addStreamEventListener(new StreamEventListener() {
      @Override
      public void on(@NotNull String eventType, @NotNull String eventBody) {
        if ("basePath".equals(eventType)) {
          myKarmaConfig = new KarmaConfig(eventBody, Collections.<String>emptySet());
        }
        else if ("coverage-finished".equals(eventType)) {
          KarmaCoverageSession coverageSession = myActiveCoverageSession;
          myActiveCoverageSession = null;
          if (coverageSession != null && coverageSession.getCoverageFilePath().equals(eventBody)) {
            coverageSession.onCoverageSessionFinished();
          }
        }
      }
    });
    myProcessEventStore.startNotify();
    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      @Override
      public void dispose() {
        if (!myProcessHandler.isProcessTerminated()) {
          ScriptRunnerUtil.terminateProcessHandler(myProcessHandler, 500, null);
        }
      }
    });
  }

  @NotNull
  public File getConfigurationFile() {
    return myConfigurationFile;
  }

  @NotNull
  public KarmaJsSourcesLocator getKarmaJsSourcesLocator() {
    return myKarmaJsSourcesLocator;
  }

  private KillableColoredProcessHandler startServer(@NotNull File nodeInterpreter,
                                                    @NotNull File configurationFile) throws IOException, ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setPassParentEnvironment(true);
    commandLine.setWorkDirectory(configurationFile.getParentFile());
    commandLine.setExePath(nodeInterpreter.getAbsolutePath());
    File serverFile = myKarmaJsSourcesLocator.getServerAppFile();
    commandLine.addParameter(serverFile.getAbsolutePath());
    commandLine.addParameter("--karmaPackageDir=" + myKarmaJsSourcesLocator.getKarmaPackageDir().getAbsolutePath());
    commandLine.addParameter("--configFile=" + configurationFile.getAbsolutePath());
    commandLine.addParameter("--coverageTempDir=" + myCoverageTempDir.getAbsolutePath());

    LOG.info("Starting karma server: " + commandLine.getCommandLineString());
    final Process process = commandLine.createProcess();
    KillableColoredProcessHandler processHandler = new KillableColoredProcessHandler(
      process,
      commandLine.getCommandLineString(),
      CharsetToolkit.UTF8_CHARSET
    );

    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        KarmaServerRegistry.serverTerminated(KarmaServer.this);
        fireOnTerminated(event.getExitCode());
      }
    });
    processHandler.addProcessListener(myState);
    ProcessTerminatedListener.attach(processHandler);
    processHandler.setShouldDestroyProcessRecursively(true);
    return processHandler;
  }

  @NotNull
  public ProcessEventStore getProcessEventStore() {
    return myProcessEventStore;
  }

  void fireOnReady(final int webServerPort, final int runnerPort) {
    if (myOnReadyFired.compareAndSet(false, true)) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          for (KarmaServerListener listener : myListeners) {
            listener.onReady(webServerPort, runnerPort);
          }
          myOnReadyExecuted = true;
          processWhenReadyWithCapturedBrowserQueue();
        }
      });
    }
  }

  private void fireOnTerminated(final int exitCode) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        for (KarmaServerListener listener : myListeners) {
          listener.onTerminated(exitCode);
        }
      }
    });
  }

  /**
   * @param task will be called in EDT
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

  public void addListener(@NotNull KarmaServerListener listener) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    myListeners.add(listener);
    if (myOnReadyExecuted) {
      listener.onReady(myState.myWebServerPort, myState.myRunnerPort);
    }
  }

  public boolean isReady() {
    return myOnReadyFired.get();
  }

  public int getRunnerPort() {
    return myState.myRunnerPort;
  }

  @Nullable
  public KarmaConfig getKarmaConfig() {
    return myKarmaConfig;
  }

  public int getWebServerPort() {
    return myState.myWebServerPort;
  }

  public boolean hasCapturedBrowsers() {
    return myState.hasCapturedBrowser();
  }

  private void fireOnBrowserConnected(@NotNull final String browserName) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        for (KarmaServerListener listener : myListeners) {
          listener.onBrowserConnected(browserName);
        }
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

  private void fireOnBrowserDisconnected(@NotNull final String browserName) {
    UIUtil.invokeLaterIfNeeded(new Runnable() {
      @Override
      public void run() {
        for (KarmaServerListener listener : myListeners) {
          listener.onBrowserDisconnected(browserName);
        }
      }
    });
  }

  @NotNull
  public Set<String> getCapturedBrowsers() {
    return myState.myCapturedBrowsers.keySet();
  }

  public void startCoverageSession(@NotNull KarmaCoverageSession coverageSession) {
    // clear directory
    if (myCoverageTempDir.isDirectory()) {
      File[] children = ObjectUtils.notNull(myCoverageTempDir.listFiles(), ArrayUtil.EMPTY_FILE_ARRAY);
      for (File child : children) {
        FileUtil.delete(child);
      }
    }
    else {
      FileUtil.createDirectory(myCoverageTempDir);
    }
    myActiveCoverageSession = coverageSession;
    @SuppressWarnings("IOResourceOpenedButNotSafelyClosed")
    PrintWriter pw = new PrintWriter(myProcessHandler.getProcessInput(), false);
    pw.print("write coverage to " + coverageSession.getCoverageFilePath() + "\n");
    pw.flush();
  }

  public static class KarmaServerState implements ProcessListener {

    private static final Pattern WEB_SERVER_LINE_PATTERN = Pattern.compile("^INFO \\[.*\\]: Karma v(.+) server started at http://[^:]+:(\\d+)/.*$");
    private static final Pattern BROWSER_CONNECTED_LINE_PATTERN = Pattern.compile(
      "^INFO \\[([^\\]]*)]: Connected on socket id \\S*$"
    );
    private static final Pattern BROWSER_DISCONNECTED_LINE_PATTERN = Pattern.compile(
      "^WARN \\[([^\\]]*)]: Disconnected$"
    );
    private static final Logger LOG = Logger.getInstance(KarmaServerState.class);

    private final KarmaServer myKarmaServer;

    private final ConcurrentMap<String, Integer> myCapturedBrowsers = new ConcurrentHashMap<String, Integer>();
    private final StringBuilder myBuffer = new StringBuilder();
    private volatile int myWebServerPort = -1;
    private volatile int myRunnerPort = -1;

    public KarmaServerState(@NotNull KarmaServer karmaServer) {
      myKarmaServer = karmaServer;
    }

    @Override
    public void startNotified(ProcessEvent event) {
    }

    @Override
    public void processTerminated(ProcessEvent event) {
    }

    @Override
    public void processWillTerminate(ProcessEvent event, boolean willBeDestroyed) {
    }

    @Override
    public void onTextAvailable(ProcessEvent event, Key outputType) {
      if (outputType != ProcessOutputTypes.SYSTEM && outputType != ProcessOutputTypes.STDOUT) {
        myBuffer.append(event.getText());
        int startInd = 0;
        for (int i = 0; i < myBuffer.length(); i++) {
          if (myBuffer.charAt(i) == '\n') {
            String line = myBuffer.substring(startInd, i);
            handleStdout(line);
            startInd = i + 1;
          }
        }
        myBuffer.delete(0, startInd);
      }
    }

    private void handleStdout(@NotNull String text) {
      if (myWebServerPort == -1) {
        myWebServerPort = parseWebServerPort(text);
      }
      if (myRunnerPort == -1) {
        myRunnerPort = parseRunnerPort(text);
      }
      if (myWebServerPort != -1 && myRunnerPort != -1) {
        myKarmaServer.fireOnReady(myWebServerPort, myRunnerPort);
      }
      handleBrowserEvents(text);
    }

    private static int parseWebServerPort(@NotNull String text) {
      Matcher m = WEB_SERVER_LINE_PATTERN.matcher(text);
      if (m.find()) {
        String karmaVersionStr = m.group(1);
        SemVer semVer = SemVer.parseFromText(karmaVersionStr);
        if (semVer == null) {
          LOG.warn("Can't parse sem ver from '" + karmaVersionStr + "'");
          return -1;
        }
        String portStr = m.group(2);
        try {
          return Integer.parseInt(portStr);
        }
        catch (NumberFormatException e) {
          LOG.warn("Can't parse web server port from '" + text + "'");
        }
      }
      return -1;
    }

    private static int parseRunnerPort(@NotNull String text) {
      String prefix = "INFO [karma]: To run via this server, use \"karma run --runner-port ";
      String suffix = "\"";
      if (text.startsWith(prefix) && text.endsWith(suffix)) {
        String str = text.substring(prefix.length(), text.length() - suffix.length());
        try {
          return Integer.parseInt(str);
        }
        catch (NumberFormatException e) {
          LOG.warn("Can't parse runner port from '" + text + "'");
        }
      }
      return -1;
    }

    private void handleBrowserEvents(@NotNull String text) {
      Matcher matcherConnected = BROWSER_CONNECTED_LINE_PATTERN.matcher(text);
      if (matcherConnected.find()) {
        String browserName = matcherConnected.group(1);
        Integer cnt = myCapturedBrowsers.get(browserName);
        if (cnt == null) {
          cnt = 0;
        }
        myCapturedBrowsers.put(browserName, cnt + 1);
        myKarmaServer.fireOnBrowserConnected(browserName);
      }
      else {
        Matcher matcherDisconnected = BROWSER_DISCONNECTED_LINE_PATTERN.matcher(text);
        if (matcherDisconnected.find()) {
          String browserName = matcherDisconnected.group(1);
          Integer cnt = myCapturedBrowsers.get(browserName);
          if (cnt == null) {
            LOG.warn("Too many disconnections for '" + browserName + "'");
          }
          else {
            cnt--;
            if (cnt == 0) {
              myCapturedBrowsers.remove(browserName);
            }
            else {
              myCapturedBrowsers.put(browserName, cnt);
            }
            myKarmaServer.fireOnBrowserDisconnected(browserName);
          }
        }
      }
    }

    public boolean hasCapturedBrowser() {
      return !myCapturedBrowsers.isEmpty();
    }
  }

}
