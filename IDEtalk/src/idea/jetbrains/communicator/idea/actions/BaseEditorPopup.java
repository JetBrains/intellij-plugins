// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea.actions;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.vfs.VirtualFile;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static jetbrains.communicator.idea.actions.ActionUtil.*;

/**
 * @author Kir
 */
public abstract class BaseEditorPopup extends ActionGroup implements DumbAware {

  protected BaseEditorPopup() {
    setPopup(true);
  }

  protected abstract String getActionDescription(User user, VirtualFile file);
  protected abstract void doActionCommand(User user, VirtualFile file, Editor editor);
  protected abstract boolean shouldAddUserToChoiceList(User user);



  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    e.getPresentation().setEnabled(getEditor(e) != null &&
            getFile(e) != null &&
            getUserModel().getAllUsers().length > 0);
  }

  @Override
  public AnAction @NotNull [] getChildren(AnActionEvent e) {

    if (e == null) return EMPTY_ARRAY;

    final Editor editor = getEditor(e);
    final VirtualFile file = getFile(e);

    if (file == null || editor == null) return EMPTY_ARRAY;


    List<AnAction> result = new ArrayList<>();
    final UserModel userModel = getUserModel();
    String[] groups = userModel.getGroups();
    List<String> groupsWithUsers = new ArrayList<>();
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
    return result.toArray(AnAction.EMPTY_ARRAY);
  }

  private ActionGroup createGroupWithUsersActionGroup(final String group, final UserModel userModel, final VirtualFile file, final Editor editor) {
    List<AnAction> users = new ArrayList<>();
    User[] groupUsers = userModel.getUsers(group);
    fillWithUserActions(groupUsers, users, file, editor);
    final AnAction[] actions = users.toArray(AnAction.EMPTY_ARRAY);

    return new ActionGroup(group, true) {
      @Override
      public AnAction @NotNull [] getChildren(AnActionEvent e) {
        return actions;
      }
    };
  }

  private void fillWithUserActions(User[] users, List<AnAction> result, final VirtualFile file, final Editor editor) {
    for (final User user : users) {
      if (shouldAddUserToChoiceList(user)) {
        result.add(new AnAction(user.getDisplayName()) {
          @Override
          public void actionPerformed(@NotNull AnActionEvent e) {
            doActionCommand(user, file, editor);
          }

          @Override
          public void update(@NotNull AnActionEvent e) {
            e.getPresentation().setDescription(getActionDescription(user, file));
            e.getPresentation().setIcon(user.getIcon());
          }
        });
      }
    }
  }

}
