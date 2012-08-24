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
import com.google.jstestdriver.idea.icons.JstdIcons;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.NullStopWatch;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public class ServerStartAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(ServerStartAction.class);

  public static volatile ServerStartupAction ACTIVE_SERVER_STARTUP_ACTION = null;
  private static boolean myLocalServerStarting = false;

  public ServerStartAction() {
    super("Start a local server", null, JstdIcons.getIcon("startLocalServer.png"));
  }

  @Override
  public void update(AnActionEvent e) {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    e.getPresentation().setEnabled(!jstdServerState.isServerRunning());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    asyncStartServer(null);
  }

  /**
   * Called on EDT.
   */
  public static void asyncStartServer(@Nullable final Runnable callback) {
    if (!myLocalServerStarting) {
      myLocalServerStarting = true;
      ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
        @Override
        public void run() {
          syncStartServer();
          if (callback != null) {
            callback.run();
          }
          ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
              localServerStarted();
            }
          });
        }
      });
    }
  }

  private static void localServerStarted() {
    myLocalServerStarting = false;
  }

  private static void syncStartServer() {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    if (jstdServerState.isServerRunning()) {
      return;
    }
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
    } catch (Exception ex) {
      ServerStopAction.runStopAction(serverStartupAction);

      final String title = "JsTestDriver Server Launching";
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
                         + "See idea.log (Help->Reveal Log in ...).";
        LOG.warn(message, ex);
      }
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          Messages.showErrorDialog(message, title);
        }
      });
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

}
