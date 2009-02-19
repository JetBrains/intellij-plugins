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

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.labels.LinkLabel;
import com.intellij.ui.components.labels.LinkListener;
import jetbrains.communicator.core.users.User;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Kir
 */
class ComponentConsoleView implements ConsoleView {
  public static final int MAX_LENGTH = 200;
  private final JPanel myPanel = new JPanel();
  private int myLength;
  private final Project myProject;

  ComponentConsoleView(final User user, final Project project) {
    myPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    myPanel.setOpaque(false);
    myProject = project;
    myPanel.addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        IDEtalkMessagesWindow messagesWindow = project.getComponent(IDEtalkMessagesWindow.class);
        messagesWindow.expandToolWindow();
        messagesWindow.showUserTabAndRequestFocus(user);
      }
    });
  }

  public void addMessageFilter(Filter filter) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void attachToProcess(ProcessHandler processHandler) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public boolean canPause() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void clear() {
    myPanel.removeAll();
  }

  public int getContentSize() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public boolean hasDeferredOutput() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public boolean isOutputPaused() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void performWhenNoDeferredOutput(Runnable runnable) {
    runnable.run();
  }

  public void print(String s, ConsoleViewContentType contentType) {
    if (myLength >= MAX_LENGTH) return;

    String updated = updateText(s);
    String[] words = updated.split("[ \t\n\r]+");

    for (String word : words) {
      JLabel comp = new JLabel(word);
      comp.setForeground(contentType.getAttributes().getForegroundColor());
      myPanel.add(comp);
    }

  }

  public void printHyperlink(String s, final HyperlinkInfo info) {
    if (myLength >= MAX_LENGTH) return;

    LinkLabel linkLabel = new LinkLabel(linkText(s), null, new LinkListener() {
      public void linkSelected(LinkLabel aSource, Object aLinkData) {
        info.navigate(myProject);
      }
    });

    myPanel.add(linkLabel);
  }

  private String linkText(String s) {
    if (s.length() > 30) {
      return s.substring(0, 30) + "...";
    }
    return s;
  }

  private String updateText(String s) {
    if (myLength + s.length() > MAX_LENGTH) {
      s = s.substring(0, MAX_LENGTH - myLength) + "...";
    }
    myLength += s.length();
    return s;
  }

  public void scrollTo(int offset) {
  }

  public void setHelpId(String helpId) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void setOutputPaused(boolean value) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  public void dispose() {
  }

  public JComponent getComponent() {
    return myPanel;
  }

  public JComponent getPreferredFocusableComponent() {
    return myPanel;
  }

  public ActionGroup getActions() {
    return null;
  }

  public String getPlace() {
    return null;
  }

  public JComponent getSearchComponent() {
    return null;
  }
  @NotNull
public AnAction[] createUpDownStacktraceActions() {
  return AnAction.EMPTY_ARRAY;
}
}
