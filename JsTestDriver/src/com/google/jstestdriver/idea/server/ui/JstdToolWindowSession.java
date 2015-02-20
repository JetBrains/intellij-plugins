package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.JstdServerSettings;
import com.intellij.execution.ui.layout.impl.JBRunnerTabs;
import com.intellij.ide.browsers.BrowserSettings;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.util.Consumer;
import com.intellij.util.Function;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

import javax.swing.*;
import java.awt.*;

public class JstdToolWindowSession {

  private final Project myProject;
  private final ActionToolbar myToolbar;
  private final JBRunnerTabs myTabs;
  private final JPanel myRootPanel;
  private final JstdServerSettingsTab mySettingsTab;
  private JstdServerConsoleTab myConsoleTab;

  public JstdToolWindowSession(@NotNull Project project) {
    myProject = project;
    myToolbar = createActionToolbar();
    myTabs = new JBRunnerTabs(project, ActionManager.getInstance(), IdeFocusManager.getInstance(project), myProject);
    myTabs.getPresentation()
      .setInnerInsets(new Insets(0, 0, 0, 0))
      .setPaintBorder(0, 0, 0, 0)
      .setPaintFocus(false)
      .setRequestFocusOnLastFocusedComponent(true);
    myTabs.setTabDraggingEnabled(false);
    myTabs.getComponent().setBorder(IdeBorderFactory.createEmptyBorder(0, 2, 0, 0));

    mySettingsTab = new JstdServerSettingsTab(myProject);
    myTabs.addTab(mySettingsTab.getTabInfo());

    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null && server.isProcessRunning()) {
      attachToServer(server);
    }

    myRootPanel = new JPanel(new BorderLayout(0, 0));
    myRootPanel.add(myToolbar.getComponent(), BorderLayout.WEST);
    myRootPanel.add(myTabs, BorderLayout.CENTER);
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
      public void actionPerformed(AnActionEvent e) {
        ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
        settingsUtil.editConfigurable(e.getProject(), new BrowserSettings());
      }
    });
    return ActionManager.getInstance().createActionToolbar(ActionPlaces.UNKNOWN, actionGroup, false);
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
      myTabs.addTab(myConsoleTab.getTabInfo());
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
    myTabs.select(consoleTab.getTabInfo(), true);
  }

  private void showServerStartupError(@NotNull Throwable error) {
    JstdServerConsoleTab consoleView = getOrRegisterConsoleContent();
    consoleView.showServerStartupError(error);
  }

  @NotNull
  public Promise<JstdServer> restart(@NotNull JstdServerSettings settings) {
    return JstdServerRegistry.getInstance().restartServer(settings)
      .rejected(new Consumer<Throwable>() {
        @Override
        public void consume(Throwable error) {
          showServerStartupError(error);
        }
      })
      .then(new Function<JstdServer, JstdServer>() {
        @Override
        public JstdServer fun(JstdServer server) {
          attachToServer(server);
          return server;
        }
      });
  }
}
