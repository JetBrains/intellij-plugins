// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.refactoring.extract;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
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
  public void invoke(@NotNull Project project, PsiElement @NotNull [] elements, DataContext dataContext) {
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    final SelectionModel selectionModel = editor.getSelectionModel();

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

    final TextRange range = findRangeContainingCaret(offset, refactoring.getOccurrencesOffsets(), refactoring.getOccurrencesLengths());
    if (range != null) {
      selectionModel.setSelection(range.getStartOffset(), range.getEndOffset());
    }

    new DartServerExtractMethodDialog(project, editor, refactoring).show();
  }

  private static @Nullable TextRange findRangeContainingCaret(int caretOffset, int[] offsets, int[] lengths) {
    for (int i = 0; i < offsets.length; i++) {
      int offset1 = offsets[i];
      int length1 = lengths[i];
      if (caretOffset >= offset1 && caretOffset <= offset1 + length1) {
        return TextRange.create(offset1, offset1 + length1);
      }
    }
    return null;
  }
}

class DartServerExtractMethodDialog extends ServerRefactoringDialog<ServerExtractMethodRefactoring> {
  private final JTextField myMethodNameField = new JTextField();
  private final JCheckBox myAllCheckBox = new JCheckBox(DartBundle.message("checkbox.text.extract.all.occurrences"));
  private final JCheckBox myGetterCheckBox = new JCheckBox(DartBundle.message("checkbox.text.extract.getter"));
  private final JLabel mySignatureLabel = new JLabel();

  DartServerExtractMethodDialog(@NotNull Project project,
                                @Nullable Editor editor,
                                @NotNull ServerExtractMethodRefactoring refactoring) {
    super(project, editor, refactoring);
    setTitle(DartBundle.message("dialog.title.extract.method"));
    init();

    final String name = StringUtil.notNullize(ArrayUtil.getFirstElement(refactoring.getNames()), "name");
    myRefactoring.setName(name);
    myMethodNameField.setText(name);
    myMethodNameField.selectAll();
    myMethodNameField.getDocument().addDocumentListener(new DocumentAdapter() {
      @Override
      protected void textChanged(@NotNull DocumentEvent e) {
        final String name = myMethodNameField.getText();
        myRefactoring.setName(name);
        mySignatureLabel.setText(myRefactoring.getSignature());
      }
    });

    if (myRefactoring.getOccurrencesCount() != 1) {
      myAllCheckBox.setSelected(true);
      myAllCheckBox.setText(DartBundle.message("checkbox.text.extract.all.0.occurrences", myRefactoring.getOccurrencesCount()));
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
    nameLabel.setText(DartBundle.message("label.text.method.name"));

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

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return myMethodNameField;
  }
}
