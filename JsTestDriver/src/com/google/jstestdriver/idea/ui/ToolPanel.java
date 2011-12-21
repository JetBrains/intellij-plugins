/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.ui;

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
import com.google.jstestdriver.idea.MessageBundle;
import com.google.jstestdriver.idea.PluginResources;
import com.google.jstestdriver.model.HandlerPathPrefix;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.server.JstdTestCaseStore;
import com.google.jstestdriver.util.NullStopWatch;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.text.MessageFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ToolPanel extends JPanel {

  // TODO - make configurable
  public static int serverPort = 9876;
  private static ServerStartupAction myServerStartupAction;
  private static ServerState myState = new ServerState();
  final JTextField captureUrl;

  private List<ServerListener> myServerListeners;

  public ToolPanel() {
    final StatusBar statusBar = new StatusBar(MessageBundle.getBundle());
    final CapturedBrowsersPanel capturedBrowsersPanel = new CapturedBrowsersPanel();
    captureUrl = createCaptureUrl();

    final JButton startServerButton = new JButton(new ServerStartAction());
    final JButton stopServerButton = new JButton(new ServerStopAction());
    LocalManager localManager = new LocalManager(startServerButton, stopServerButton, captureUrl);

    myServerListeners = Arrays.asList(statusBar, capturedBrowsersPanel, localManager);
    for (ServerListener serverListener : myServerListeners) {
      if (myState.isServerRunning()) {
        serverListener.serverStarted();
        for (BrowserInfo browserInfo : myState.getCapturedBrowsers()) {
          serverListener.browserCaptured(browserInfo);
        }
      } else {
        serverListener.serverStopped();
      }
      myState.addServerListener(serverListener);
    }

    setBackground(UIUtil.getTreeTextBackground());
    setLayout(new BorderLayout());
    add(new JPanel() {{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(statusBar);
        add(startServerButton);
        add(stopServerButton);
      }});
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel(PluginResources.getCaptureUrlMessage()));
        add(captureUrl);
      }});
      add(capturedBrowsersPanel);
    }}, BorderLayout.NORTH);

  }

  public JComponent getPrefferedFocusedComponent() {
    return captureUrl;
  }

  private static JTextField createCaptureUrl() {
    final JTextField textField = new JTextField();
    textField.setEditable(false);
    textField.getCaret().setVisible(true);
    final Runnable selectAll = new Runnable() {
      @Override
      public void run() {
        textField.selectAll();
      }
    };
    textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          ApplicationManager.getApplication().invokeLater(selectAll);
        }
      }
    });
    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        ApplicationManager.getApplication().invokeLater(selectAll);
      }
    });
    return textField;
  }


  public void onDispose() {
    for (ServerListener serverListener : myServerListeners) {
      myState.removeServerListener(serverListener);
    }
  }

  private static class ServerStartAction extends AbstractAction {

    ServerStartAction() {
      super("", PluginResources.getServerStartIcon());
      putValue(SHORT_DESCRIPTION, "Start a local server");
    }

    public void actionPerformed(ActionEvent event) {
      FileLoader fileLoader = new ProcessingFileLoader(
          new SimpleFileReader(),
          Collections.<FileLoadPostProcessor>singleton(new InlineHtmlProcessor(new HtmlDocParser(), new HtmlDocLexer())),
          new File("."),
          new NullStopWatch()
      );
      CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new TimeImpl()));
      JsTestDriverServer.Factory factory = new DefaultServerFactory(
          browsers,
          SlaveBrowser.TIMEOUT,
          new NullPathPrefix(),
          Sets.<ServerListener>newHashSet(myState)
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
        myServerStartupAction = serverStartupAction;
      } catch (Exception ex) {
        Messages.showErrorDialog("Can't start JsTestDriver server on port " + serverPort + ".\nMake sure the port is free.", "JsTestDriver server launching");
      }
    }
  }

  private static final class DefaultServerFactory implements JsTestDriverServer.Factory {

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

  private static class ServerStopAction extends AbstractAction {

    ServerStopAction() {
      super("", PluginResources.getServerStopIcon());
      putValue(SHORT_DESCRIPTION, "Stop the local server");
    }

    public void actionPerformed(ActionEvent e) {
      ServerStartupAction serverStartupAction = myServerStartupAction;
      if (serverStartupAction != null) {
        new ServerShutdownAction(serverStartupAction).run(null);
        myServerStartupAction = null;
      }
    }
  }

  private static class LocalManager implements ServerListener {

    private final JButton myStartServerButton;
    private final JButton myStopServerButton;
    private final JTextField myCaptureUrl;

    private LocalManager(JButton startServerButton, JButton stopServerButton, JTextField captureUrl) {
      myStartServerButton = startServerButton;
      myStopServerButton = stopServerButton;
      myCaptureUrl = captureUrl;
    }

    @Override
    public void serverStarted() {
      serverStatusChanged(true);
    }

    @Override
    public void serverStopped() {
      serverStatusChanged(false);
    }

    private void serverStatusChanged(final boolean started) {
      ApplicationManager.getApplication().invokeLater(new Runnable() {
        @Override
        public void run() {
          myStartServerButton.setEnabled(!started);
          myStopServerButton.setEnabled(started);
          if (started) {
            String serverUrl = MessageFormat.format("http://{0}:{1,number,###}/capture",
                                                    InfoPanel.getHostName(), serverPort);
            myCaptureUrl.setText(serverUrl);
          } else {
            myCaptureUrl.setText("");
          }
        }
      });
    }

    @Override
    public void browserCaptured(BrowserInfo info) {}

    @Override
    public void browserPanicked(BrowserInfo info) {}
  }

  private static class ServerState implements ServerListener {

    private volatile boolean myServerRunning = false;
    private final Map<String, BrowserInfo> myCapturedBrowsers = new ConcurrentHashMap<String, BrowserInfo>();
    private final Map<ServerListener, Object> myServerListeners = new IdentityHashMap<ServerListener, Object>();

    @Override
    public void serverStarted() {
      myServerRunning = true;
      for (ServerListener serverListener : myServerListeners.keySet()) {
        serverListener.serverStarted();
      }
    }

    @Override
    public void serverStopped() {
      myServerRunning = false;
      for (ServerListener serverListener : myServerListeners.keySet()) {
        serverListener.serverStopped();
      }
    }

    @Override
    public void browserCaptured(BrowserInfo info) {
      myCapturedBrowsers.put(info.getName(), info);
      for (ServerListener serverListener : myServerListeners.keySet()) {
        serverListener.browserCaptured(info);
      }
    }

    @Override
    public void browserPanicked(BrowserInfo info) {
      myCapturedBrowsers.remove(info.getName());
      for (ServerListener serverListener : myServerListeners.keySet()) {
        serverListener.browserPanicked(info);
      }
    }

    public boolean isServerRunning() {
      return myServerRunning;
    }

    public Collection<BrowserInfo> getCapturedBrowsers() {
      return myCapturedBrowsers.values();
    }

    public void addServerListener(@NotNull ServerListener serverListener) {
      myServerListeners.put(serverListener, true);
    }

    public void removeServerListener(@NotNull ServerListener serverListener) {
      myServerListeners.remove(serverListener);
    }
  }
}
