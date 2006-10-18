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
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.components.labels.BoldLabel;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.StringUtil;
import jetbrains.communicator.util.icons.CompositeIcon;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * @author Kir
 */
public abstract class BaseIncomingLocalMessage extends BaseLocalMessage implements IdeaLocalMessage {

  public BaseIncomingLocalMessage(String comment, Date when) {
    super(comment, when);
  }

  protected abstract void outputMessage(ConsoleView consoleView);

  public void customizeTreeNode(SimpleColoredComponent label, int refreshCounter) {
    CompositeIcon newIcon = new CompositeIcon(label.getIcon(), getMessageIcon(refreshCounter));
    label.setIcon(newIcon);
  }

  public JComponent getPopupComponent(User user, Project project) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new BoldLabel(StringUtil.getMsg("0.from.1", getTitle(), user.getDisplayName())), BorderLayout.NORTH);
    ComponentConsoleView componentConsoleView = new ComponentConsoleView(user, project);
    outputMessage(componentConsoleView);
    panel.add(componentConsoleView.getComponent());
    return panel;
  }

  public ConsoleMessage createConsoleMessage(final User user) {
    return new ConsoleMessageImpl(user) {
      public void printMessage(Project project, ConsoleView console) {
        outputMessage(console);
      }
    };
  }

  protected ConsoleViewContentType getTextAttributes() {
    return ConsoleViewContentType.NORMAL_OUTPUT;
  }

  private abstract class ConsoleMessageImpl implements ConsoleMessage {
    private final User myUser;

    ConsoleMessageImpl(User user) {
      myUser = user;
    }

    public Date getWhen() {
      return BaseIncomingLocalMessage.this.getWhen();
    }

    public String getTitle() {
      return ConsoleUtil.getHeader(BaseIncomingLocalMessage.this.getTitle(),
          getUser(), getWhen());
    }

    public User getUser() {
      return myUser;
    }

    public String getUsername() {
      return myUser.getDisplayName();
    }

  }
}
