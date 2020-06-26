// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.util;

import com.intellij.util.ArrayUtil;
import jetbrains.communicator.core.users.User;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.LinkedList;

/**
 * @author Kir Maximov
 */
public final class TreeUtils {
  private TreeUtils() {
  }

  public static Object getUserObject(Object value) {
    if (value instanceof DefaultMutableTreeNode) {
      return ((DefaultMutableTreeNode) value).getUserObject();
    }
    return value;
  }

  public static Object getUserObject(TreePath path) {
    Object value = path.getLastPathComponent();
    if (value instanceof DefaultMutableTreeNode) {
      return ((DefaultMutableTreeNode) value).getUserObject();
    }
    return value;
  }

  @Nullable
  public static DefaultMutableTreeNode findNodeWithObject(DefaultMutableTreeNode node, Object userData) {
    if (userData == null) return null;
    if (userData.equals(node.getUserObject())) return node;

    for (int i = 0; i < node.getChildCount(); i++) {
      TreeNode childAt = node.getChildAt(i);
      if (childAt instanceof DefaultMutableTreeNode) {
        DefaultMutableTreeNode mutableTreeNode = (DefaultMutableTreeNode) childAt;
        DefaultMutableTreeNode found = findNodeWithObject(mutableTreeNode, userData);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  @Nullable
  @SuppressWarnings({"CollectionDeclaredAsConcreteClass"})
  public static TreePath getPathFromRoot(TreeNode node) {
    if (node == null) return null;

    LinkedList<TreeNode> path = new LinkedList<>();
    path.add(node);
    while (node.getParent() != null) {
      path.addFirst(node.getParent());
      node = node.getParent();
    }
    return new TreePath(ArrayUtil.toObjectArray(path));
  }

  public static void collapseAll(JTree jTree) {
    for (int i = 0; i < jTree.getRowCount(); i ++) {
      jTree.collapseRow(i);
    }
  }

  public static void expandAll(JTree jTree) {
    for (int i = 0; i < jTree.getRowCount(); i ++) {
      jTree.expandRow(i);
    }
  }

  public static Object convertValueIfUserNode(Object value, UserActionWithValue convertor) {
    Object userObject = getUserObject(value);
    if (userObject instanceof User) {
      User user = (User) userObject;
//noinspection AssignmentToMethodParameter
      value = convertor.execute(user);
    }
    return value;
  }

}
