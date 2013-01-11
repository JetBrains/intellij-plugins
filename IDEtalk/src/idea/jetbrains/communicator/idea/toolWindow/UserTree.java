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

import com.intellij.openapi.util.SystemInfo;
import com.intellij.ui.ScreenUtil;
import com.intellij.util.ui.tree.WideSelectionTreeUI;
import jetbrains.communicator.commands.SendMessageCommand;
import jetbrains.communicator.core.Pico;
import jetbrains.communicator.core.commands.UserCommand;
import jetbrains.communicator.core.dispatcher.LocalMessageDispatcher;
import jetbrains.communicator.core.dispatcher.Message;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.IDEAFacade;
import jetbrains.communicator.idea.IdeaLocalMessage;
import jetbrains.communicator.idea.actions.BaseAction;
import jetbrains.communicator.util.KirTree;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.TreeUtils;
import jetbrains.communicator.util.UIUtil;
import org.picocontainer.MutablePicoContainer;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.intellij.util.ui.UIUtil.isUnderAquaLookAndFeel;

/**
 * @author Kir
 */
public class UserTree extends KirTree {
  private final LocalMessageDispatcher myLocalMessageDispatcher;
  private boolean myDelivered;

  private final MyTreeUI myUi;
  private final TreeDragListener myDragListener;

  public UserTree(LocalMessageDispatcher localMessageDispatcher) {

    IDEAFacade.installIdeaTreeActions(this);

    myLocalMessageDispatcher = localMessageDispatcher;

    setEditable(true);
    getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0), "startEditing");

    myDragListener = new TreeDragListener();

    myUi = createUI();
    setUI((BasicTreeUI)myUi);

    if (Pico.isLocalTesting()) {
      addMouseListener(new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          super.mousePressed(e);
          if (e.getClickCount() == 3) {
            throw new RuntimeException();
          }
        }
      });
    }
  }

  private MyTreeUI createUI() {
    if (SystemInfo.isMac  && isUnderAquaLookAndFeel()) {
      return new MyWideSelectionTreeUIImpl();
    }
    return new MyBasicTreeUIImpl();
  }

  @Override
  protected boolean isCustomUI() {
    return true;
  }

  public void addNotify() {
    super.addNotify();
    addMouseListener(myDragListener);
    addMouseMotionListener(myDragListener);
  }

  public void removeNotify() {
    removeMouseListener(myDragListener);
    removeMouseMotionListener(myDragListener);

    if (ScreenUtil.isStandardAddRemoveNotify(this))
      UIUtil.removeListenersToPreventMemoryLeak(myUi.getRendererPane());
    super.removeNotify();
  }


  public void updateUI() {
    super.updateUI();
    if (myUi != null) {
      setUI((BasicTreeUI)myUi);
    }
  }

  public String getToolTipText(MouseEvent e) {
    TreePath pathForLocation = getPathForLocation(e.getX(), e.getY());
    if (pathForLocation != null) {
      Object userObject = TreeUtils.getUserObject(pathForLocation);
      if (userObject instanceof User) {
        User user = (User) userObject;
        StringBuffer result = new StringBuffer();
        if (!user.getName().equals(user.getDisplayName())) {
          result.append(StringUtil.getMsg("user.tooltip", user.getName())).append("\n");
        }

        Message[] pendingMessages = myLocalMessageDispatcher.getPendingMessages(user);
        if (pendingMessages.length > 0) {
          IdeaLocalMessage ideaLocalMessage = (IdeaLocalMessage) pendingMessages[0];
          result.append(ideaLocalMessage.getTitle());
          if (result.length() > 0 && ideaLocalMessage.getComment().length() > 0) {
            result.append(": ");
          }
          result.append(ideaLocalMessage.getComment());
        }
        return result.length() == 0 ? super.getToolTipText(e) : result.toString() ;
      }
    }

    return super.getToolTipText(e);
  }

  @SuppressWarnings({"RefusedBequest"})
  protected void onDblClick(TreePath path, Object pathComponent, MouseEvent e) {
    Object userObject = TreeUtils.getUserObject(path);
    if (userObject instanceof User && !myDelivered) {
      e.consume();
      invokeSendMessageAction();
    }
  }

  protected void onEnter() {
    super.onEnter();
    invokeSendMessageAction();
  }

  protected void onClick(TreePath path, Object pathComponent, MouseEvent e) {
    super.onClick(path, pathComponent, e);

    if (e.isShiftDown() || e.isControlDown() || e.isAltDown()) return;

    myDelivered = false;
    Object userObject = TreeUtils.getUserObject(pathComponent);
    if (userObject instanceof User) {
      User user = (User) userObject;
      deliverLocalMessage(user, path, e);
    }
  }

  private void deliverLocalMessage(User user, TreePath path, MouseEvent e) {
    Message[] pendingMessages = myLocalMessageDispatcher.getPendingMessages(user);
    if (pendingMessages.length > 0) {
      IdeaLocalMessage message = (IdeaLocalMessage) pendingMessages[0];
      if (myLocalMessageDispatcher.sendNow(user, message)) {
        myDelivered = true;
        e.consume();
        ((MyTreeUI) getUI()).invalidatePath(path);
        treeDidChange();
      }
    }
  }

  protected void invokeSendMessageAction() {
    MutablePicoContainer container = BaseAction.getContainer(this);
    UserCommand command = Pico.getCommandManager().getCommand(SendMessageCommand.class, container);
    if (command.isEnabled()) {
      command.execute();
    }
  }

  public TreePath getPathForUser(User user) {
    TreeNode node = TreeUtils.findNodeWithObject((DefaultMutableTreeNode) getModel().getRoot(), user);
    return TreeUtils.getPathFromRoot(node);
  }

  public TreePath expandUserNode(User user) {
    TreePath objectPath = getPathForUser(user);
    if (objectPath != null) {
      expandPath(objectPath.getParentPath());
    }

    return objectPath;
  }

  public interface MyTreeUI {
    void invalidatePath(TreePath path);
    Container getRendererPane();
  }

  public static class MyBasicTreeUIImpl extends BasicTreeUI implements MyTreeUI {
    public void invalidatePath(TreePath path) {
      treeState.invalidatePathBounds(path);
    }

    public Container getRendererPane() {
      return rendererPane;
    }
  }

  public static class MyWideSelectionTreeUIImpl extends WideSelectionTreeUI implements MyTreeUI {
    public void invalidatePath(TreePath path) {
      treeState.invalidatePathBounds(path);
    }

    public Container getRendererPane() {
      return rendererPane;
    }
  }
}
