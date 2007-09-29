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

import com.intellij.concurrency.JobScheduler;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.messager.Callout;
import com.thoughtworks.xstream.XStream;
import jetbrains.communicator.core.EventVisitor;
import jetbrains.communicator.core.IDEtalkAdapter;
import jetbrains.communicator.core.IDEtalkEvent;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.transport.TransportEvent;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserEvent;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.idea.IdeaLocalMessage;
import jetbrains.communicator.idea.UserTreeRenderer;
import jetbrains.communicator.util.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NonNls;
import org.picocontainer.Disposable;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Kir Maximov
 */
public class UserListComponentImpl implements UserListComponent, Disposable {
  private static final Logger LOG = Logger.getLogger(UserListComponentImpl.class);
  @NonNls
  private static final String TREE_STATE_FILE = "treeState.xml";

  private final IDEFacade myIDEFacade;
  private UserModel myUserModel;
  private LocalMessageDispatcher myLocalMessageDispatcher;

  private final UserTree myTree;
  private UserTree.MyTreeUI myTreeUi;
  private int myRefreshCounter;
  private Timer myTimer4Renderer;
  private IDEtalkAdapter myExpandTreeNodeListener;
  private final XStream myXStream = XMLUtil.createXStream();

  public UserListComponentImpl(UserModel userModel,
                               IDEFacade facade, final LocalMessageDispatcher localMessageDispatcher) {

    myIDEFacade = facade;
    myLocalMessageDispatcher = localMessageDispatcher;
    myUserModel = userModel;

    myTimer4Renderer = new Timer(200, new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (myLocalMessageDispatcher.hasUsersWithMessages()) {
          myRefreshCounter ++;
          if (myRefreshCounter > 4) {
            myRefreshCounter = 0;
          }
          myTree.repaint();
        }
      }
    });

    myTree = new UserTree(localMessageDispatcher);

    myTree.setCellRenderer(createRenderer());
    myTree.setCellEditor(new MyTreeCellEditor());
    myTreeUi = (UserTree.MyTreeUI) myTree.getUI();

    myTree.setTransferHandler(new UserTreeTransferHandler(myUserModel));
    enableDnD();
    myTree.setModel(new UsersTreeModel(myTree, userModel, myLocalMessageDispatcher));

    myExpandTreeNodeListener = new IDEtalkAdapter() {
      public void afterChange(IDEtalkEvent event) {
        event.accept(new EventVisitor(){

          @SuppressWarnings({"RefusedBequest"})
          public void visitTransportEvent(TransportEvent event) {
            User user = event.createUser(myUserModel);
            expandAndRepaintUserNode(user);
          }

          @SuppressWarnings({"RefusedBequest"})
          public void visitUserAdded(UserEvent.Added event) {
            expandAndRepaintUserNode(event.getUser());
          }
        });
      }
    };

    UIUtil.runWhenShown(myTree, new MakeNodeWithMessageVisible());

    myUserModel.getBroadcaster().addListener(myExpandTreeNodeListener);

    readState();
  }

  private void enableDnD() {
    try {
      myTree.setDragEnabled(true);
    } catch (Exception e) {
      LOG.info(e, e);
    }
  }

  public void dispose() {
    myTimer4Renderer.stop();
    TreeModel model = myTree.getModel();
    if (model instanceof Disposable) {
      ((Disposable) model).dispose();
    }
    myUserModel.getBroadcaster().removeListener(myExpandTreeNodeListener);
  }

  private void expandAndRepaintUserNode(final User user) {
    UIUtil.invokeLater(new Runnable() {
      public void run() {
        myTree.expandUserNode(user);
      }
    });
    repaintUserNode(user);
  }

  private void repaintUserNode(final User user) {
    UIUtil.invokeLater(new Runnable() {
      public void run() {
        TreePath path = myTree.getPathForUser(user);
        if (path != null) {
          myTreeUi.invalidatePath(path);
          myTree.revalidate();
          myTree.repaint();
        }
      }
    });
  }

  public void startEditing() {
    myTree.startEditingAtPath(myTree.getSelectionPath());
  }

  public boolean isSingleItemSelected() {
    final TreePath[] selectionPaths = myTree.getSelectionPaths();
    return selectionPaths != null && selectionPaths.length == 1;
  }

  public Container getComponent() {
    return myTree;
  }

  public Object[] getSelectedNodes() {
    if (myTree.isEditing()) return new Object[0];

    final TreePath[] selectionPaths = myTree.getSelectionPaths();
    if (selectionPaths == null) return new Object[0];

    List<Object> result = new ArrayList<Object>();
    for (TreePath selectionPath : selectionPaths) {
      result.add(TreeUtils.getUserObject(selectionPath));
    }
    return result.toArray();
  }

  public void rebuild() {
    ((UsersTreeModel) myTree.getModel()).updateTree(null);
  }

  public User getSelectedUser() {
    Object[] nodes = getSelectedNodes();
    if (nodes.length == 1 && nodes[0] instanceof User)
      return (User) nodes[0];
    return null;
  }

  private TreeCellRenderer createRenderer() {
    myTimer4Renderer.start();

    return new MyUserTreeRenderer(myTree);
  }

  public void saveState() {
    TreeState treeState = new TreeState(myTree);
    XMLUtil.toXml(myXStream, myIDEFacade.getCacheDir(), TREE_STATE_FILE, treeState);
  }

  private void readState() {
    TreeState treeState = (TreeState) XMLUtil.fromXml(myXStream, myIDEFacade.getCacheDir(), TREE_STATE_FILE, false);
    if (treeState != null) {
      treeState.restore(myTree);
    }
  }

  public JTree getTree() {
    return myTree;
  }

  private class MyUserTreeRenderer extends UserTreeRenderer {

    MyUserTreeRenderer(JTree tree) {
      super(tree);
    }

    protected void customizeUserNode(User user) {
      super.customizeUserNode(user);
      Message[] pendingMessages = myLocalMessageDispatcher.getPendingMessages(user);
      if (pendingMessages.length > 0) {
        ((IdeaLocalMessage) pendingMessages[0]).customizeTreeNode(this, myRefreshCounter);
      }
    }

    protected SimpleTextAttributes getGroupAttributes(String group) {
      int usersInGroup = myUserModel.getUsers(TreeUtils.getUserObject(group).toString()).length;
      if (usersInGroup == 0) {
        return SimpleTextAttributes.GRAYED_ATTRIBUTES;
      }
      return super.getGroupAttributes(group);
    }
  }

  private class MakeNodeWithMessageVisible implements Runnable {

    MakeNodeWithMessageVisible() {
    }

    public void run() {
      User[] usersWithMessages = myLocalMessageDispatcher.getUsersWithMessages();
      if (usersWithMessages.length > 0) {
        expandAndRepaintUserNode(usersWithMessages[0]);
        TreePath path = myTree.getPathForUser(usersWithMessages[0]);
        myTree.scrollPathToVisible(path);
      }
    }
  }

  private class MyTreeCellEditor extends DefaultTreeCellEditor {
    private User myEditingUser;

    MyTreeCellEditor() {
      super(myTree, new DefaultTreeCellRenderer(), new MyValidatingCellEditor());
    }

    public Component getTreeCellEditorComponent(JTree tree1, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
      myEditingUser = null;
      Object originalValue = TreeUtils.convertValueIfUserNode(value, new UserActionWithValue() {
        public Object execute(User user) {
          myEditingUser = user;
          return user.getDisplayName();
        }
      });
      //noinspection AssignmentToMethodParameter
      value = originalValue;

      Component editor = super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);

      ((MyValidatingCellEditor) realEditor).setOriginalValue(originalValue);
      ((MyValidatingCellEditor) realEditor).setEditingUser(myEditingUser);

      editingComponent.addKeyListener(new KeyAdapter() {
        public void keyPressed(KeyEvent e) {
          super.keyPressed(e);
          if (myTree.isEditing() && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            myTree.cancelEditing();
            UIUtil.requestFocus(myTree);
            e.consume();
          }
        }
      });

      return editor;
    }

    protected boolean shouldStartEditingTimer(EventObject event) {
      return false;
    }

    protected void determineOffset(JTree tree1, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
      TreePath path = tree1.getSelectionPath();
      if (path == null) {
        super.determineOffset(tree1, value, isSelected, expanded, leaf, row);
      } else {
        Object userObject = TreeUtils.getUserObject(path);
        editingIcon = ((JLabel) renderer.getTreeCellRendererComponent(tree1, userObject, isSelected, expanded, leaf, row, false)).getIcon();
        if (editingIcon != null)
          offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
        else
          offset = renderer.getIconTextGap();
      }


    }
  }
  private class MyValidatingCellEditor extends DefaultCellEditor {

    private Object myOriginalValue;
    private User myEditingUser;

    MyValidatingCellEditor() {
      super(new JTextField());
    }

    public void setEditingUser(User editingUser) {
      myEditingUser = editingUser;
    }

    public void setOriginalValue(Object originalValue) {
      myOriginalValue = originalValue;
    }

    public boolean stopCellEditing() {
          String value = (String) getCellEditorValue();
          if (!value.equals(myOriginalValue)) {

            if (myEditingUser == null) {
              if (Arrays.asList(myUserModel.getGroups()).contains(value)) {
                return problem("duplicate.group.name");
              }
            }
            else {
              User[] allUsers = myUserModel.getAllUsers();
              for (User user : allUsers) {
                if (user.getTransportCode().equals(myEditingUser.getTransportCode()) &&
                    user.getDisplayName().equals( value )) {
                  return problem("duplicate.user.name");
                }
              }
            }
          }
          UIUtil.requestFocus(myTree);
          return super.stopCellEditing();
        }

    private boolean problem(final String resourceCode) {
      JobScheduler.getScheduler().schedule(new Runnable(){
        public void run() {
          UIUtil.invokeLater(new Runnable() {
            public void run() {
              Callout.showText(myTree, myTree.getEditingPath(), Callout.SOUTH_WEST,
                  StringUtil.getMsg(resourceCode));
            }
          });
        }
      }, (long)100, TimeUnit.MILLISECONDS);
      return false;
    }

  }
}
