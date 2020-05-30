// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package jetbrains.communicator.commands;

import jetbrains.communicator.core.users.User;
import jetbrains.communicator.core.users.UserModel;
import jetbrains.communicator.ide.IDEFacade;
import jetbrains.communicator.ide.UserListComponent;
import jetbrains.communicator.util.CommunicatorStrings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Kir
 */
public class DeleteCommand extends EnabledWhenFocusedCommand {
  static final String QUESTION_PREFIX = "Delete ";

  private final UserModel myUserModel;
  private final IDEFacade myIDEFacade;

  public DeleteCommand(UserModel userModel, UserListComponent usetListComponent, IDEFacade facade) {
    super(usetListComponent);
    myUserModel = userModel;
    myIDEFacade = facade;
  }

  @Override
  public boolean enabled() {
    return myUserListComponent.getSelectedNodes().length > 0;
  }

  @Override
  public void execute() {
    Object[] selectedNodes = myUserListComponent.getSelectedNodes();

    if (!myIDEFacade.askQuestion("Delete Confirmation", buildQuestion(selectedNodes))) {
      return;
    }

    deleteSelectedUsers(selectedNodes);
    deleteSelectedGroups(selectedNodes);
  }

  private void deleteSelectedGroups(Object[] selectedNodes) {
    for (Object selectedNode : selectedNodes) {
      if (selectedNode instanceof String) {
        myUserModel.removeGroup((String)selectedNode);
      }
    }
  }

  private void deleteSelectedUsers(Object[] selectedNodes) {
    for (Object selectedNode : selectedNodes) {
      if (selectedNode instanceof User) {
        myUserModel.removeUser((User)selectedNode);
      }
    }
  }

  String buildQuestion(Object[] selectedNodes) {
    List usersToDelete = new ArrayList();
    final List groupsToDelete = new ArrayList();
    buildListOfDeletedUsersAndGroups(selectedNodes, usersToDelete, groupsToDelete);

    StringBuffer question = new StringBuffer(QUESTION_PREFIX);

    final boolean hasBothUsersAndGroups = !usersToDelete.isEmpty() && !groupsToDelete.isEmpty();

    appendItems(question, groupsToDelete, "group", hasBothUsersAndGroups,
        new GroupTextExtractor(groupsToDelete, hasBothUsersAndGroups));

    if (hasBothUsersAndGroups) {
      appendAndBetweenGroupsAndUsers(question, groupsToDelete, usersToDelete);
    }

    appendItems(question, usersToDelete, "user", hasBothUsersAndGroups, new ItemTextExtractor() {
      @Override
      public String getText(Object item) {
        return ((User) item).getDisplayName();
      }
    });

    if (hasBothUsersAndGroups && usersToDelete.size() > 1) {
      question.append(" \nfrom other groups");
    }

    question.append('?');
    return question.toString();
  }

  private static void appendAndBetweenGroupsAndUsers(StringBuffer question, List groupsToDelete, List usersToDelete) {
    if (groupsToDelete.size() > 1) {
      question.append(" \n");
    }
    else {
      question.append(' ');
    }
    question.append("and ");
    if (usersToDelete.size() > 1) {
      question.append('\n');
    }
  }

  private static void buildListOfDeletedUsersAndGroups(Object[] selectedNodes, List usersToDelete, List groupsToDelete) {
    for (Object selectedNode : selectedNodes) {
      if (selectedNode instanceof User) {
        usersToDelete.add(selectedNode);
      }
      else if (selectedNode instanceof String) {
        groupsToDelete.add(selectedNode);
      }
    }

    for (Iterator it = usersToDelete.iterator(); it.hasNext();) {
      User user = (User) it.next();
      if (groupsToDelete.contains(user.getGroup())) {
        it.remove();
      }
    }
  }

  private static void appendItems(StringBuffer question, List items, String itemName, boolean useCommasOnly, ItemTextExtractor extractor) {
    if (!items.isEmpty()) {
      CommunicatorStrings.appendItemName(question, itemName, items.size());
      question.append(' ');
      appendCommaSeparated(question, items, extractor);
      appendTail(question, items, extractor, useCommasOnly);
    }
  }

  private static void appendTail(StringBuffer question, List items, ItemTextExtractor extractor, boolean useCommasOnly) {
    if (items.size() >= 2) {
      question.append(extractor.getText(items.get(items.size() - 2)));
      if (items.size() >= 3 || useCommasOnly) {
        question.append(", ");
      }
      else {
        question.append(' ');
      }
      if (!useCommasOnly) {
        question.append("and ");
      }
    }
    question.append(extractor.getText(items.get(items.size() - 1)));
  }

  private static void appendCommaSeparated(StringBuffer question, List items, ItemTextExtractor extractor) {
    for (int i = 0; i < items.size() - 2; i++) {
      String itemText = extractor.getText(items.get(i));
      question.append(itemText).append(", ");
    }
  }

  private interface ItemTextExtractor {
    String getText(Object item);
  }

  private class GroupTextExtractor implements ItemTextExtractor {
    private final List myGroupsToDelete;
    private final boolean myHasBothUsersAndGroups;

    GroupTextExtractor(List groupsToDelete, boolean hasBothUsersAndGroups) {
      myGroupsToDelete = groupsToDelete;
      myHasBothUsersAndGroups = hasBothUsersAndGroups;
    }

    @Override
    public String getText(Object item) {
      int numberOfUsers = myUserModel.getUsers((String) item).length;
      StringBuffer sb = new StringBuffer("\"" + item + '"');
      if (numberOfUsers == 0) {
        return sb.toString();
      }
      if (myGroupsToDelete.size() == 1 && !myHasBothUsersAndGroups) {
        sb.append(" with its ");
        CommunicatorStrings.appendItems(sb, "user", numberOfUsers);
      }
      else {
        sb.append('(');
        CommunicatorStrings.appendItems(sb, "user", numberOfUsers);
        sb.append(')');
      }
      return sb.toString();
    }
  }
}
