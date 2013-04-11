package com.google.jstestdriver.idea.server.ui;

import com.google.common.collect.Sets;
import com.google.jstestdriver.*;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.config.ExecutionType;
import com.google.jstestdriver.hooks.FileInfoScheme;
import com.google.jstestdriver.hooks.FileLoadPostProcessor;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.html.HtmlDocLexer;
import com.google.jstestdriver.html.HtmlDocParser;
import com.google.jstestdriver.html.InlineHtmlProcessor;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.runner.RunnerMode;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.NullStopWatch;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.ide.actions.ShowLogAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.Pair;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.LogManager;

/**
 * @author Sergey Simonchik
 */
public class ServerStartAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(ServerStartAction.class);

  public static volatile ServerStartupAction ACTIVE_SERVER_STARTUP_ACTION = null;
  private static boolean myLocalServerStarting = false;

  public ServerStartAction() {
    super("Start a local server", null, AllIcons.Actions.Execute);
  }

  @Override
  public void update(AnActionEvent e) {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    boolean disabled = myLocalServerStarting || jstdServerState.isServerRunning();
    e.getPresentation().setEnabled(!disabled);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    asyncStartServer(e.getProject(), null);
  }

  /**
   * Called on EDT.
   */
  public static void asyncStartServer(@Nullable final Project project, @Nullable final Runnable callback) {
    if (!myLocalServerStarting) {
      myLocalServerStarting = true;
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          syncStartServer(project);
          if (callback != null) {
            callback.run();
          }
          UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
              //noinspection AssignmentToStaticFieldFromInstanceMethod
              myLocalServerStarting = false;
            }
          });
        }
      });
    }
  }

  private static void setJstdLoggerConfiguration(@NotNull RunnerMode runnerMode) {
    try {
      LogManager.getLogManager().readConfiguration(runnerMode.getLogConfig());
    }
    catch (IOException e) {
      LOG.info("Can't configure JsTestDriver server logger", e);
    }
  }

  private static void syncStartServer(@Nullable final Project project) {
    final JstdServerState jstdServerState = JstdServerState.getInstance();
    if (jstdServerState.isServerRunning()) {
      return;
    }
    setJstdLoggerConfiguration(RunnerMode.DEBUG);
    final ServerStartupError serverStartupError;
    try {
      Pair<ServerStartupError, StandardStreamsUtil.CapturedStreams> result = StandardStreamsUtil.captureStandardStreams(
        new NullableComputable<ServerStartupError>() {
          @Nullable
          @Override
          public ServerStartupError compute() {
            return doStartServer(jstdServerState);
          }
        }
      );
      serverStartupError = result.getFirst();
      StandardStreamsUtil.CapturedStreams streams = result.getSecond();
      LOG.info("JsTestDriver server startup log:\n"
               + "stdout:" + streams.getStdOut() + "\n"
               + "stderr:" + streams.getStdErr()
      );
    }
    finally {
      setJstdLoggerConfiguration(RunnerMode.INFO);
    }
    if (serverStartupError != null) {
      UIUtil.invokeLaterIfNeeded(new Runnable() {
        @Override
        public void run() {
          String title = "JsTestDriver Server Launching";
          boolean addShowLogAction = serverStartupError.isUnknownReason() && ShowFilePathAction.isSupported();
          List<String> options = ContainerUtil.newArrayList("Close");
          if (addShowLogAction) {
            options.add(0, ShowLogAction.getActionName());
          }
          int result = Messages.showDialog(project, serverStartupError.getMessage(), title,
                                           ArrayUtil.toStringArray(options), 0, Messages.getErrorIcon());
          if (addShowLogAction && result == 0) {
            final File logFile = new File(PathManager.getLogPath(), "idea.log");
            ShowFilePathAction.openFile(logFile);
          }
        }
      });
    }
  }

  @Nullable
  private static ServerStartupError doStartServer(@NotNull JstdServerState jstdServerState) {
    int serverPort = JstdToolWindowPanel.serverPort;
    FileLoader fileLoader = new ProcessingFileLoader(
      new SimpleFileReader(),
      Collections.<FileLoadPostProcessor>singleton(new InlineHtmlProcessor(new HtmlDocParser(), new HtmlDocLexer())),
      new NullStopWatch()
    );
    CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new TimeImpl()));
    jstdServerState.setCapturedBrowsers(browsers);
    JsTestDriverServer.Factory factory = new DefaultServerFactory(
      browsers,
      SlaveBrowser.TIMEOUT,
      new NullPathPrefix(),
      Sets.<ServerListener>newHashSet(jstdServerState)
    );
    ServerStartupAction serverStartupAction = new ServerStartupAction(
      serverPort,
      -1,
      new JstdTestCaseStore(),
      false,
      fileLoader,
      factory
    );
    try {
      serverStartupAction.run(null);
      ACTIVE_SERVER_STARTUP_ACTION = serverStartupAction;
      return null;
    } catch (Exception ex) {
      ServerStopAction.runStopAction(serverStartupAction);

      boolean unknownReason = false;

      int sslPort = serverPort + 1;
      final String message;
      if (!isLocalPortAvailable(serverPort)) {
        message = "Can't start JsTestDriver server on port " + serverPort
                  + ".\nMake sure the port is free.";
      }
      else if (!isLocalPortAvailable(sslPort)) {
        message = "Can't start JsTestDriver server.\nMake sure the ssl port " + sslPort + " is free.";
      }
      else {
        message = "Can't start JsTestDriver server due to unknown reasons." + "\n"
                  + "See idea.log for details.";
        unknownReason = true;
      }
      LOG.warn(message, ex);
      return new ServerStartupError(message, unknownReason);
    }
  }

  @SuppressWarnings({"SocketOpenedButNotSafelyClosed", "SynchronizationOnLocalVariableOrMethodParameter", "WaitNotInLoop"})
  private static boolean isLocalPortAvailable(int port) {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket();
      SocketAddress endpoint = new InetSocketAddress(InetAddress.getByName(null), port);
      serverSocket.bind(endpoint);
      //workaround for linux : calling close() immediately after opening socket
      //may result that socket is not closed
      synchronized(serverSocket) {
        try {
          serverSocket.wait(1);
        }
        catch (InterruptedException e) {
          LOG.error(e);
        }
      }
      return true;
    }
    catch (IOException e) {
      return false;
    } finally {
      if (serverSocket != null) {
        try {
          serverSocket.close();
        }
        catch (IOException ignored) {
        }
      }
    }
  }

  private static class DefaultServerFactory implements JsTestDriverServer.Factory {

    private final CapturedBrowsers myCapturedBrowsers;
    private final long myTimeout;
    private final HandlerPathPrefix myHandlerPathPrefix;
    private final Set<ServerListener> myServerListeners;

    public DefaultServerFactory(CapturedBrowsers capturedBrowsers,
                                long timeout,
                                HandlerPathPrefix handlerPathPrefix,
                                Set<ServerListener> serverListeners) {
      myCapturedBrowsers = capturedBrowsers;
      myTimeout = timeout;
      myHandlerPathPrefix = handlerPathPrefix;
      myServerListeners = serverListeners;
    }

    public JsTestDriverServer create(int port, int sslPort, JstdTestCaseStore testCaseStore) {
      return new JsTestDriverServerImpl(port, sslPort, testCaseStore, myCapturedBrowsers, myTimeout,
          myHandlerPathPrefix, myServerListeners, Collections.<FileInfoScheme>emptySet(), ExecutionType.INTERACTIVE, false);
    }
  }

  private static class ServerStartupError {
    private final String myMessage;
    private final boolean myUnknownReason;

    private ServerStartupError(@NotNull String message, boolean unknownReason) {
      myMessage = message;
      myUnknownReason = unknownReason;
    }

    @NotNull
    public String getMessage() {
      return myMessage;
    }

    public boolean isUnknownReason() {
      return myUnknownReason;
    }
  }
}
