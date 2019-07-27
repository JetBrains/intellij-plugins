// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.idea.toolWindow.UserListComponentImpl;
import jetbrains.communicator.util.TreeUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public class ExpandAllAction extends AnAction {
  private JTree myTree;

  public ExpandAllAction() {
  }

  public ExpandAllAction(JTree userTree) {
    myTree = userTree;
    getTemplatePresentation().setIcon(AllIcons.Actions.Expandall);
  }

  @Override
  public void update(@NotNull AnActionEvent anActionEvent) {
    anActionEvent.getPresentation().setIcon(AllIcons.Actions.Expandall);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    TreeUtils.expandAll(getTree(e));
  }

  private JTree getTree(AnActionEvent e) {
    if (myTree != null) {
      return myTree;
    }
    UserListComponentImpl userListComponent = ((UserListComponentImpl) BaseAction.getContainer(e).getComponentInstanceOfType(UserListComponentImpl.class));
    return userListComponent.getTree();
  }
}
