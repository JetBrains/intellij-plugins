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
import com.intellij.ui.PlaceProvider;
import com.intellij.ui.components.labels.LinkLabel;
import jetbrains.communicator.core.users.User;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author Kir
 */
class ComponentConsoleView implements ConsoleView, PlaceProvider<String> {
  public static final int MAX_LENGTH = 200;
  private final JPanel myPanel = new JPanel();
  private int myLength;
  private final Project myProject;

  ComponentConsoleView(final User user, final Project project) {
    myPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 2, 2));
    myPanel.setOpaque(false);
    myProject = project;
    myPanel.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        IDEtalkMessagesWindow messagesWindow = project.getComponent(IDEtalkMessagesWindow.class);
        messagesWindow.expandToolWindow();
        messagesWindow.showUserTabAndRequestFocus(user);
      }
    });
  }

  @Override
  public void addMessageFilter(Filter filter) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void attachToProcess(ProcessHandler processHandler) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean canPause() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void clear() {
    myPanel.removeAll();
  }

  @Override
  public void allowHeavyFilters() {
  }

  @Override
  public int getContentSize() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean hasDeferredOutput() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public boolean isOutputPaused() {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void performWhenNoDeferredOutput(Runnable runnable) {
    runnable.run();
  }

  @Override
  public void print(@NotNull String s, @NotNull ConsoleViewContentType contentType) {
    if (myLength >= MAX_LENGTH) return;

    String updated = updateText(s);
    String[] words = updated.split("[ \t\n\r]+");

    for (String word : words) {
      JLabel comp = new JLabel(word);
      comp.setForeground(contentType.getAttributes().getForegroundColor());
      myPanel.add(comp);
    }
  }

  @Override
  public void printHyperlink(String s, final HyperlinkInfo info) {
    if (myLength >= MAX_LENGTH) return;

    myPanel.add((LinkLabel)LinkLabel.create(linkText(s), () -> info.navigate(myProject)));
  }

  private static String linkText(String s) {
    if (s.length() > 30) {
      return s.substring(0, 30) + "...";
    }
    return s;
  }

  private String updateText(@NotNull String s) {
    if (myLength + s.length() > MAX_LENGTH) {
      s = s.substring(0, MAX_LENGTH - myLength) + "...";
    }
    myLength += s.length();
    return s;
  }

  @Override
  public void scrollTo(int offset) {
  }

  @Override
  public void setHelpId(String helpId) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void setOutputPaused(boolean value) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void dispose() {
  }

  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public JComponent getPreferredFocusableComponent() {
    return myPanel;
  }

  public ActionGroup getActions() {
    return null;
  }

  @Override
  public String getPlace() {
    return null;
  }

  @Override
  @NotNull
  public AnAction[] createConsoleActions() {
    return AnAction.EMPTY_ARRAY;
  }
}
