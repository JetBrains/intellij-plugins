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
import com.intellij.ide.BrowserSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class JstdToolWindowPanel extends SimpleToolWindowPanel {

  public static final String PLACE = "JsTestDriverToolbar";

  // TODO - make configurable
  public final static int serverPort = 9876;
  public static final JstdServerState SHARED_STATE = new JstdServerState();
  static ServerStartupAction myServerStartupAction;

  private final CaptureUrlController myCaptureUrlController;

  public JstdToolWindowPanel() {
    super(false, true);

    StatusBar statusBar = new StatusBar();
    myCaptureUrlController = new CaptureUrlController();
    JTextField captureUrlTextField = myCaptureUrlController.getCaptureUrlTextField();
    CapturedBrowsersController capturedBrowsersController = new CapturedBrowsersController(captureUrlTextField);

    final ActionToolbar actionToolbar = createActionToolbar();
    setToolbar(actionToolbar.getComponent());

    List<ServerListener> serverListeners = Arrays.asList(
        statusBar,
        capturedBrowsersController,
        myCaptureUrlController.getServerListener(),
        new EdtServerAdapter() {
          @Override
          public void serverStateChanged(boolean started) {
            actionToolbar.updateActionsImmediately();
          }
        }
    );
    for (ServerListener serverListener : serverListeners) {
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

    JPanel content = newVerticalPanelWithLeftAlignedChildren(
      statusBar.getComponent(),
      myCaptureUrlController.getComponent(),
      capturedBrowsersController.getComponent()
    );
    setContent(content);
  }

  @NotNull
  public static JPanel newVerticalPanelWithLeftAlignedChildren(@NotNull Component... children) {
    JPanel panel = new JPanel(new GridBagLayout());
    int childId = 0;
    for (Component child : children) {
      final GridBagConstraints constraints;
      if (childId == children.length - 1) {
        constraints = new GridBagConstraints(
          0, childId,
          1, 1,
          1.0, 1.0,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.BOTH,
          new Insets(0, 0, 0, 0),
          0, 0
        );
      } else {
        constraints = new GridBagConstraints(
          0, childId,
          1, 1,
          1.0, 0.0,
          GridBagConstraints.NORTHWEST,
          GridBagConstraints.HORIZONTAL,
          new Insets(0, 0, 0, 0),
          0, 0
        );
      }
      panel.add(child, constraints);
      childId++;
    }
    return panel;
  }

  @NotNull
  private static ActionToolbar createActionToolbar() {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new ServerStartAction(SHARED_STATE));
    actionGroup.add(new ServerStopAction(SHARED_STATE));
    actionGroup.add(new AnAction("Configure paths to local web browsers", null, PlatformIcons.WEB_ICON) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
        settingsUtil.editConfigurable(e.getProject(), new BrowserSettings());
      }
    });
    return ActionManager.getInstance().createActionToolbar(PLACE, actionGroup, false);
  }

  public JComponent getPreferredFocusedComponent() {
    return myCaptureUrlController.getCaptureUrlTextField();
  }

}
