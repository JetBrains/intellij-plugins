// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil.refactoring;

import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EditorComboBoxEditor;
import com.intellij.ui.EditorComboBoxRenderer;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.StringComboboxEditor;
import org.intellij.terraform.hcl.HCLBundle;
import org.intellij.terraform.config.TerraformFileType;
import org.intellij.terraform.hil.psi.ILExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.Collection;

public class ILIntroduceDialog extends DialogWrapper {
  private JPanel myContentPane;
  private JLabel myNameLabel;
  private ComboBox<String> myNameComboBox;
  private JCheckBox myReplaceAll;

  private final Project myProject;
  private final int myOccurrencesCount;
  private final IntroduceValidator myValidator;
  private final ILExpression myExpression;

  public ILIntroduceDialog(@NotNull final Project project,
                           @NotNull @Nls final String caption,
                           @NotNull IntroduceValidator validator,
                           final IntroduceOperation operation) {
    super(project, true);
    myOccurrencesCount = operation.getOccurrences().size();
    myValidator = validator;
    myProject = project;
    myExpression = operation.getInitializer();
    setUpNameComboBox(operation.getSuggestedNames());

    setTitle(caption);
    init();
    setupDialog();
    updateControls();
  }

  private void setUpNameComboBox(Collection<String> possibleNames) {
    final EditorComboBoxEditor comboEditor = new StringComboboxEditor(myProject, TerraformFileType.INSTANCE, myNameComboBox);

    myNameComboBox.setEditor(comboEditor);
    //noinspection GtkPreferredJComboBoxRenderer,unchecked
    myNameComboBox.setRenderer(new EditorComboBoxRenderer(comboEditor));
    myNameComboBox.setEditable(true);
    myNameComboBox.setMaximumRowCount(8);

    myNameComboBox.addItemListener(e -> updateControls());
    ((EditorTextField)myNameComboBox.getEditor().getEditorComponent()).addDocumentListener(new DocumentListener() {
      @Override
      public void beforeDocumentChange(@NotNull DocumentEvent event) {
        DocumentListener.super.beforeDocumentChange(event);
      }

      @Override
      public void documentChanged(@NotNull DocumentEvent event) {
        updateControls();
      }
    });

    myContentPane.registerKeyboardAction(e -> IdeFocusManager.getGlobalInstance().doWhenFocusSettlesDown(
        () -> IdeFocusManager.getGlobalInstance().requestFocus(myNameComboBox, true)), KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.ALT_MASK), JComponent.WHEN_IN_FOCUSED_WINDOW);

    for (@NlsSafe String possibleName : possibleNames) {
      myNameComboBox.addItem(possibleName);
    }
  }

  private void setupDialog() {
    myReplaceAll.setMnemonic(KeyEvent.VK_A);
    myNameLabel.setLabelFor(myNameComboBox);

    // Replace occurrences check box setup
    if (myOccurrencesCount > 1) {
      myReplaceAll.setSelected(false);
      myReplaceAll.setEnabled(true);
      myReplaceAll.setText(HCLBundle.message("introduce.dialogs.replace.all.checkbox.title", myReplaceAll.getText(), myOccurrencesCount));
    } else {
      myReplaceAll.setSelected(false);
      myReplaceAll.setEnabled(false);
    }
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return myNameComboBox;
  }

  @Override
  protected JComponent createCenterPanel() {
    return myContentPane;
  }

  @Nullable
  public String getName() {
    final Object item = myNameComboBox.getEditor().getItem();
    if ((item instanceof String) && ((String) item).length() > 0) {
      return ((String) item).trim();
    }
    return null;
  }

  public Project getProject() {
    return myProject;
  }

  public ILExpression getExpression() {
    return myExpression;
  }

  public boolean doReplaceAllOccurrences() {
    return myReplaceAll.isSelected();
  }

  private void updateControls() {
    final String name = getName();
    boolean nameValid = myValidator.isNameValid(name, getProject());
    setErrorText(!nameValid ? HCLBundle.message("refactoring.introduce.name.error") : null);
    nameValid &= name != null && myValidator.checkPossibleName(name, myExpression);
    setErrorText(!nameValid ? HCLBundle.message("refactoring.introduce.variable.scope.error") : null);
    setOKActionEnabled(nameValid);
  }

}
