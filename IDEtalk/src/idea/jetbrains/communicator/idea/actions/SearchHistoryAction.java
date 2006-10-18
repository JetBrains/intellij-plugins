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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import jetbrains.communicator.commands.SearchHistoryCommand;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.idea.IDEtalkMessagesWindow;
import jetbrains.communicator.idea.messagesWindow.IDEtalkMessagesWindowImpl;
import org.picocontainer.MutablePicoContainer;

/**
 * @author Kir
 */
public class SearchHistoryAction extends BaseAction<SearchHistoryCommand> {

  public SearchHistoryAction() {
    super(SearchHistoryCommand.class);
  }

  public void update(AnActionEvent e) {
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
