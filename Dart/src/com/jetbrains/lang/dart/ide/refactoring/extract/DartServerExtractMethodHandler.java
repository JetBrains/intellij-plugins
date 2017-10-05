/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.ide.refactoring.extract;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.intellij.ui.DocumentAdapter;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.JBUI;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.refactoring.ServerExtractMethodRefactoring;
import com.jetbrains.lang.dart.ide.refactoring.ServerRefactoringDialog;
import com.jetbrains.lang.dart.ide.refactoring.status.RefactoringStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;

public class DartServerExtractMethodHandler implements RefactoringActionHandler {
  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final SelectionModel selectionModel = editor.getSelectionModel();
    if (!selectionModel.hasSelection()) selectionModel.selectLineAtCaret();

    final int offset = selectionModel.getSelectionStart();
    final int length = selectionModel.getSelectionEnd() - offset;
    final ServerExtractMethodRefactoring refactoring = new ServerExtractMethodRefactoring(project, file.getVirtualFile(), offset, length);

    // Validate initial status.
    {
      final RefactoringStatus initialStatus = refactoring.checkInitialConditions();
      if (initialStatus == null) {
        return;
      }
      if (initialStatus.hasError()) {
        final String title = DartBundle.message("dart.refactoring.extract.method.error");
        final String message = initialStatus.getMessage();
        // This is not null if the status has an error.
        assert message != null;
        CommonRefactoringUtil.showErrorHint(project, editor, message, title, null);
        return;
      }
    }

    new DartServerExtractMethodDialog(project, editor, refactoring).show();
  }
}

class DartServerExtractMethodDialog extends ServerRefactoringDialog<ServerExtractMethodRefactoring> {
  @NotNull final ServerExtractMethodRefactoring myRefactoring;
  private JTextField myMethodNameField = new JTextField();
  private JCheckBox myAllCheckBox = new JCheckBox("Extract all occurrences");
  private JCheckBox myGetterCheckBox = new JCheckBox("Extract getter");
  private JLabel mySignatureLabel = new JLabel();

  public DartServerExtractMethodDialog(@NotNull Project project,
                                       @Nullable Editor editor,
                                       @NotNull ServerExtractMethodRefactoring refactoring) {
    super(project, editor, refactoring);
    myRefactoring = refactoring;
    setTitle("Extract Method");
    init();

    final String name = StringUtil.notNullize(ArrayUtil.getFirstElement(refactoring.getNames()), "name");
    myRefactoring.setName(name);
    myMethodNameField.setText(name);
    myMethodNameField.selectAll();
    myMethodNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(DocumentEvent e) {
        final String name = myMethodNameField.getText();
        myRefactoring.setName(name);
        mySignatureLabel.setText(myRefactoring.getSignature());
      }
    });

    if (myRefactoring.getOccurrences() != 1) {
      myAllCheckBox.setSelected(true);
      myAllCheckBox.setText("Extract all " + myRefactoring.getOccurrences() + " occurrences");
      myAllCheckBox.addActionListener(e -> myRefactoring.setExtractAll(myAllCheckBox.isSelected()));
    }
    else {
      myAllCheckBox.setEnabled(false);
    }

    if (myRefactoring.canExtractGetter()) {
      myGetterCheckBox.setSelected(false);
      updateRefactoringPreview();
      myGetterCheckBox.addActionListener(e -> updateRefactoringPreview());
    }
    else {
      myGetterCheckBox.setEnabled(false);
    }

    mySignatureLabel.setText(myRefactoring.getSignature());
  }

  private void updateRefactoringPreview() {
    myRefactoring.setCreateGetter(myGetterCheckBox.isSelected());
    mySignatureLabel.setText(myRefactoring.getSignature());
  }

  @Override
  protected JComponent createCenterPanel() {
    return null;
  }

  @Override
  protected JComponent createNorthPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbConstraints = new GridBagConstraints();

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.gridx = 0;
    gbConstraints.gridy = 0;
    gbConstraints.gridwidth = 1;
    gbConstraints.weightx = 0;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.NONE;
    gbConstraints.anchor = GridBagConstraints.WEST;
    JLabel nameLabel = new JLabel();
    panel.add(nameLabel, gbConstraints);
    nameLabel.setText("Method name:");

    gbConstraints.insets = JBUI.insets(0, 4, 4, 0);
    gbConstraints.gridx = 1;
    gbConstraints.gridy = 0;
    gbConstraints.gridwidth = GridBagConstraints.REMAINDER;
    gbConstraints.weightx = 1;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.BOTH;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(myMethodNameField, gbConstraints);
    myMethodNameField.setPreferredSize(new Dimension(200, myMethodNameField.getPreferredSize().height));

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.gridx = 0;
    gbConstraints.gridy = 1;
    gbConstraints.gridwidth = 2;
    gbConstraints.weightx = 1;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(myGetterCheckBox, gbConstraints);

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.gridx = 0;
    gbConstraints.gridy = 2;
    gbConstraints.gridwidth = 2;
    gbConstraints.weightx = 1;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(myAllCheckBox, gbConstraints);

    gbConstraints.insets = JBUI.insetsBottom(4);
    gbConstraints.gridx = 0;
    gbConstraints.gridy = 3;
    gbConstraints.gridwidth = 2;
    gbConstraints.weightx = 1;
    gbConstraints.weighty = 0;
    gbConstraints.fill = GridBagConstraints.HORIZONTAL;
    gbConstraints.anchor = GridBagConstraints.WEST;
    panel.add(mySignatureLabel, gbConstraints);

    return panel;
  }

  @Nullable
  @Override
  public JComponent getPreferredFocusedComponent() {
    return myMethodNameField;
  }
}
