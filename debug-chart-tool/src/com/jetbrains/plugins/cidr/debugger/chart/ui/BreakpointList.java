package com.jetbrains.plugins.cidr.debugger.chart.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ui.table.JBTable;
import com.intellij.util.containers.SortedList;
import com.intellij.xdebugger.breakpoints.XBreakpoint;
import com.intellij.xdebugger.breakpoints.XBreakpointListener;
import com.intellij.xdebugger.breakpoints.XLineBreakpoint;
import com.intellij.xdebugger.impl.breakpoints.XBreakpointBase;
import com.jetbrains.plugins.cidr.debugger.chart.ChartToolPersistence;
import com.jetbrains.plugins.cidr.debugger.chart.state.LineState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.util.Collection;
import java.util.stream.IntStream;

import static com.jetbrains.plugins.cidr.debugger.chart.ChartTool.CHART_EXPR_KEY;


public class BreakpointList extends JBTable implements XBreakpointListener<XBreakpoint<?>> {

  private final SortedList<XLineBreakpoint<?>> breakpoints = new SortedList<>(XLineBreakpointComparator.COMPARATOR);
  private final ChartToolPersistence persistence;

  public BreakpointList(ChartToolPersistence persistence) {
    this.persistence = persistence;
    setModel(createModel());

    TableColumnModel columnModel = getColumnModel();
    TableColumn column0 = columnModel.getColumn(0);
    column0.setPreferredWidth(16);
    column0.setMaxWidth(16);
    column0.setMinWidth(16);
    column0.setResizable(false);
    getTableHeader().setResizingColumn(column0);
    IntStream.range(2, getColumnCount()).mapToObj(columnModel::getColumn).forEach(c -> c.setPreferredWidth(60));
    setAutoResizeMode(AUTO_RESIZE_NEXT_COLUMN);
    setShowColumns(true);
  }

  private TableModel createModel() {
    return new BreakpointModel();
  }


  protected Object getIcon(XLineBreakpoint<?> breakpoint) {
    Icon icon = null;
    if (breakpoint instanceof XBreakpointBase) {
      icon = ((XBreakpointBase)breakpoint).getIcon();
    }
    return icon == null ? AllIcons.Debugger.Db_set_breakpoint : icon;
  }

  public void setAllBreakpoints(@Nullable Collection<XLineBreakpoint<?>> breakpoints) {
    this.breakpoints.clear();
    if (breakpoints != null) {
      this.breakpoints.addAll(breakpoints);
    }
    rebuild();
  }

  @Override
  public void breakpointAdded(@NotNull XBreakpoint<?> breakpoint) {
    if (breakpoint instanceof XLineBreakpoint) {
      this.breakpoints.add((XLineBreakpoint<?>)breakpoint);
      rebuild();
    }
  }

  @Override
  public void breakpointRemoved(@NotNull XBreakpoint<?> breakpoint) {
    if (breakpoint instanceof XLineBreakpoint) {
      breakpoints.remove(breakpoint);
      rebuild();
    }
  }

  @Override
  public void breakpointChanged(@NotNull XBreakpoint<?> breakpoint) {
    //the key is changed; there is a chance that it's not searchable anymore
    if (breakpoint instanceof XLineBreakpoint) {
      if (!breakpoints.remove(breakpoint)) {
        breakpoints.removeIf(b -> b == breakpoint);
      }
      breakpointAdded(breakpoint);
    }
  }

  private void rebuild() {
    revalidate();
    repaint();
  }

  private class BreakpointModel extends AbstractTableModel {
    @Override
    public int getRowCount() {
      return breakpoints.size();
    }

    @Override
    public int getColumnCount() {
      return 5;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      XLineBreakpoint<?> breakpoint = breakpoints.get(rowIndex);
      switch (columnIndex) {
        case 0:
          return getIcon(breakpoint);
        case 1:
          return breakpoint.getShortFilePath() + ": " + breakpoint.getLine();
        default:
          LineState lineState = CHART_EXPR_KEY.get(breakpoint);
          if (lineState == null) {
            return false;
          }
          switch (columnIndex) {
            case 2:
              return lineState.myClearChart;
            case 3:
              return lineState.mySample;
            case 4:
              return lineState.myAutoResume;
            default:
              return false;
          }
      }
    }

    @Override
    public String getColumnName(int column) {
      return new String[]{"", "Location", "Clear", "Sample", "Auto Resume"}[column];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      switch (columnIndex) {
        case 0:
          return Icon.class;
        case 1:
          return String.class;
        default:
          return Boolean.class;
      }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return columnIndex >= 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      XLineBreakpoint<?> breakpoint = breakpoints.get(rowIndex);
      LineState lineState = CHART_EXPR_KEY.get(breakpoint);
      if (lineState == null) {
        lineState = new LineState();
        CHART_EXPR_KEY.set(breakpoint, lineState);
      }
      persistence.registerChange();
      switch (columnIndex) {
        case 2:
          lineState.myClearChart = Boolean.TRUE.equals(aValue);
          break;
        case 3:
          lineState.mySample = Boolean.TRUE.equals(aValue);
          break;
        case 4:
          lineState.myAutoResume = Boolean.TRUE.equals(aValue);
          break;
      }
    }
  }
}
