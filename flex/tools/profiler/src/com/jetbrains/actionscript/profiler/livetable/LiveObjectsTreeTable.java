package com.jetbrains.actionscript.profiler.livetable;

import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;
import com.intellij.util.Function;
import com.intellij.util.ui.ColumnInfo;
import com.jetbrains.actionscript.profiler.base.ColoredSortableTreeTable;
import com.jetbrains.actionscript.profiler.sampler.FrameInfo;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.text.NumberFormat;
import java.util.Comparator;

/**
 * @author: Fedor.Korotkov
 */
public class LiveObjectsTreeTable extends ColoredSortableTreeTable implements DataProvider {
  private Function<FrameInfo, Navigatable> frameLocationResolveFunction;
  private Function<String, Navigatable> classNameLocationResolveFunction;

  public LiveObjectsTreeTable(@Nullable Project project) {
    super(getColumns(), project);
  }

  @Override
  @Nullable
  public Object getData(@NonNls String dataId) {
    if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
      return navigatableSelectedItem();
    }
    return null;
  }

  @Nullable
  private Navigatable navigatableSelectedItem() {
    final TreePath path = getTree().getSelectionPath();
    if (path == null) {
      return null;
    }
    final Object component = path.getLastPathComponent();
    if (component instanceof SizeInfoNode) {
      return resolveNavigatable((SizeInfoNode)component);
    }
    return null;
  }

  @Nullable
  private Navigatable resolveNavigatable(SizeInfoNode node) {
    if (node.isMethod() && frameLocationResolveFunction != null) {
      return frameLocationResolveFunction.fun(node.getFrameInfo());
    }
    else if (classNameLocationResolveFunction != null) {
      return classNameLocationResolveFunction.fun(node.getQName());
    }
    return null;
  }

  public void setFrameLocationResolveFunction(Function<FrameInfo, Navigatable> function) {
    frameLocationResolveFunction = function;
  }

  public void setClassNameLocationResolveFunction(Function<String, Navigatable> function) {
    classNameLocationResolveFunction = function;
  }

  private static ColumnInfo[] getColumns() {
    final ColumnInfo qnameColumn = new ColumnInfo("Class") {
      @Override
      public Class getColumnClass() {
        return TreeTableModel.class;
      }

      @Override
      public Object valueOf(final Object o) {
        return o;
      }

      @Override
      public Comparator<DefaultMutableTreeNode> getComparator() {
        return new AbstractSizeNodeComparator() {
          @Override
          protected int compareInfo(SizeInfoNode si1, SizeInfoNode si2) {
            if (si1.isMethod() && si2.isMethod()) {
              return si1.getFrameInfo().compareTo(si2.getFrameInfo());
            }
            int result = si1.getClassName().compareTo(si2.getClassName());
            if (result != 0) {
              return result;
            }
            return si1.getPackageName().compareTo(si2.getPackageName());
          }
        };
      }
    };

    final ColumnInfo countColumn = new AbstractSizeColumnInfo("Count") {
      @Override
      protected String extractValueFromSizeInfoNode(SizeInfoNode value) {
        return Integer.toString(value.getCount());
      }

      @Override
      public Comparator<DefaultMutableTreeNode> getComparator() {
        return new AbstractSizeNodeComparator() {
          @Override
          protected int compareInfo(SizeInfoNode si1, SizeInfoNode si2) {
            return (int)Math.signum(si2.getCount() - si1.getCount());
          }
        };
      }
    };


    final ColumnInfo sizeColumn = new AbstractSizeColumnInfo("Size, bytes") {
      @Override
      protected String extractValueFromSizeInfoNode(SizeInfoNode value) {
        return NumberFormat.getInstance().format(value.getSize());
      }

      @Override
      public Comparator<DefaultMutableTreeNode> getComparator() {
        return new AbstractSizeNodeComparator() {
          @Override
          protected int compareInfo(SizeInfoNode si1, SizeInfoNode si2) {
            return (int)Math.signum(si2.getSize() - si1.getSize());
          }
        };
      }
    };

    return new ColumnInfo[]{qnameColumn, countColumn, sizeColumn};
  }

  private static abstract class AbstractSizeNodeComparator implements Comparator<DefaultMutableTreeNode> {
    @Override
    public int compare(DefaultMutableTreeNode o1, DefaultMutableTreeNode o2) {
      SizeInfoNode si1 = o1 instanceof SizeInfoNode ? (SizeInfoNode)o1 : null;
      SizeInfoNode si2 = o2 instanceof SizeInfoNode ? (SizeInfoNode)o2 : null;
      return compareInfo(si1, si2);
    }

    protected abstract int compareInfo(SizeInfoNode ci1, SizeInfoNode ci2);
  }

  private static abstract class AbstractSizeColumnInfo extends ColumnInfo<DefaultMutableTreeNode, String> {
    public AbstractSizeColumnInfo(String name) {
      super(name);
    }

    @Override
    public String valueOf(DefaultMutableTreeNode node) {
      SizeInfoNode infoNode = node instanceof SizeInfoNode ? (SizeInfoNode)node : null;
      return infoNode != null ? extractValueFromSizeInfoNode(infoNode) : "";
    }

    protected abstract String extractValueFromSizeInfoNode(SizeInfoNode value);
  }
}
