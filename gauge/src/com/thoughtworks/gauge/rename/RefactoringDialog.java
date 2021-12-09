/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.rename;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorTextField;
import com.thoughtworks.gauge.GaugeBundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public final class RefactoringDialog extends DialogWrapper {
  private EditorTextField inputText;
  private JPanel contentPane;
  private JLabel heading;
  private JLabel infoPane;

  private final Project project;
  private final PsiFile file;
  private final Editor editor;
  private final String text;

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return inputText;
  }

  public RefactoringDialog(Project project, PsiFile file, Editor editor, String text) {
    super(project);
    this.project = project;
    this.file = file;
    this.editor = editor;
    this.text = text;
    setModal(true);
    setTitle(GaugeBundle.message("gauge.group.refactoring"));
    this.heading.setText(GaugeBundle.message("refactoring.to", this.text));
    this.inputText.setText(this.text);
    setSize();
    registerActions();
    repaint();
    init();
    addListeners();
  }

  private void registerActions() {
    this.contentPane.registerKeyboardAction(e -> doOKAction(), KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                                            JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void addListeners() {
    addResizeListener();
    addInputFocusListener();
  }

  private void addResizeListener() {
    final ComponentAdapter resizeListener = new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent event) {
        Dimension contentSize = contentPane.getSize();
        Dimension componentSize = event.getComponent().getSize();
        if (componentSize.getWidth() > contentSize.getWidth()) {
          event.getComponent().setSize(new Dimension((int)contentSize.getWidth() - 20, (int)componentSize.getHeight()));
        }
      }
    };
    this.inputText.addComponentListener(resizeListener);
    this.heading.addComponentListener(resizeListener);
  }

  private void addInputFocusListener() {
    inputText.addFocusListener(new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        inputText.removeSelection();
        inputText.setCaretPosition(0);
      }
    });
  }

  @Override
  protected void doOKAction() {
    setOKActionEnabled(false);
    this.infoPane.setEnabled(true);
    this.infoPane.setVisible(true);
    String inputString = inputText.getText();
    new GaugeRefactorHandler(project, file, editor).compileAndRefactor(text, inputString, new RefactorStatusCallback() {
      @Override
      public void onStatusChange(@Nls String statusMessage) {
        infoPane.setText(statusMessage);
      }

      @Override
      public void onFinish(RefactoringStatus refactoringStatus) {
        if (refactoringStatus.isPassed()) {
          doCancelAction();
        }
        else {
          getRootPane().setDefaultButton(getButton(myCancelAction));
          infoPane.setVisible(false);
          setErrorText(refactoringStatus.getErrorMessage());
        }
      }
    });
  }

  @Override
  public void doCancelAction() {
    this.getWindow().setVisible(false);
    dispose();
  }

  @Nullable
  @Override
  protected JComponent createCenterPanel() {
    return contentPane;
  }

  private void setSize() {
    int stringWidth = SwingUtilities.computeStringWidth(this.heading.getFontMetrics(this.heading.getFont()), this.heading.getText());
    int height = this.contentPane.getHeight();
    this.contentPane.setSize(new Dimension(stringWidth + 30, height));
    this.contentPane.setMinimumSize(new Dimension(stringWidth + 30, height));
  }
}
