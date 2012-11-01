/*
 * Copyright 2000-2006 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.communicator.util;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    List<TreePath> selectedPaths = new ArrayList<TreePath>();
    for (Object o : mySelectedNodes) {
      DefaultMutableTreeNode node = TreeUtils.findNodeWithObject((DefaultMutableTreeNode) tree.getModel().getRoot(), o);
      if (node != null) {
        selectedPaths.add(TreeUtils.getPathFromRoot(node));
      }
    }

    tree.setSelectionPaths(selectedPaths.toArray(new TreePath[selectedPaths.size()]));
  }

  private Object getObject(TreePath path) {
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
