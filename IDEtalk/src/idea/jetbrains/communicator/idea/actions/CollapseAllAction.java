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
package jetbrains.communicator.idea.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import jetbrains.communicator.idea.toolWindow.UserListComponentImpl;
import jetbrains.communicator.util.TreeUtils;

import javax.swing.*;

/**
 * @author Kir Maximov
 */
public class CollapseAllAction extends AnAction {
  private JTree myTree;

  public CollapseAllAction() {
  }

  public CollapseAllAction(JTree userTree) {
    myTree = userTree;
    getTemplatePresentation().setIcon(AllIcons.Actions.Collapseall);
  }

  public void update(AnActionEvent anActionEvent) {
    super.update(anActionEvent);
    anActionEvent.getPresentation().setIcon(AllIcons.Actions.Collapseall);
  }

  public void actionPerformed(AnActionEvent e) {
    TreeUtils.collapseAll(getTree(e));
  }

  private JTree getTree(AnActionEvent e) {
    if (myTree != null) {
      return myTree;
    }
    UserListComponentImpl userListComponent = ((UserListComponentImpl) BaseAction.getContainer(e).getComponentInstanceOfType(UserListComponentImpl.class));
    return userListComponent.getTree();
  }
}
