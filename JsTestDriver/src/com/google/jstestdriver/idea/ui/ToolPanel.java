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
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Collections;
import java.util.Set;

import static java.text.MessageFormat.format;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ToolPanel extends JPanel implements ServerListener {

  // TODO - make configurable
  public static int serverPort = 9876;

  private final StatusBar myStatusBar;
  private final CapturedBrowsersPanel myCapturedBrowsersPanel;
  private ServerStartupAction myServerStartupAction;
  private final JTextField myCaptureUrl;
  private JButton myStartServerButton;
  private JButton myStopServerButton;

  public ToolPanel() {
    myStatusBar = new StatusBar(MessageBundle.getBundle());
    myCapturedBrowsersPanel = new CapturedBrowsersPanel();
    myCaptureUrl = createCaptureUrl();

    setBackground(UIUtil.getTreeTextBackground());
    setLayout(new BorderLayout());
    add(new JPanel() {{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(myStatusBar);
        myStartServerButton = new JButton(new ServerStartAction());
        add(myStartServerButton);
        myStopServerButton = new JButton(new ServerStopAction());
        add(myStopServerButton);
        myStopServerButton.setEnabled(false);
      }});
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel(PluginResources.getCaptureUrlMessage()));
        add(myCaptureUrl);
      }});
      add(myCapturedBrowsersPanel);
    }}, BorderLayout.NORTH);
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
      }
    });
  }

  @Override
  public void browserCaptured(BrowserInfo info) {}

  @Override
  public void browserPanicked(BrowserInfo info) {}

  private class ServerStartAction extends AbstractAction {

    ServerStartAction() {
      super("", PluginResources.getServerStartIcon());
      putValue(SHORT_DESCRIPTION, "Start a local server");
    }

    public void actionPerformed(ActionEvent e) {
      CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new TimeImpl()));

      FileLoader fileLoader = new ProcessingFileLoader(
          new SimpleFileReader(),
          Collections.<FileLoadPostProcessor>singleton(new InlineHtmlProcessor(new HtmlDocParser(), new HtmlDocLexer())),
          new File("."),
          new NullStopWatch()
      );
      JsTestDriverServer.Factory factory = new DefaultServerFactory(
          browsers,
          SlaveBrowser.TIMEOUT,
          new NullPathPrefix(),
          Sets.<ServerListener>newHashSet(ToolPanel.this, myStatusBar, myCapturedBrowsersPanel)
      );
      myServerStartupAction = new ServerStartupAction(serverPort, -1, new JstdTestCaseStore(),
                                                      false, fileLoader, factory);
      myServerStartupAction.run(null);

      final String serverUrl = format("http://{0}:{1,number,###}/capture", InfoPanel.getHostName(), serverPort);
      myCaptureUrl.setText(serverUrl);
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
          myHandlerPathPrefix, myServerListeners, Collections.<FileInfoScheme>emptySet(), ExecutionType.INTERACTIVE);
    }
  }

  private class ServerStopAction extends AbstractAction {
    ServerStopAction() {
      super("", PluginResources.getServerStopIcon());
      putValue(SHORT_DESCRIPTION, "Stop the local server");
    }
    public void actionPerformed(ActionEvent e) {
      if (myServerStartupAction != null) {
        new ServerShutdownAction(myServerStartupAction).run(null);
      }
    }
  }
}
