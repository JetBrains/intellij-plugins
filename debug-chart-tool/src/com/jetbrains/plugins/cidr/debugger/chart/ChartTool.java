package com.jetbrains.plugins.cidr.debugger.chart;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.plugins.cidr.debugger.chart.state.LineState;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;


public class ChartTool implements ToolWindowFactory {
  public static final Key<LineState> CHART_EXPR_KEY = Key.create(ChartTool.class.getName() + "#breakpoint");

  static {
    Platform.setImplicitExit(false);
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ContentManager contentManager = toolWindow.getContentManager();
    ContentFactory factory = contentManager.getFactory();
    ChartsPanel chartsPanel = new ChartsPanel();
    contentManager.addContent(
      factory.createContent(chartsPanel, "Chart", true)
    );

    ChartToolPersistence persistence = project.getComponent(ChartToolPersistence.class);
    DebugListener debugListener = new DebugListener(project, chartsPanel, persistence);
    SignalSources sources = new SignalSources(project, debugListener, persistence, chartsPanel);
    contentManager.addContent(
      factory.createContent(sources, "Sources", true)
    );

    project.getMessageBus().connect().subscribe(XDebuggerManager.TOPIC, sources);
  }
}
