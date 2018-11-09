// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
public class UsersTreeModel extends DefaultTreeModel implements Disposable {

  private final UserModel myUserModel;
  private final EventBroadcaster myBroadcaster;
  private final JTree myTree;
  private final IDEtalkAdapter myListener;
  private final LocalMessageDispatcher myLocalMessageDispatcher;

  public UsersTreeModel(JTree tree, UserModel userModel, LocalMessageDispatcher localMessageDispatcher) {
    super(new RootNode(userModel, localMessageDispatcher));
    myUserModel = userModel;
    myLocalMessageDispatcher = localMessageDispatcher;
    myBroadcaster = myUserModel.getBroadcaster();
    myTree = tree;

    myListener = new IDEtalkAdapter() {
      @Override
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){

          @Override public void visitUserRemoved(UserEvent.Removed event) {
            updateTree(null);
            UIUtil.invokeLater(() -> {
              if (myTree.getRowCount() > 1) {
                myTree.setSelectionRow(0);
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

    UIUtil.invokeLater(() -> {
      TreeState state = new TreeState(myTree);
      if (updated instanceof GroupEvent.Updated) {
        GroupEvent.Updated evt = ((GroupEvent.Updated) updated);
        state.addReplacement(evt.getOldGroup(), evt.getNewGroup());
      }

      setRoot(new RootNode(myUserModel, myLocalMessageDispatcher));
      state.restore(myTree);
    });
  }

  @Override
  public void dispose() {
    myBroadcaster.removeListener(myListener);
  }

  @Override
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
