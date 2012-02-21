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
import com.intellij.openapi.ui.Messages;

import java.util.Collections;
import java.util.Set;

/**
 * @author Sergey Simonchik
 */
public class ServerStartAction extends AnAction {

  private final JstdServerState myServerState;

  public ServerStartAction(JstdServerState serverState) {
    super("Start a local server", null, JstdIcons.getIcon("startLocalServer.png"));
    myServerState = serverState;
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(!myServerState.isServerRunning());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    int serverPort = ToolPanel.serverPort;
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
        Sets.<ServerListener>newHashSet(ToolPanel.SHARED_STATE)
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
      ToolPanel.myServerStartupAction = serverStartupAction;
    } catch (Exception ex) {
      Messages.showErrorDialog(
          "Can't start JsTestDriver server on port " + serverPort
              + ".\nMake sure the port is free.",
          "JsTestDriver Server Launching"
      );
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
