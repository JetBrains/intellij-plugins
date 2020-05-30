// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.util.ui.tree.TreeUtil.EMPTY_TREE_PATH;

/**
 * @author Kir Maximov
 */
@SuppressWarnings({"unchecked"})
public class TreeState {
  private final Set mySelectedNodes = new HashSet();
  private final Set myExpandedNodes = new HashSet();

  @SuppressWarnings("UnusedDeclaration")
  public TreeState() {
  }

  public TreeState(JTree tree) {
    saveTreeSelection(tree);
    saveExpandedNodes(tree);
  }

  private void saveExpandedNodes(JTree tree) {
    int rowCount = tree.getRowCount();
    for (int i = 0; i < rowCount; i++) {
      TreePath path = tree.getPathForRow(i);
      if (tree.isExpanded(path)) {
        myExpandedNodes.add(getObject(path));
      }
    }
  }

  private void saveTreeSelection(JTree tree) {
    TreePath[] selectionPaths = tree.getSelectionPaths();
    if (selectionPaths != null) {
      for (TreePath path : selectionPaths) {
        mySelectedNodes.add(getObject(path));
      }
    }
  }

  public void restore(JTree tree) {
    restoreExpandedNodes(tree);
    restoreSelection(tree);
  }

  private void restoreExpandedNodes(JTree tree) {
    for (Object o : myExpandedNodes) {
      DefaultMutableTreeNode node = TreeUtils.findNodeWithObject((DefaultMutableTreeNode) tree.getModel().getRoot(), o);
      if (node != null) {
        tree.expandPath(TreeUtils.getPathFromRoot(node));
      }
    }
  }

  private void restoreSelection(JTree tree) {
    List<TreePath> selectedPaths = new ArrayList<>();
    for (Object o : mySelectedNodes) {
      DefaultMutableTreeNode node = TreeUtils.findNodeWithObject((DefaultMutableTreeNode) tree.getModel().getRoot(), o);
      if (node != null) {
        selectedPaths.add(TreeUtils.getPathFromRoot(node));
      }
    }
    tree.setSelectionPaths(selectedPaths.toArray(EMPTY_TREE_PATH));
  }

  private static Object getObject(TreePath path) {
    return ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
  }

  public void addReplacement(Object oldValue, Object newValue) {
    if (myExpandedNodes.contains(oldValue)) {
      myExpandedNodes.add(newValue);
    }

    if (mySelectedNodes.contains(oldValue)) {
      mySelectedNodes.add(newValue);
    }
  }
}
