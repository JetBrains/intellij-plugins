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
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import jetbrains.communicator.core.dispatcher.LocalMessage;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.StringUtil;
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

  public Date getWhen() {
    return myWhen;
  }

  public boolean containsString(String searchString) {
    return StringUtil.containedIn(myComment, searchString);
  }

  protected abstract Icon getIcon();

  public Icon getMessageIcon(int refreshCounter) {
    if (refreshCounter <= 2) {
      return getIcon();
    }
    else {
      return new EmptyIcon(getIcon().getIconWidth(), getIcon().getIconHeight());
    }
  }

  public boolean send(final User user) {
    UIUtil.invokeLater(new Runnable() {
      public void run() {
        showEventInConsoles(user);
      }
    });
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
    Collection<IDEtalkMessagesWindow> result = new HashSet<IDEtalkMessagesWindow>();
    for (Project openProject : openProjects) {
      if (openProject.isOpen() && openProject.isInitialized()) {
        IDEtalkMessagesWindow messagesWindow = openProject.getComponent(IDEtalkMessagesWindow.class);
        result.add(messagesWindow);
      }
    }
    return result.toArray(new IDEtalkMessagesWindow[result.size()]);
  }
}
