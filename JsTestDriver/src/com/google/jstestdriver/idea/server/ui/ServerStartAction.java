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
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.NullStopWatch;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public class ServerStartAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(ServerStartAction.class);

  private final JstdServerState myServerState;

  public ServerStartAction(@NotNull JstdServerState serverState) {
    super("Start a local server", null, JstdIcons.getIcon("startLocalServer.png"));
    myServerState = serverState;
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(!myServerState.isServerRunning());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    int serverPort = JstdToolWindowPanel.serverPort;
    FileLoader fileLoader = new ProcessingFileLoader(
        new SimpleFileReader(),
        Collections.<FileLoadPostProcessor>singleton(new InlineHtmlProcessor(new HtmlDocParser(), new HtmlDocLexer())),
        new NullStopWatch()
    );
    CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new TimeImpl()));
    JsTestDriverServer.Factory factory = new DefaultServerFactory(
        browsers,
        SlaveBrowser.TIMEOUT,
        new NullPathPrefix(),
        Sets.<ServerListener>newHashSet(JstdToolWindowPanel.SHARED_STATE)
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
      JstdToolWindowPanel.myServerStartupAction = serverStartupAction;
    } catch (Exception ex) {
      ServerStopAction.runStopAction(serverStartupAction);

      String title = "JsTestDriver Server Launching";
      int sslPort = serverPort + 1;
      if (!isLocalPortAvailable(serverPort)) {
        Messages.showErrorDialog(
          "Can't start JsTestDriver server on port " + serverPort
          + ".\nMake sure the port is free.",
          title
        );
      }
      else if (!isLocalPortAvailable(sslPort)) {
          Messages.showErrorDialog(
            "Can't start JsTestDriver server.\nMake sure the ssl port " + sslPort + " is free.",
            title
          );
      }
      else {
        String message = "Can't start JsTestDriver server due to unknown reasons.";
        Messages.showErrorDialog(
          message + "\n" + "See idea.log (Help->Reveal Log in ...).",
          title
        );
        LOG.warn(message, ex);
      }
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
