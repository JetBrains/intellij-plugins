package com.intellij.javascript.karma.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.karma.util.ProcessEventStore;
import com.intellij.javascript.karma.util.StreamEventListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.SemVer;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaServer {

  private static final Pattern WEB_SERVER_LINE_PATTERN = Pattern.compile("^INFO \\[.*\\]: Karma v(.+) server started at http://[^:]+:(\\d+)/.*$");
  private static final Pattern BROWSER_CONNECTED_LINE_PATTERN = Pattern.compile("^INFO \\[.*\\]: Connected on socket id \\S*$");
  private static final Logger LOG = Logger.getInstance(KarmaServer.class);

  private final List<KarmaServerListener> myListeners = ContainerUtil.createEmptyCOWList();
  private final File myConfigurationFile;
  private final KillableColoredProcessHandler myProcessHandler;
  private final ProcessEventStore myProcessEventStore;
  private final File myKarmaIntellijPackageDir;
  private volatile int myWebServerPort = -1;
  private volatile int myRunnerPort = -1;
  private volatile boolean myBrowserConnected = false;
  private final AtomicBoolean myIsReady = new AtomicBoolean(false);
  private boolean myOnReadyFired = false;

  public KarmaServer(@NotNull File nodeInterpreter,
                     @NotNull File karmaPackageDir,
                     @NotNull File configurationFile) throws IOException {
    /* 'nodeInterpreter', 'karmaPackageDir' and 'configurationFile'
        are already checked in KarmaRunConfiguration.checkConfiguration */
    myConfigurationFile = configurationFile;
    myKarmaIntellijPackageDir = findKarmaIntellijPackageDir(karmaPackageDir);
    try {
      long start = System.nanoTime();
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException e) {

      }
      myProcessHandler = startServer(nodeInterpreter, configurationFile);
      System.out.printf("Starting server takes: %.2f ms\n", (System.nanoTime() - start) / 1000000.0);
    }
    catch (ExecutionException e) {
      throw new IOException("Can not create karma server process", e);
    }
    myProcessEventStore = new ProcessEventStore(myProcessHandler);
    myProcessEventStore.addStreamEventListener(new StreamEventListener() {
      @Override
      public void on(@NotNull String eventText) {
        System.out.println("Got " + eventText);
      }
    });
    myProcessEventStore.startNotify();
    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      @Override
      public void dispose() {
        ScriptRunnerUtil.terminateProcess(myProcessHandler, 500, null);
      }
    });
  }

  /**
   * 'karma-intellij' directory contains installed node module 'karma-intellij'
   * Source code: https://github.com/karma-runner/karma-intellij
   *
   * @param karmaPackageDir 'karma' package directory
   * @return 'karma-intellij' directory
   * @throws IOException in case of any I/O troubles
   */
  private static File findKarmaIntellijPackageDir(@NotNull File karmaPackageDir) throws IOException {
    File dir = new File(karmaPackageDir.getParentFile(), "karma-intellij");
    if (!dir.isDirectory()) {
      throw new IOException("Can't find " + dir.getAbsolutePath());
    }
    return dir;
  }

  @NotNull
  public File getConfigurationFile() {
    return myConfigurationFile;
  }

  private KillableColoredProcessHandler startServer(@NotNull File nodeInterpreter,
                                                    @NotNull File configurationFile) throws IOException, ExecutionException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setPassParentEnvironment(true);
    commandLine.setWorkDirectory(configurationFile.getParentFile());
    commandLine.setExePath(nodeInterpreter.getAbsolutePath());
    File serverFile = getServerAppFile();
    commandLine.addParameter(serverFile.getAbsolutePath());
    commandLine.addParameter("--configFile=" + configurationFile.getAbsolutePath());

    LOG.info("Starting karma server: " + commandLine.getCommandLineString());
    final Process process = commandLine.createProcess();
    KillableColoredProcessHandler processHandler = new KillableColoredProcessHandler(
      process,
      commandLine.getCommandLineString(),
      CharsetToolkit.UTF8_CHARSET
    );

    processHandler.addProcessListener(new ProcessAdapter() {
      @Override
      public void onTextAvailable(ProcessEvent event, Key outputType) {
        String text = event.getText().trim();
        if (text != null && outputType == ProcessOutputTypes.STDOUT) {
          handleStdout(text);
        }
      }

      @Override
      public void processTerminated(ProcessEvent event) {
        KarmaServerRegistry.serverTerminated(KarmaServer.this);
      }
    });
    ProcessTerminatedListener.attach(processHandler);
    processHandler.setShouldDestroyProcessRecursively(true);
    return processHandler;
  }

  @NotNull
  public ProcessEventStore getProcessEventStore() {
    return myProcessEventStore;
  }

  @NotNull
  private File getServerAppFile() throws IOException {
    return getAppFile("intellijServer.js");
  }

  @NotNull
  public File getClientAppFile() throws IOException {
    return getAppFile("intellijRunner.js");
  }

  private File getAppFile(@NotNull String baseName) throws IOException {
    File file = new File(myKarmaIntellijPackageDir, "lib" + File.separatorChar + baseName);
    if (!file.isFile()) {
      throw new IOException("Can't find " + file);
    }
    return file;
  }

  private void handleStdout(@NotNull String text) {
    if (myWebServerPort == -1) {
      myWebServerPort = parseWebServerPort(text);
    }
    if (myRunnerPort == -1) {
      myRunnerPort = parseRunnerPort(text);
    }
    if (!myBrowserConnected) {
      myBrowserConnected = parseBrowserConnected(text);
    }
    if (myWebServerPort != -1 && myRunnerPort != -1 && myBrowserConnected) {
      fireOnReady(myWebServerPort, myRunnerPort);
    }
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
        LOG.info("Can't parse runner port from '" + text + "'");
      }
    }
    return -1;
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
        LOG.info("Can't parse web server port from '" + text + "'");
      }
    }
    return -1;
  }

  private static boolean parseBrowserConnected(@NotNull String text) {
    Matcher m = BROWSER_CONNECTED_LINE_PATTERN.matcher(text);
    return m.matches();
  }

  private void fireOnReady(final int webServerPort, final int runnerPort) {
    if (myIsReady.compareAndSet(false, true)) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          for (KarmaServerListener listener : myListeners) {
            listener.onReady(webServerPort, runnerPort);
          }
          myOnReadyFired = true;
        }
      });
    }
  }

  public void addListener(@NotNull KarmaServerListener listener) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    myListeners.add(listener);
    if (myOnReadyFired) {
      listener.onReady(myWebServerPort, myRunnerPort);
    }
  }

  public boolean isReady() {
    return myIsReady.get();
  }

  public int getRunnerPort() {
    return myRunnerPort;
  }

}
