package com.intellij.javascript.karma.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.karma.util.EventEmitterProcess;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sergey Simonchik
 */
public class KarmaServer {

  private static final Pattern WEB_SERVER_URL_PATTERN = Pattern.compile("^http://([^:]+):(\\d+)(/.*)$");
  private static final Logger LOG = Logger.getInstance(KarmaServer.class);

  private final List<KarmaServerListener> myListeners = ContainerUtil.createEmptyCOWList();
  private final File myConfigurationFile;
  private final KillableColoredProcessHandler myProcessHandler;
  private final File myKarmaIntellijDir;
  private volatile int myWebServerPort = -1;
  private volatile int myRunnerPort = -1;
  private final AtomicBoolean myIsReady = new AtomicBoolean(false);
  private boolean myOnReadyFired = false;

  public KarmaServer(@NotNull File nodeInterpreter,
                     @NotNull File karmaPackageDir,
                     @NotNull File configurationFile) throws IOException {
    /* 'nodeInterpreter', 'karmaPackageDir' and 'configurationFile'
        are already checked in KarmaRunConfiguration.checkConfiguration */
    myConfigurationFile = configurationFile;
    myKarmaIntellijDir = findKarmaIntellijDir(karmaPackageDir);
    myProcessHandler = startServer(nodeInterpreter, configurationFile);
    Disposer.register(ApplicationManager.getApplication(), new Disposable() {
      @Override
      public void dispose() {
        myProcessHandler.destroyProcess();
      }
    });
  }

  private static File findKarmaIntellijDir(@NotNull File karmaPackageDir) throws IOException {
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
                                                    @NotNull File configurationFile) throws IOException {
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setPassParentEnvironment(true);
    commandLine.setWorkDirectory(configurationFile.getParentFile());
    commandLine.setExePath(nodeInterpreter.getAbsolutePath());
    File serverFile = getServerAppFile();
    commandLine.addParameter(serverFile.getAbsolutePath());
    commandLine.addParameter("--configFile=" + configurationFile.getAbsolutePath());

    try {
      LOG.info("Starting karma server: " + commandLine.getCommandLineString());
      Process originalProcess = commandLine.createProcess();
      Process eventEmitterProcess = new EventEmitterProcess(originalProcess);
      KillableColoredProcessHandler processHandler = new KillableColoredProcessHandler(
        eventEmitterProcess,
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
      processHandler.startNotify();
      processHandler.setShouldDestroyProcessRecursively(true);
      ProcessTerminatedListener.attach(processHandler);
      return processHandler;
    }
    catch (ExecutionException e) {
      throw new IOException("Can not create karma server process", e);
    }
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
    File file = new File(myKarmaIntellijDir, "lib" + File.separatorChar + baseName);
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
    if (myWebServerPort != -1 && myRunnerPort != -1) {
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
    String webServerPrefix = "INFO [karma]: Karma v0.9.2 server started at ";
    if (text.startsWith(webServerPrefix)) {
      Matcher m = WEB_SERVER_URL_PATTERN.matcher(text.substring(webServerPrefix.length()));
      if (m.find()) {
        String portStr = m.group(2);
        try {
          return Integer.parseInt(portStr);
        }
        catch (NumberFormatException e) {
          LOG.info("Can't parse web server port from '" + text + "'");
        }
      }
    }
    return -1;
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

  @NotNull
  public KillableColoredProcessHandler getProcessHandler() {
    return myProcessHandler;
  }
}
