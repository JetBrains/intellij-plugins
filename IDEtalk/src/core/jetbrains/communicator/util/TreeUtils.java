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

import jetbrains.communicator.core.users.User;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.LinkedList;

/**
 * @author Kir Maximov
 */
public class TreeUtils {
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

  @SuppressWarnings({"CollectionDeclaredAsConcreteClass"})
  public static TreePath getPathFromRoot(TreeNode node) {
    if (node == null) return null;

    LinkedList<TreeNode> path = new LinkedList<TreeNode>();
    path.add(node);
    while (node.getParent() != null) {
      path.addFirst(node.getParent());
      node = node.getParent();
    }
    return new TreePath(path.toArray(new Object[path.size()]));
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
