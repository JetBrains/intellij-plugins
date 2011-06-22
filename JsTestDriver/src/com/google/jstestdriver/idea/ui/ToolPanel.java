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

import com.google.jstestdriver.*;
import com.google.jstestdriver.browser.BrowserIdStrategy;
import com.google.jstestdriver.hooks.FileLoadPostProcessor;
import com.google.jstestdriver.html.HtmlDocLexer;
import com.google.jstestdriver.html.HtmlDocParser;
import com.google.jstestdriver.html.InlineHtmlProcessor;
import com.google.jstestdriver.idea.MessageBundle;
import com.google.jstestdriver.idea.PluginResources;
import com.google.jstestdriver.model.NullPathPrefix;
import com.google.jstestdriver.util.NullStopWatch;
import com.intellij.util.ui.UIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

import static java.text.MessageFormat.format;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ToolPanel extends JPanel implements Observer {

  private StatusBar statusBar;
  private CapturedBrowsersPanel capturedBrowsersPanel;
  private ServerStartupAction serverStartupAction;
  // TODO - make configurable
  public static int serverPort = 9876;
  private FilesCache cache = new FilesCache(new HashMap<String, FileInfo>());
  private JTextField captureUrl;
  private JButton startServerButton;
  private JButton stopServerButton;

  public ToolPanel() {
    statusBar = new StatusBar(StatusBar.Status.NOT_RUNNING, MessageBundle.getBundle());
    capturedBrowsersPanel = new CapturedBrowsersPanel();
    captureUrl = new JTextField() {{
      setEditable(false);
    }};

    setBackground(UIUtil.getTreeTextBackground());
    setLayout(new BorderLayout());
    add(new JPanel() {{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(statusBar);
        startServerButton = new JButton(new ServerStartAction());
        add(startServerButton);
        stopServerButton = new JButton(new ServerStopAction());
        add(stopServerButton);
        stopServerButton.setEnabled(false);
      }});
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel(PluginResources.getCaptureUrlMessage()));
        add(captureUrl);
      }});
      add(capturedBrowsersPanel);
    }}, BorderLayout.NORTH);
  }

  public void update(Observable observable, Object event) {
    if (observable instanceof JsTestDriverServer) {
      switch ((JsTestDriverServer.Event) event) {
        case STARTED:
          stopServerButton.setEnabled(true);
          startServerButton.setEnabled(false);
          break;
        case STOPPED:
          startServerButton.setEnabled(true);
          stopServerButton.setEnabled(false);
          break;
      }
    }
  }

  private class ServerStartAction extends AbstractAction {
    ServerStartAction() {
      super("", PluginResources.getServerStartIcon());
      putValue(SHORT_DESCRIPTION, "Start a local server");
    }
    public void actionPerformed(ActionEvent e) {
      CapturedBrowsers browsers = new CapturedBrowsers(new BrowserIdStrategy(new TimeImpl()));
      browsers.addObserver(capturedBrowsersPanel);
      browsers.addObserver(statusBar);
      serverStartupAction =
          new ServerStartupAction(serverPort, browsers, cache, SlaveBrowser.TIMEOUT, false, new ProcessingFileLoader(
              new SimpleFileReader(),
              Collections.<FileLoadPostProcessor>singleton(new InlineHtmlProcessor(new HtmlDocParser(), new HtmlDocLexer())),
              new File("."),
              new NullStopWatch()
          ), new NullPathPrefix());
      serverStartupAction.addObservers(Arrays.<Observer>asList(statusBar, capturedBrowsersPanel, ToolPanel.this));
      serverStartupAction.run(null);
      final String serverUrl = format("http://{0}:{1,number,###}/capture", InfoPanel.getHostName(), serverPort);
      captureUrl.setText(serverUrl);
    }
  }

  private class ServerStopAction extends AbstractAction {
    ServerStopAction() {
      super("", PluginResources.getServerStopIcon());
      putValue(SHORT_DESCRIPTION, "Stop the local server");
    }
    public void actionPerformed(ActionEvent e) {
      if (serverStartupAction != null) {
        new ServerShutdownAction(serverStartupAction).run(null);
      }
    }
  }
}
