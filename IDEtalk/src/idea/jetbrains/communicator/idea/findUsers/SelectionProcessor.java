// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.findUsers;

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.util.KirTree;
import jetbrains.communicator.util.TreeUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

import static com.intellij.util.ui.tree.TreeUtil.collectSelectedPaths;

/**
 * @author Kir
 */
class SelectionProcessor {
  private final KirTree myUserTree;
  private final JComboBox myGroups;

  SelectionProcessor(KirTree userTree, JComboBox groups, String[] strings) {
    myUserTree = userTree;
    myGroups = groups;

    myGroups.setEditable(true);

    myUserTree.setSelectionRow(0);
    myUserTree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        TreePath[] paths = e.getPaths();
        for (TreePath path : paths) {
          if (myUserTree.isPathSelected(path)) {
            Object userObject = TreeUtils.getUserObject(path);
            if (userObject instanceof User) {
              removeGroupSelection(path);
            } else { // Group is selected
              removeSelectionOfGroupUsers(path);
            }
          }
        }
        updateGroupSelector();
      }
    });


    Vector<String> groupsItems = new Vector<>(Arrays.asList(strings));
    Collections.sort(groupsItems);
    groupsItems.add(0, UserModel.AUTO_GROUP);
    myGroups.setModel(new DefaultComboBoxModel(groupsItems));
    myGroups.setSelectedItem(UserModel.AUTO_GROUP);

    updateGroupSelector();
  }

  private void updateGroupSelector() {
    Set<String> projects = new HashSet<>();
    for (User user : getSelectedUsers()) {
      projects.addAll(Arrays.asList(user.getProjects()));
    }
    if (projects.size() == 0) {
      myGroups.setSelectedItem(UserModel.DEFAULT_GROUP);
    }
    else if (projects.size() == 1) {
      myGroups.setSelectedItem(projects.iterator().next());
    }
    else {
      myGroups.setSelectedItem(UserModel.AUTO_GROUP);
    }
  }

  private void removeSelectionOfGroupUsers(TreePath path) {
    TreeNode treeNode = ((TreeNode) path.getLastPathComponent());
    Enumeration children = treeNode.children();
    while (children.hasMoreElements()) {
      TreeNode node = (TreeNode) children.nextElement();
      myUserTree.removeSelectionPath(TreeUtils.getPathFromRoot(node));
    }
  }

  private void removeGroupSelection(TreePath path) {
    myUserTree.removeSelectionPath(path.getParentPath());
  }

  public Set<User> getSelectedUsers() {
    Set<User> result = new HashSet<>();
    for (TreePath selectionPath : collectSelectedPaths(myUserTree)) {
      Object userObject = TreeUtils.getUserObject(selectionPath);
      if (userObject instanceof User) {
        result.add((User) userObject);
      } else { // group
        TreeNode treeNode = ((TreeNode) selectionPath.getLastPathComponent());
        Enumeration enumeration = treeNode.children();
        while (enumeration.hasMoreElements()) {
          TreeNode node = (TreeNode) enumeration.nextElement();
          result.add((User) ((DefaultMutableTreeNode) node).getUserObject());
        }
      }
    }
    return result;
  }

}
