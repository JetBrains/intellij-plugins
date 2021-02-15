// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package jetbrains.communicator.idea;

import com.intellij.execution.filters.Filter;
import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.ActionLink;
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
  private static final int MAX_LENGTH = 200;
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
  public void addMessageFilter(@NotNull Filter filter) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void attachToProcess(@NotNull ProcessHandler processHandler) {
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
  public void performWhenNoDeferredOutput(@NotNull Runnable runnable) {
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
  public void printHyperlink(@NotNull String s, final HyperlinkInfo info) {
    if (myLength >= MAX_LENGTH) return;

    myPanel.add(new ActionLink(linkText(s), e -> { info.navigate(myProject); }));
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
  public void setHelpId(@NotNull String helpId) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void setOutputPaused(boolean value) {
    throw new UnsupportedOperationException("Not implemented in " + getClass().getName());
  }

  @Override
  public void dispose() {
  }

  @NotNull
  @Override
  public JComponent getComponent() {
    return myPanel;
  }

  @Override
  public JComponent getPreferredFocusableComponent() {
    return myPanel;
  }

  @Override
  public AnAction @NotNull [] createConsoleActions() {
    return AnAction.EMPTY_ARRAY;
  }
}
