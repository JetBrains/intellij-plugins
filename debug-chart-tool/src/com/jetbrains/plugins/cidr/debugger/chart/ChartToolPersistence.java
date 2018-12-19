package com.jetbrains.plugins.cidr.debugger.chart;

import com.intellij.openapi.components.PersistentStateComponentWithModificationTracker;
import com.intellij.openapi.components.State;
import com.intellij.openapi.project.Project;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.jetbrains.plugins.cidr.debugger.chart.state.ChartExpression;
import com.jetbrains.plugins.cidr.debugger.chart.state.ChartToolState;
import com.jetbrains.plugins.cidr.debugger.chart.state.LineState;
import com.jetbrains.plugins.cidr.debugger.chart.state.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.jetbrains.plugins.cidr.debugger.chart.ChartTool.CHART_EXPR_KEY;


@State(name = "charttool")
public class ChartToolPersistence implements PersistentStateComponentWithModificationTracker<ChartToolState> {
  private final Project myProject;
  private long myModificationsCount = 0;
  private Runnable myChangeListener;
  private final List<ChartExpression> myExpressions = new ArrayList<>();

  public ChartToolPersistence(Project project) {
    this.myProject = project;
  }

  @Override
  public long getStateModificationCount() {
    return myModificationsCount;
  }

  @Nullable
  @Override
  public ChartToolState getState() {
    ChartToolState state = new ChartToolState();

    for (XLineBreakpoint<?> breakpoint : SignalSources.getAllXLineBreakpoints(myProject)) {
      LineState lineState = breakpoint.getUserData(CHART_EXPR_KEY);

      if (lineState != null) {
        Location location = new Location(breakpoint);
        state.myLocations.put(location, lineState);
      }
    }
    state.myExpressions.clear();
    state.myExpressions.addAll(myExpressions);
    return state;
  }

  @Override
  public void loadState(@NotNull ChartToolState state) {

    for (XLineBreakpoint<?> breakpoint : SignalSources.getAllXLineBreakpoints(myProject)) {
      LineState lineState = state.myLocations.get(new Location(breakpoint));
      breakpoint.putUserData(CHART_EXPR_KEY, lineState);
    }
    myExpressions.clear();
    myExpressions.addAll(state.myExpressions);

    if (myChangeListener != null) {
      myChangeListener.run();
    }
  }

  public void registerChange() {
    myModificationsCount++;
  }

  public void setChangeListener(Runnable changeListener) {
    this.myChangeListener = changeListener;
  }

  public List<ChartExpression> getExpressions() {
    return myExpressions;
  }
}
