package com.jetbrains.plugins.cidr.debugger.chart;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.JBUI;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XDebuggerManagerListener;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointManager;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.plugins.cidr.debugger.chart.ui.BreakpointList;
import com.jetbrains.plugins.cidr.debugger.chart.ui.ExpressionList;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SignalSources extends JBSplitter implements XDebuggerManagerListener {

  private final Project myProject;
  private final BreakpointList myBreakpointList;
  private final DebugListener myDebugListener;

  public SignalSources(Project project, DebugListener debugListener, ChartToolPersistence persistence,
                       ChartsPanel chartsPanel) {
    super(true, 0.5f, 0.1f, 0.9f);
    setBorder(JBUI.Borders.empty(15));
    this.myProject = project;
    this.myDebugListener = debugListener;
    persistence.setChangeListener(this::setAllBreakpoints);
    myBreakpointList = new BreakpointList(persistence);
    setAllBreakpoints();
    ExpressionList expressionList =
      new ExpressionList(persistence, () -> chartsPanel.refreshData(persistence.getExpressions()));

    expressionList.setBorder(IdeBorderFactory.createTitledBorder("Expressions"));
    JBPanel<JBPanel> linesPanel = new JBPanel<>(new BorderLayout());
    linesPanel.add(myBreakpointList, BorderLayout.CENTER);
    linesPanel.add(myBreakpointList.getTableHeader(), BorderLayout.NORTH);
    linesPanel.setBorder(IdeBorderFactory.createTitledBorder("Breakpoints"));
    setFirstComponent(linesPanel);
    setSecondComponent(expressionList);
    XDebuggerManager.getInstance(project).getBreakpointManager().addBreakpointListener(myBreakpointList);

    invalidate();
  }

  public static List<XLineBreakpoint<?>> getAllXLineBreakpoints(Project project) {
    XBreakpointManager breakpointManager = XDebuggerManager.getInstance(project).getBreakpointManager();
    XBreakpoint<?>[] xBreakpoints = ApplicationManager.getApplication()
      .runReadAction((Computable<XBreakpoint<?>[]>)breakpointManager::getAllBreakpoints);
    return Stream.of(xBreakpoints)
      .filter(bp -> bp instanceof XLineBreakpoint)
      .map(bp -> (XLineBreakpoint<?>)bp)
      .collect(Collectors.toList());
  }

  protected void setAllBreakpoints() {
    myBreakpointList.setAllBreakpoints(getAllXLineBreakpoints(myProject));
  }

  @Override
  public void currentSessionChanged(@Nullable XDebugSession previousSession, @Nullable XDebugSession currentSession) {
    if (previousSession != null) {
      previousSession.removeSessionListener(myDebugListener);
    }
    if (currentSession != null) {
      currentSession.addSessionListener(myDebugListener);
    }
    else {
      myBreakpointList.setAllBreakpoints(null);
    }
  }
}
