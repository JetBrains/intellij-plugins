// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.JstdServerSettings;
import com.intellij.ide.browsers.BrowserSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.TabbedPaneWrapper;
import com.intellij.ui.border.CustomLineBorder;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.awt.*;

public class JstdToolWindowSession {

  private final Project myProject;
  private final ActionToolbar myToolbar;
  private final TabbedPaneWrapper myTabs;
  private final JPanel myRootPanel;
  private final JstdServerSettingsTab mySettingsTab;
  private JstdServerConsoleTab myConsoleTab;

  public JstdToolWindowSession(@NotNull Project project) {
    myProject = project;
    myToolbar = createActionToolbar();
    myTabs = new TabbedPaneWrapper(myProject);
    myTabs.getComponent().setBorder(JBUI.Borders.empty(0, 2, 0, 0));

    mySettingsTab = new JstdServerSettingsTab(myProject);
    addTab(mySettingsTab.getTabInfo());

    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null && server.isProcessRunning()) {
      attachToServer(server);
    }

    myRootPanel = new JPanel(new BorderLayout(0, 0));
    myToolbar.getComponent().setBorder(new CustomLineBorder(JBColor.border(), 0, 0, 0, 1));
    myRootPanel.add(myToolbar.getComponent(), BorderLayout.WEST);
    myRootPanel.add(myTabs.getComponent(), BorderLayout.CENTER);
  }

  private void addTab(@NotNull TabInfo tabInfo) {
    myTabs.addTab(tabInfo.getText(), tabInfo.getComponent());
  }

  @NotNull
  public JComponent getComponent() {
    return myRootPanel;
  }

  @NotNull
  private ActionToolbar createActionToolbar() {
    DefaultActionGroup actionGroup = new DefaultActionGroup();
    actionGroup.add(new JstdServerRestartAction(this));
    actionGroup.add(new JstdServerStopAction());
    actionGroup.add(new AnAction("Configure paths to local web browsers", null, PlatformIcons.WEB_ICON) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
        settingsUtil.editConfigurable(e.getProject(), new BrowserSettings());
      }
    });
    return ActionManager.getInstance().createActionToolbar("JstdToolWindowSession", actionGroup, false);
  }

  @NotNull
  public Project getProject() {
    return myProject;
  }

  public void saveSettings() {
    mySettingsTab.saveSettings();
  }

  @NotNull
  private JstdServerConsoleTab getOrRegisterConsoleContent() {
    if (myConsoleTab == null) {
      myConsoleTab = new JstdServerConsoleTab(myProject, myProject);
      addTab(myConsoleTab.getTabInfo());
    }
    return myConsoleTab;
  }

  private void attachToServer(@NotNull JstdServer server) {
    ApplicationManager.getApplication().assertIsDispatchThread();
    myToolbar.updateActionsImmediately();
    server.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
      @Override
      public void onServerStopped() {
        myToolbar.updateActionsImmediately();
      }
    }, myProject);
    JstdServerConsoleTab consoleTab = getOrRegisterConsoleContent();
    consoleTab.attachToServer(server);
    myTabs.setSelectedTitle(consoleTab.getTabInfo().getText());
  }

  private void showServerStartupError(@NotNull Throwable error) {
    JstdServerConsoleTab consoleView = getOrRegisterConsoleContent();
    consoleView.showServerStartupError(error);
  }

  @NotNull
  public Promise<JstdServer> restart(@NotNull JstdServerSettings settings) {
    return JstdServerRegistry.getInstance().restartServer(settings)
      .onError(this::showServerStartupError)
      .then(server -> {
        attachToServer(server);
        return server;
      });
  }
}
