package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.pom.Navigatable;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.tree.TreeUtil;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.util.MathUtil;
import com.jetbrains.actionscript.profiler.vo.CallInfo;
import org.jetbrains.annotations.NonNls;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Comparator;
import java.util.List;

public class CallTreeTable extends TreeTable implements DataProvider {
  private static final int DEFAULT_ROW_HEIGHT = 20;

  public CallTreeTable() {
    super(new SortableListTreeTableModel(new DefaultMutableTreeNode(), getHotSpotsColumns()));

    TreeTableRowSorter rowSorter = new TreeTableRowSorter(this);

    for (int i = 0; i < getSortableTreeTableModel().getColumnCount(); ++i) {
      TableCellRenderer tableCellRenderer = getSortableTreeTableModel().getRenderer(i);
      if (tableCellRenderer != null) {
        getColumnModel().getColumn(i).setCellRenderer(tableCellRenderer);
      }

      Comparator comparator = getSortableTreeTableModel().getComparator(i);

      if (comparator != null) {
        rowSorter.setComparator(i, comparator);
      }
    }

    setRowSorter(rowSorter);

    rowSorter.addRowSorterListener(new RowSorterListener() {
      @Override
      public void sorterChanged(RowSorterEvent e) {
        final List<TreePath> paths = TreeUtil.collectExpandedPaths(getTree());
        reload();
        repaint();
        getTableHeader().repaint();
        TreeUtil.restoreExpandedPaths(getTree(), paths);
      }
    });

    setRowHeight(DEFAULT_ROW_HEIGHT);
  }

  @Override
  public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
    JComponent jComponent = (JComponent)super.prepareRenderer(renderer, row, column);
    jComponent.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
    return jComponent;
  }

  public void reload() {
    getSortableTreeTableModel().reload();
  }

  public SortableListTreeTableModel getSortableTreeTableModel() {
    return (SortableListTreeTableModel)getTableModel();
  }

  @Override
  public Object getData(@NonNls String dataId) {
    if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
      return getSelectedNavigableItem();
    }
    return null;
  }

  private Navigatable getSelectedNavigableItem() {
    int row = getSelectedRow();
    int column = getSelectedColumn();
    Object value = getValueAt(row, column);
    if (value instanceof Navigatable) {
      return (Navigatable)value;
    }
    if (value instanceof NavigatableDataProducer) {
      return ((NavigatableDataProducer)value).getNavigatableData();
    }
    return null;
  }

  private static ColumnInfo[] getHotSpotsColumns() {
    final ColumnInfo methodsColumn = new ColumnInfo("Methods") {
      @Override
      public Class getColumnClass() {
        return TreeTableModel.class;
      }

      @Override
      public Object valueOf(final Object o) {
        return o;
      }

      @Override
      public Comparator getComparator() {
        return new FrameNameComparator();
      }
    };

    final ColumnInfo countColumn = new AbstractCallColumnInfo("Cumulative time, ms") {
      @Override
      public Comparator<DefaultMutableTreeNode> getComparator() {
        return new CumulativeTimeComparator();
      }

      @Override
      protected long extractValueFromCallInfo(CallInfo value) {
        return value.getCumulativeTime();
      }
    };

    final ColumnInfo selfColumn = new AbstractCallColumnInfo("Self time, ms") {
      @Override
      public Comparator<DefaultMutableTreeNode> getComparator() {
        return new SelfTimeComparator();
      }

      @Override
      protected long extractValueFromCallInfo(CallInfo value) {
        return value.getSelfTime();
      }
    };

    return new ColumnInfo[]{methodsColumn, countColumn, selfColumn};
  }

  private static class FrameNameComparator extends AbstractTreeNodeComparator {
    @Override
    public int compareInfo(CallInfo a, CallInfo b) {
      return a.getFrameName().compareTo(b.getFrameName());
    }
  }

  private static class CumulativeTimeComparator extends AbstractTreeNodeComparator {
    @Override
    public int compareInfo(CallInfo a, CallInfo b) {
      return MathUtil.sign(b.getCumulativeTime() - a.getCumulativeTime());
    }
  }

  private static class SelfTimeComparator extends AbstractTreeNodeComparator {
    @Override
    public int compareInfo(CallInfo a, CallInfo b) {
      return MathUtil.sign(b.getSelfTime() - a.getSelfTime());
    }
  }

  private static abstract class AbstractTreeNodeComparator implements Comparator<DefaultMutableTreeNode> {
    @Override
    public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
      CallInfo ci1 = o1.getUserObject() instanceof CallInfo ? (CallInfo)o1.getUserObject() : null;
      CallInfo ci2 = o2.getUserObject() instanceof CallInfo ? (CallInfo)o2.getUserObject() : null;
      return compareInfo(ci1, ci2);
    }

    protected abstract int compareInfo(CallInfo ci1, CallInfo ci2);
  }

  private static abstract class AbstractCallColumnInfo extends ColumnInfo<DefaultMutableTreeNode, String> {
    private static final long MS_IN_MICROSECOND = 1000;

    public AbstractCallColumnInfo(String name) {
      super(name);
    }

    @Override
    public String valueOf(DefaultMutableTreeNode o) {
      final MergedCallNode node = o instanceof MergedCallNode ? (MergedCallNode)o : null;
      final CallInfo info = node != null ? node.getCallInfo() : null;
      return info != null ? getDisplayValue(extractValueFromCallInfo(info)) : "";
    }

    protected abstract long extractValueFromCallInfo(CallInfo value);

    private static String getDisplayValue(long value) {
      if (value > 1000) {
        return Long.toString(value / MS_IN_MICROSECOND);
      }
      return Double.toString(value / (double)MS_IN_MICROSECOND);
    }
  }
}
