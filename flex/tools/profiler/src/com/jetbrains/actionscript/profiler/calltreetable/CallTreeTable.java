package com.jetbrains.actionscript.profiler.calltreetable;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.ui.ColumnInfo;
import com.jetbrains.actionscript.profiler.ProfilerBundle;
import com.jetbrains.actionscript.profiler.base.ColoredSortableTreeTable;
import com.jetbrains.actionscript.profiler.base.NavigatableDataProducer;
import com.jetbrains.actionscript.profiler.vo.CallInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

public class CallTreeTable extends ColoredSortableTreeTable implements DataProvider {
  public CallTreeTable(@Nullable Project project) {
    super(getHotSpotsColumns(), project);
  }

  @Override
  @Nullable
  public Object getData(@NonNls String dataId) {
    if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
      return getSelectedNavigableItem();
    }
    return null;
  }

  @Nullable
  private Navigatable getSelectedNavigableItem() {
    Object value = getSelectedValue();
    if (value instanceof NavigatableDataProducer) {
      return ((NavigatableDataProducer)value).getNavigatable();
    }
    return null;
  }

  @Nullable
  public Object getSelectedValue() {
    int row = getSelectedRow();
    if (row < 0 || row >= getRowCount()) {
      return null;
    }
    int column = Math.max(0, getSelectedColumn());
    return getValueAt(row, column);
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
      return a.getFrameInfo().compareTo(b.getFrameInfo());
    }
  }

  private static class CumulativeTimeComparator extends AbstractTreeNodeComparator {
    @Override
    public int compareInfo(CallInfo a, CallInfo b) {
      return (int)Math.signum(b.getCumulativeTime() - a.getCumulativeTime());
    }
  }

  private static class SelfTimeComparator extends AbstractTreeNodeComparator {
    @Override
    public int compareInfo(CallInfo a, CallInfo b) {
      return (int)Math.signum(b.getSelfTime() - a.getSelfTime());
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
      return ProfilerBundle.message("less.one.ms");
    }
  }
}
