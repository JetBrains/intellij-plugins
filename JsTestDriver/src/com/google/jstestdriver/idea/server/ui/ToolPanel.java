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
package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.BrowserInfo;
import com.google.jstestdriver.ServerStartupAction;
import com.google.jstestdriver.hooks.ServerListener;
import com.google.jstestdriver.idea.MessageBundle;
import com.google.jstestdriver.idea.PluginResources;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class ToolPanel extends SimpleToolWindowPanel {

  // TODO - make configurable
  public final static int serverPort = 9876;
  static ServerStartupAction myServerStartupAction;
  public static final JstdServerState SHARED_STATE = new JstdServerState();

  private final JTextField myCaptureUrlTextField;

  public ToolPanel() {
    super(false, true);

    final StatusBar statusBar = new StatusBar(MessageBundle.getBundle());
    final CapturedBrowsersPanel capturedBrowsersPanel = new CapturedBrowsersPanel();
    myCaptureUrlTextField = createCaptureUrlTextField();

    ActionToolbar actionToolbar = createActionToolbar();
    setToolbar(actionToolbar.getComponent());

    List<ServerListener> myServerListeners = Arrays.asList(
        statusBar,
        capturedBrowsersPanel,
        new LocalManager(actionToolbar, myCaptureUrlTextField)
    );
    for (ServerListener serverListener : myServerListeners) {
      if (SHARED_STATE.isServerRunning()) {
        serverListener.serverStarted();
        for (BrowserInfo browserInfo : SHARED_STATE.getCapturedBrowsers()) {
          serverListener.browserCaptured(browserInfo);
        }
      } else {
        serverListener.serverStopped();
      }
      SHARED_STATE.addServerListener(serverListener);
    }

    setBackground(UIUtil.getTreeTextBackground());
    JPanel content = new JPanel() {{
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      add(statusBar);
      add(new JPanel() {{
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel(PluginResources.getCaptureUrlMessage()));
        add(myCaptureUrlTextField);
      }});
      add(capturedBrowsersPanel);
    }};
    JPanel wrapPanel = new JPanel(new BorderLayout());
    wrapPanel.add(content, BorderLayout.NORTH);
    setContent(wrapPanel);
  }

  private static ActionToolbar createActionToolbar() {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new ServerStartAction(SHARED_STATE));
    actionGroup.add(new ServerStopAction(SHARED_STATE));
    return ActionManager.getInstance().createActionToolbar(
        ActionPlaces.TODO_VIEW_TOOLBAR, actionGroup, false
    );
  }

  public JComponent getPreferredFocusedComponent() {
    return myCaptureUrlTextField;
  }

  private static JTextField createCaptureUrlTextField() {
    final JTextField textField = new JTextField();
    textField.setEditable(false);
    textField.setBackground(Color.WHITE);
    textField.getCaret().setVisible(true);
    textField.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getClickCount() == 2) {
          textField.selectAll();
        }
      }
    });
    textField.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        textField.selectAll();
      }
    });
    return textField;
  }

  private static class LocalManager implements ServerListener {

    private final ActionToolbar myActionToolbar;
    private final JTextField myCaptureUrl;

    private LocalManager(@NotNull ActionToolbar actionToolbar, @NotNull JTextField captureUrl) {
      myActionToolbar = actionToolbar;
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
          if (started) {
            String serverUrl = MessageFormat.format("http://{0}:{1,number,###}/capture",
                                                    getHostName(), serverPort);
            myCaptureUrl.setText(serverUrl);
            myCaptureUrl.requestFocusInWindow();
            myCaptureUrl.selectAll();
          } else {
            myCaptureUrl.setText("");
          }
          myActionToolbar.updateActionsImmediately();
        }
      });
    }

    public static String getHostName() {
      try {
        InetAddress address = InetAddress.getByName(null);
        return address.getHostName();
      } catch (UnknownHostException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void browserCaptured(BrowserInfo info) {}

    @Override
    public void browserPanicked(BrowserInfo info) {}
  }

}
