package com.intellij.javascript.karma.server;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.javascript.karma.execution.KarmaJavaScriptSourcesLocator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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
  private volatile boolean myTerminated = false;
  private volatile int myWebServerPort = -1;
  private volatile int myRunnerPort = -1;
  private AtomicBoolean myReadyStatus = new AtomicBoolean(false);

  public KarmaServer(@NotNull File nodeInterpreterPath,
                     @NotNull File karmaPackageDir,
                     @NotNull File configurationFile) throws IOException {
    myConfigurationFile = configurationFile;
    startServer(nodeInterpreterPath, karmaPackageDir, configurationFile);
  }

  private void startServer(@NotNull File nodeInterpreterPath,
                           @NotNull File karmaPackageDir,
                           @NotNull File configurationFile) throws IOException {
    if (!nodeInterpreterPath.isFile() || !nodeInterpreterPath.canExecute()) {
      throw new IOException("node interpreter isn't executable file");
    }
    if (!karmaPackageDir.isDirectory()) {
      throw new IOException("Karma directory is illegal");
    }
    if (!configurationFile.isFile()) {
      throw new IOException("Configuration file is illegal");
    }
    GeneralCommandLine commandLine = new GeneralCommandLine();
    commandLine.setPassParentEnvironment(true);
    commandLine.setWorkDirectory(configurationFile.getParentFile());
    commandLine.setExePath(nodeInterpreterPath.getAbsolutePath());
    File serverFile = KarmaJavaScriptSourcesLocator.getServerAppFile();
    commandLine.addParameter(serverFile.getAbsolutePath());
    commandLine.addParameter(karmaPackageDir.getAbsolutePath());
    commandLine.addParameter(configurationFile.getAbsolutePath());

    try {
      Process process = commandLine.createProcess();
      ProcessHandler processHandler = new KillableColoredProcessHandler(process, commandLine.getCommandLineString(), CharsetToolkit.UTF8_CHARSET);

      processHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void onTextAvailable(ProcessEvent event, Key outputType) {
          String text = event.getText().trim();
          if (text != null && outputType == ProcessOutputTypes.STDOUT) {
            if (myWebServerPort == -1) {
              String webServerPrefix = "INFO [karma]: Karma server started at ";
              if (text.startsWith(webServerPrefix)) {
                Matcher m = WEB_SERVER_URL_PATTERN.matcher(text.substring(webServerPrefix.length()));
                if (m.find()) {
                  String portStr = m.group(2);
                  try {
                    myWebServerPort = Integer.parseInt(portStr);
                    checkReadyStatus();
                  }
                  catch (NumberFormatException e) {
                    LOG.info("Can't parse web server port from '" + text + "'");
                  }
                }
              }
            }
            if (myRunnerPort == -1) {
              String prefix = "INFO [karma]: To run via this server, use \"karma run --runner-port ";
              String suffix = "\"";
              if (text.startsWith(prefix) && text.endsWith(suffix)) {
                String str = text.substring(prefix.length(), text.length() - suffix.length());
                try {
                  myRunnerPort = Integer.parseInt(str);
                  checkReadyStatus();
                }
                catch (NumberFormatException e) {
                  LOG.info("Can't parse runner port from '" + text + "'");
                }
              }
            }
          }
        }

        @Override
        public void processTerminated(ProcessEvent event) {
          myTerminated = true;
          KarmaServerRegistry.serverTerminated(KarmaServer.this);
        }
      });
      final Timer t = new Timer("Runner port", false);
      t.schedule(new TimerTask() {
        @Override
        public void run() {
          if (myRunnerPort == -1) {
            myRunnerPort = 9100;
            checkReadyStatus();
          }
          t.cancel();
        }
      }, 500);
    }
    catch (ExecutionException e) {
      throw new IOException("Can not create karma server process", e);
    }
  }

  private void checkReadyStatus() {
    if (myRunnerPort != -1 && myWebServerPort != -1) {
      if (myReadyStatus.compareAndSet(false, true)) {
        fireOnReady(myWebServerPort, myRunnerPort);
      }
    }
  }

  private void fireOnReady(int webServerPort, int runnerPort) {
    for (KarmaServerListener listener : myListeners) {
      listener.onReady(webServerPort, runnerPort);
    }
  }

  public boolean isTerminated() {
    return myTerminated;
  }

  @NotNull
  public File getConfigurationFile() {
    return myConfigurationFile;
  }

  public void addListener(@NotNull KarmaServerListener listener) {
    myListeners.add(listener);
  }
}
