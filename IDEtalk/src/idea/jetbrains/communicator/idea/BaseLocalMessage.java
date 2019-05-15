// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.CommunicatorStrings;
import jetbrains.communicator.util.UIUtil;
import jetbrains.communicator.util.icons.EmptyIcon;

import javax.swing.*;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

/**
 * @author Kir
 */
public abstract class BaseLocalMessage implements LocalMessage {
  protected final String myComment;
  protected final Date myWhen;

  protected BaseLocalMessage(String comment, Date when) {
    myComment = comment;
    myWhen = when;
  }

  public String getComment() {
    return myComment;
  }

  @Override
  public Date getWhen() {
    return myWhen;
  }

  @Override
  public boolean containsString(String searchString) {
    return CommunicatorStrings.containedIn(myComment, searchString);
  }

  protected abstract Icon getIcon();

  @Override
  public Icon getMessageIcon(int refreshCounter) {
    if (refreshCounter <= 2) {
      return getIcon();
    }
    else {
      return new EmptyIcon(getIcon().getIconWidth(), getIcon().getIconHeight());
    }
  }

  @Override
  public boolean send(final User user) {
    UIUtil.invokeLater(() -> showEventInConsoles(user));
    return true;
  }

  private void showEventInConsoles(final User user) {
    for (final IDEtalkMessagesWindow messagesWindow : getMessagesWindows()) {
      messagesWindow.deliverMessage(createConsoleMessage(user));
    }
  }

  public abstract ConsoleMessage createConsoleMessage(User user);

  protected void printComment(ConsoleView consoleView) {
    ConsoleUtil.printMessageIfExists(consoleView, getComment(), getTextAttributes());
  }

  protected abstract ConsoleViewContentType getTextAttributes();

  private static IDEtalkMessagesWindow[] getMessagesWindows() {
    Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
    Collection<IDEtalkMessagesWindow> result = new HashSet<>();
    for (Project openProject : openProjects) {
      if (openProject.isOpen() && openProject.isInitialized()) {
        IDEtalkMessagesWindow messagesWindow = openProject.getComponent(IDEtalkMessagesWindow.class);
        result.add(messagesWindow);
      }
    }
    return result.toArray(new IDEtalkMessagesWindow[0]);
  }
}
