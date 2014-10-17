package com.jetbrains.lang.dart.ide.hierarchy;

import com.intellij.ide.hierarchy.HierarchyBrowserManager;
import com.intellij.ide.util.treeView.AlphaComparator;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;

import java.util.Comparator;

public class DartHierarchyUtil {
  private static final Comparator<NodeDescriptor> NODE_DESCRIPTOR_COMPARATOR = new Comparator<NodeDescriptor>() {
    public int compare(final NodeDescriptor first, final NodeDescriptor second) {
      return first.getIndex() - second.getIndex();
    }
  };

  private DartHierarchyUtil() {
  }

  public static Comparator<NodeDescriptor> getComparator(Project project) {
    if (HierarchyBrowserManager.getInstance(project).getState().SORT_ALPHABETICALLY) {
      return AlphaComparator.INSTANCE;
    }
    else {
      return NODE_DESCRIPTOR_COMPARATOR;
    }
  }
}
