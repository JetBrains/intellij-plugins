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
package jetbrains.communicator.idea.findUsers;

import jetbrains.communicator.util.KirTree;
import jetbrains.communicator.util.TreeUtils;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;
import java.util.*;

/**
 * @author Kir
 */
class SelectionProcessor {
  private KirTree myUserTree;
  private JComboBox myGroups;

  SelectionProcessor(KirTree userTree, JComboBox groups, String[] strings) {
    myUserTree = userTree;
    myGroups = groups;

    myGroups.setEditable(true);

    myUserTree.setSelectionRow(0);
    myUserTree.addTreeSelectionListener(new TreeSelectionListener() {
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


    Vector<String> groupsItems = new Vector<String>(Arrays.asList(strings));
    Collections.sort(groupsItems);
    groupsItems.add(0, UserModel.AUTO_GROUP);
    myGroups.setModel(new DefaultComboBoxModel(groupsItems));
    myGroups.setSelectedItem(UserModel.AUTO_GROUP);

    updateGroupSelector();
  }

  private void updateGroupSelector() {
    Set<String> projects = new HashSet<String>();
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
    TreePath[] selectionPaths = myUserTree.getSelectionPaths();
    if (selectionPaths == null) selectionPaths = new TreePath[0];
    Set<User> result = new HashSet<User>();
    for (TreePath selectionPath : selectionPaths) {
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
