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
package jetbrains.communicator.idea.toolWindow;

import jetbrains.communicator.OptionFlag;
import jetbrains.communicator.core.EventBroadcaster;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.*;
import jetbrains.communicator.util.TreeState;
import jetbrains.communicator.util.UIUtil;
import org.picocontainer.Disposable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 * @author Kir Maximov
 */
@SuppressWarnings({"RefusedBequest"})
public class UsersTreeModel extends DefaultTreeModel implements Disposable {

  private final UserModel myUserModel;
  private EventBroadcaster myBroadcaster;
  private JTree myTree;
  private final IDEtalkAdapter myListener;
  private final LocalMessageDispatcher myLocalMessageDispatcher;

  public UsersTreeModel(JTree tree, UserModel userModel, LocalMessageDispatcher localMessageDispatcher) {
    super(new RootNode(userModel, localMessageDispatcher));
    myUserModel = userModel;
    myLocalMessageDispatcher = localMessageDispatcher;
    myBroadcaster = myUserModel.getBroadcaster();
    myTree = tree;

    myListener = new IDEtalkAdapter() {
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){

          @Override public void visitUserRemoved(UserEvent.Removed event) {
            updateTree(null);
            UIUtil.invokeLater(new Runnable() {
              public void run() {
                if (myTree.getRowCount() > 1) {
                  myTree.setSelectionRow(0);
                }
              }
            });
          }

          @Override public void visitUserEvent(UserEvent event) {
            updateTree(null);
          }

          @Override public void visitGroupEvent(GroupEvent event) {
            updateTree(event);
          }

          @Override public void visitSettingsChanged(SettingsChanged settingsChanged) {
            updateTree(null);
          }

          @Override public void visitTransportEvent(TransportEvent event) {
            updateTree(null);
          }
        });
      }
    };
    myBroadcaster.addListener(myListener);
  }

  void updateTree(final GroupEvent updated) {

    UIUtil.invokeLater(new Runnable() {
      public void run() {
        TreeState state = new TreeState(myTree);
        if (updated instanceof GroupEvent.Updated) {
          GroupEvent.Updated evt = ((GroupEvent.Updated) updated);
          state.addReplacement(evt.getOldGroup(), evt.getNewGroup());
        }

        setRoot(new RootNode(myUserModel, myLocalMessageDispatcher));
        state.restore(myTree);
      }
    });
  }

  public void dispose() {
    myBroadcaster.removeListener(myListener);
  }

  public void valueForPathChanged(TreePath path, Object newValue) {
    DefaultMutableTreeNode aNode = (DefaultMutableTreeNode) path.getLastPathComponent();
    Object userObject = aNode.getUserObject();
    if (userObject instanceof User) {
      User user = (User) userObject;
      user.setDisplayName(newValue.toString(), myUserModel);
      nodeChanged(aNode);
    }
    else { // GROUP rename
      String newGroupName = myUserModel.renameGroup(userObject.toString(), newValue.toString());
      super.valueForPathChanged(path, newGroupName);
    }
  }

  private static class RootNode extends DefaultMutableTreeNode {
    RootNode(UserModel userModel, LocalMessageDispatcher localMessageDispatcher) {
      super(RootNode.class);

      for (String group : userModel.getGroups()) {
        DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode(group, true);
        add(groupNode);

        for (final User user : userModel.getUsers(group)) {
          if (!OptionFlag.OPTION_HIDE_OFFLINE_USERS.isSet() || user.isOnline()
              || 0 != localMessageDispatcher.getPendingMessages(user).length) {
            groupNode.add(new DefaultMutableTreeNode(user, false) {
              public String toString() {
                // For speed search:
                return user.getDisplayName();
              }
            });
          }
        }
        if (groupNode.getChildCount() == 0 && OptionFlag.OPTION_HIDE_OFFLINE_USERS.isSet()) {
          remove(groupNode);
        }
      }
    }
  }
}
