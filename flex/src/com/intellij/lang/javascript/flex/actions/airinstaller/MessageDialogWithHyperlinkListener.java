package com.intellij.lang.javascript.flex.actions.airinstaller;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;

import javax.swing.*;
import javax.swing.event.HyperlinkListener;

public class MessageDialogWithHyperlinkListener extends DialogWrapper {
  private JPanel myMainPanel;
  private JLabel myIconPlaceholder;
  private JTextPane myTextPane;

  protected MessageDialogWithHyperlinkListener(final Project project,
                                               final String title,
                                               final Icon icon,
                                               final String message) {
    super(project);

    setTitle(title);
    myIconPlaceholder.setText("");
    myIconPlaceholder.setIcon(icon);
    Messages.configureMessagePaneUi(myTextPane, message);
    for (final HyperlinkListener listener : myTextPane.getHyperlinkListeners()) {
      myTextPane.removeHyperlinkListener(listener);
    }

    init();
  }

  protected Action[] createActions() {
    return new Action[]{getOKAction()};
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  public void addHyperlinkListener(final HyperlinkListener listener) {
    myTextPane.addHyperlinkListener(listener);
  }
}
