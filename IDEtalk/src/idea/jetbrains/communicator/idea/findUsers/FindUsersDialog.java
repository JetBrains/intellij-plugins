// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
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
      @Override
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

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myGroups;
  }

  private void updateButtonStatus() {
    getOKAction().setEnabled(getSelectedUsers().size() > 0);
  }

  @Override
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
