package com.jetbrains.plugins.cidr.debugger.chart;

import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebugSessionListener;
import com.intellij.xdebugger.XDebuggerManager;
import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.cidr.execution.debugger.CidrDebugProcess;
import com.jetbrains.cidr.execution.debugger.CidrEvaluator;
import com.jetbrains.cidr.execution.debugger.backend.gdb.GDBDriver;
import com.jetbrains.plugins.cidr.debugger.chart.state.ChartExpression;
import com.jetbrains.plugins.cidr.debugger.chart.state.ExpressionState;
import com.jetbrains.plugins.cidr.debugger.chart.state.LineState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.jetbrains.plugins.cidr.debugger.chart.ChartTool.CHART_EXPR_KEY;
import static com.jetbrains.plugins.cidr.debugger.chart.SignalSources.getAllXLineBreakpoints;

public class DebugListener implements XDebugSessionListener {
  private static final Pattern ARRAY_STRIPPER = Pattern.compile("^[^{]*\\{|\\s+|}$");
  private final Project myProject;
  private final ChartsPanel myChartsPanel;
  private final XDebuggerManager myDebuggerManager;
  private final ChartToolPersistence myPersistence;

  public DebugListener(Project project, ChartsPanel chartsPanel, ChartToolPersistence persistence) {
    this.myProject = project;
    this.myChartsPanel = chartsPanel;
    myDebuggerManager = XDebuggerManager.getInstance(project);
    this.myPersistence = persistence;
  }

  protected void showError(Throwable rejected, String chartExpression) {
    String message = chartExpression + ": " + rejected.getLocalizedMessage();
    String title = rejected.getClass().getSimpleName();
    ApplicationManager.getApplication().invokeLater(() -> Messages.showErrorDialog(message, title));
  }

  @Override
  public void sessionPaused() {
    XDebugSession session = myDebuggerManager.getCurrentSession();
    if (session == null) {
      return;
    }
    XStackFrame currentStackFrame = session.getCurrentStackFrame();
    if (currentStackFrame == null) {
      return;
    }
    XSourcePosition currentPosition = session.getCurrentPosition();
    if (currentPosition == null) {
      return;
    }
    CidrEvaluator evaluator = (CidrEvaluator)currentStackFrame.getEvaluator();
    if (evaluator == null) {
      return;
    }
    List<XLineBreakpoint<?>> allXLineBreakpoints = getAllXLineBreakpoints(myProject);
    for (XLineBreakpoint<?> breakpoint : allXLineBreakpoints) {
      if (XSourcePosition.isOnTheSameLine(currentPosition, breakpoint.getSourcePosition())) {
        LineState lineState = breakpoint.getUserData(CHART_EXPR_KEY);
        if (lineState != null) {
          if (lineState.myClearChart) {
            myChartsPanel.clear();
          }
          if (lineState.mySample) {
            sampleChart((CidrDebugProcess)session.getDebugProcess());
          }
          if (lineState.myAutoResume) {
            session.resume();
          }
        }
      }
    }
  }

  private void sampleChart(@NotNull CidrDebugProcess debugProcess) {
    for (ChartExpression chartExpression : myPersistence.getExpressions()) {
      String expressionTrim = chartExpression.getExpressionTrim();
      if (chartExpression.getState() == ExpressionState.DISABLED ||
          expressionTrim.isEmpty() ||
          (chartExpression.getState() == ExpressionState.SAMPLE_ONCE
           && myChartsPanel.isSampled(chartExpression.getName()))) {
        continue;
      }
      Promise<String> sinDataPromise = debugProcess.postCommand(
        debuggerDriver -> (((GDBDriver)debuggerDriver).interruptAndExecuteConsole("p/r " + expressionTrim))
      );
      try {
        String evalResult = sinDataPromise
          .blockingGet(20, TimeUnit.SECONDS);
        processGdbOutput(evalResult, chartExpression);
      }
      catch (ExecutionException e) {
        ApplicationManager.getApplication().invokeLater(
          () ->
          {
            ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
            if (toolWindowManager.canShowNotification(ToolWindowId.RUN)) {
              Throwable t = e;
              while (t.getCause() != null) {
                t = t.getCause();
              }
              String localizedMessage = t.getLocalizedMessage();
              NotificationGroup
                .toolWindowGroup("Debugger Charts", ToolWindowId.DEBUG)
                .createNotification(expressionTrim + ": " + localizedMessage,
                                    MessageType.WARNING);
            }
          }
        );
      }
      catch (Throwable e) {
        showError(e, expressionTrim);
      }
    }
  }

  private void processGdbOutput(String v, ChartExpression chartExpression) {
    if (v != null) {
      try {
        String strippedV = ARRAY_STRIPPER.matcher(v).replaceAll("");
        List<Number> data = new ArrayList<>();
        for (StringTokenizer tokenizer = new StringTokenizer(strippedV, ",");
             tokenizer.hasMoreTokens(); ) {
          data.add(Double.parseDouble(tokenizer.nextToken()));
        }
        myChartsPanel.series(chartExpression, data);
      }
      catch (Throwable e) {
        showError(e, chartExpression.getName());
      }
    }
  }
}
