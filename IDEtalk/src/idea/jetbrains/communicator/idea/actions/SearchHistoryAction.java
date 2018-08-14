// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.commands.SearchHistoryCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import jetbrains.communicator.idea.messagesWindow.IDEtalkMessagesWindowImpl;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class SearchHistoryAction extends BaseAction<SearchHistoryCommand> {

  public SearchHistoryAction() {
    super(SearchHistoryCommand.class);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);

    Project project = getProject(e);
    MutablePicoContainer container = getContainer(project);
    SearchHistoryCommand command = getCommand(e);
    User selectedUser;
    if (container != null && project != null && command != null) {

      boolean focused = false;
      if (IDEtalkMessagesWindowImpl.PLACE_TOOLBAR.equals(e.getPlace())) {
        IDEtalkMessagesWindow messagesWindow = project.getComponent(IDEtalkMessagesWindow.class);
        selectedUser = messagesWindow.getSelectedUser();
        focused = messagesWindow.hasFocus();
      }
      else {
        UserListComponent userList = (UserListComponent) container.getComponentInstanceOfType(UserListComponent.class);
        selectedUser = userList.getSelectedUser();
        focused = userList.getComponent().hasFocus();
      }
      command.setUser(selectedUser);

      e.getPresentation().setEnabled(command.isEnabled() && focused);
    }
  }
}
