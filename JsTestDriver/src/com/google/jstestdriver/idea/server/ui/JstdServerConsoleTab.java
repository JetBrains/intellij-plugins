package com.google.jstestdriver.idea.server.ui;

import com.google.gson.JsonObject;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerOutputListener;
import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class JstdServerConsoleTab {

  private final ConsoleView myConsoleView;
  private final JstdServerStatusView myStatusView;
  private final TabInfo myTabInfo;

  public JstdServerConsoleTab(@NotNull Project project, @NotNull Disposable parentDisposable) {
    TextConsoleBuilder consoleBuilder = TextConsoleBuilderFactory.getInstance().createBuilder(project);
    consoleBuilder.setViewer(true);
    myConsoleView = consoleBuilder.getConsole();
    Disposer.register(parentDisposable, myConsoleView);
    myStatusView = new JstdServerStatusView(parentDisposable);
    JPanel panel = createContent(myConsoleView, myStatusView);
    myTabInfo = new TabInfo(panel);
    myTabInfo.setText("Console");
  }

  @NotNull
  public TabInfo getTabInfo() {
    return myTabInfo;
  }

  private static JPanel createContent(@NotNull ConsoleView consoleView, @NotNull JstdServerStatusView capturingView) {
    JPanel panel = new JPanel(new BorderLayout(0, 0));
    JComponent consoleComponent = consoleView.getComponent();
    panel.add(consoleComponent, BorderLayout.CENTER);
    ActionToolbar consoleActionToolbar = createActionToolbar(consoleView);
    consoleActionToolbar.setTargetComponent(consoleComponent);
    panel.add(consoleActionToolbar.getComponent(), BorderLayout.WEST);
    panel.add(capturingView.getComponent(), BorderLayout.NORTH);
    return panel;
  }

  @NotNull
  private static ActionToolbar createActionToolbar(@NotNull ConsoleView consoleView) {
    DefaultActionGroup group = new DefaultActionGroup();
    AnAction[] actions = consoleView.createConsoleActions();
    for (AnAction action : actions) {
      group.add(action);
    }
    return ActionManager.getInstance().createActionToolbar("JstdServerConsoleTab", group, false);
  }

  public void attachToServer(@NotNull JstdServer server) {
    myStatusView.attachToServer(server);
    myConsoleView.clear();
    server.addOutputListener(new JstdServerOutputListener() {
      @Override
      public void onOutputAvailable(@NotNull String text, @NotNull Key outputType) {
        ConsoleViewContentType contentType = ConsoleViewContentType.getConsoleViewType(outputType);
        myConsoleView.print(text, contentType);
      }

      @Override
      public void onEvent(@NotNull JsonObject obj) {}
    });
  }

  public void showServerStartupError(@NotNull Throwable error) {
    myConsoleView.clear();
    StringWriter buffer = new StringWriter();
    try (PrintWriter printer = new PrintWriter(buffer)) {
      error.printStackTrace(printer);
    }
    myConsoleView.print(buffer.getBuffer().toString(), ConsoleViewContentType.ERROR_OUTPUT);
  }
}
