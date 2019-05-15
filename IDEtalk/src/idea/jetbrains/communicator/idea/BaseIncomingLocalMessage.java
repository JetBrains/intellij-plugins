// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleColoredComponent;
import com.intellij.ui.components.labels.BoldLabel;
import jetbrains.communicator.core.users.User;
import jetbrains.communicator.util.CommunicatorStrings;
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

  @Override
  public void customizeTreeNode(SimpleColoredComponent label, int refreshCounter) {
    CompositeIcon newIcon = new CompositeIcon(label.getIcon(), getMessageIcon(refreshCounter));
    label.setIcon(newIcon);
  }

  @Override
  public JComponent getPopupComponent(User user, Project project) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(new BoldLabel(CommunicatorStrings.getMsg("0.from.1", getTitle(), user.getDisplayName())), BorderLayout.NORTH);
    ComponentConsoleView componentConsoleView = new ComponentConsoleView(user, project);
    outputMessage(componentConsoleView);
    panel.add(componentConsoleView.getComponent());
    return panel;
  }

  @Override
  public ConsoleMessage createConsoleMessage(final User user) {
    return new ConsoleMessageImpl(user) {
      @Override
      public void printMessage(Project project, ConsoleView console) {
        outputMessage(console);
      }
    };
  }

  @Override
  protected ConsoleViewContentType getTextAttributes() {
    return ConsoleViewContentType.NORMAL_OUTPUT;
  }

  private abstract class ConsoleMessageImpl implements ConsoleMessage {
    private final User myUser;

    ConsoleMessageImpl(User user) {
      myUser = user;
    }

    @Override
    public Date getWhen() {
      return BaseIncomingLocalMessage.this.getWhen();
    }

    @Override
    public String getTitle() {
      return ConsoleUtil.getHeader(BaseIncomingLocalMessage.this.getTitle(),
          getUser(), getWhen());
    }

    @Override
    public User getUser() {
      return myUser;
    }

    @Override
    public String getUsername() {
      return myUser.getDisplayName();
    }

  }
}
