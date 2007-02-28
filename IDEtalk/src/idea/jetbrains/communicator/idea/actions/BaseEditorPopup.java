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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import static jetbrains.communicator.idea.actions.ActionUtil.*;
import jetbrains.communicator.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kir
 */
public abstract class BaseEditorPopup extends ActionGroup {

  protected BaseEditorPopup() {
    setPopup(true);
  }

  protected abstract String getActionDescription(User user, VirtualFile file);
  protected abstract void doActionCommand(User user, VirtualFile file, Editor editor);
  protected abstract boolean shouldAddUserToChoiceList(User user);



  public void update(AnActionEvent e) {
    super.update(e);
    e.getPresentation().setEnabled(getEditor(e) != null &&
            getFile(e) != null &&
            getUserModel().getAllUsers().length > 0);
  }

  public AnAction[] getChildren(AnActionEvent e) {

    if (e == null) return EMPTY_ARRAY;

    final Editor editor = getEditor(e);
    final VirtualFile file = getFile(e);


    List<AnAction> result = new ArrayList<AnAction>();
    final UserModel userModel = getUserModel();
    String[] groups = userModel.getGroups();
    List<String> groupsWithUsers = new ArrayList<String>();
    for (String group : groups) {
      if (userModel.getUsers(group).length > 0) {
        groupsWithUsers.add(group);
      }
    }

    if (groupsWithUsers.size() == 1 ) {
      User[] users = userModel.getUsers(groupsWithUsers.get(0));
      fillWithUserActions(users, result, file, editor);
    }
    else {
      for (String groupsWithUser : groupsWithUsers) {

        ActionGroup actionGroup =
            createGroupWithUsersActionGroup(groupsWithUser, userModel, file, editor);

        result.add(actionGroup);
      }
    }
    return result.toArray(new AnAction[result.size()]);
  }

  private ActionGroup createGroupWithUsersActionGroup(final String group, final UserModel userModel, final VirtualFile file, final Editor editor) {
    List<AnAction> users = new ArrayList<AnAction>();
    User[] groupUsers = userModel.getUsers(group);
    fillWithUserActions(groupUsers, users, file, editor);
    final AnAction[] actions = users.toArray(new AnAction[users.size()]);

    return new ActionGroup(group, true) {
      public AnAction[] getChildren(AnActionEvent e) {
        return actions;
      }
    };
  }

  private void fillWithUserActions(User[] users, List<AnAction> result, final VirtualFile file, final Editor editor) {
    for (final User user : users) {
      if (shouldAddUserToChoiceList(user)) {
        result.add(new AnAction(user.getDisplayName()) {
          public void actionPerformed(AnActionEvent e) {
            doActionCommand(user, file, editor);
          }

          public void update(AnActionEvent e) {
            super.update(e);
            e.getPresentation().setDescription(getActionDescription(user, file));
            e.getPresentation().setIcon(UIUtil.getUserIcon(user));
          }
        });
      }
    }
  }

}
