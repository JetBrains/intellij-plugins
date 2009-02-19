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

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.idea.IdeaDialog;
import jetbrains.communicator.idea.UserTreeRenderer;
import jetbrains.communicator.idea.actions.CollapseAllAction;
import jetbrains.communicator.idea.actions.ExpandAllAction;
import jetbrains.communicator.util.KirTree;
import jetbrains.communicator.util.TreeUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * @author Kir
 */
public class FindUsersDialog extends IdeaDialog {

  private JPanel myMainPanel;
  private JComboBox myGroups;
  private KirTree myUserTree;
  private JPanel myToolbarPanel;
  private final SelectionProcessor mySelectionProcessor;

  public FindUsersDialog(final List<User> foundUsers, String[] groups) {
    super(true);

    setTitle(" Find New Users");
    setOKButtonText("Add");

    myUserTree.setModel(new FoundUsersModel(foundUsers));
    myUserTree.setCellRenderer(new UserTreeRenderer(myUserTree));
    TreeUtils.expandAll(myUserTree);

    mySelectionProcessor = new SelectionProcessor(myUserTree, myGroups, groups);
    myUserTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        updateButtonStatus();
      }
    });

    DefaultActionGroup toolbarActions = new DefaultActionGroup();
    toolbarActions.add(new ExpandAllAction(myUserTree));
    toolbarActions.add(new CollapseAllAction(myUserTree));
    myToolbarPanel.setLayout(new BorderLayout());
    myToolbarPanel.add(ActionManager.getInstance().createActionToolbar("toolbar",
    toolbarActions, true).getComponent(), BorderLayout.EAST);

    updateButtonStatus();

    init();
  }

  public JComponent getPreferredFocusedComponent() {
    return myGroups;
  }

  private void updateButtonStatus() {
    getOKAction().setEnabled(getSelectedUsers().size() > 0);
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public Set<User> getSelectedUsers() {
    return mySelectionProcessor.getSelectedUsers();
  }

  public String getGroup() {
    return myGroups.getSelectedItem().toString();
  }

}
