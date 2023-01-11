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

package com.thoughtworks.gauge.extract;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.TextFieldWithAutoCompletionListProvider;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.GaugeBundle;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Objects;

final class ExtractConceptDialog extends JDialog {
  private JPanel contentPane;
  private TextFieldWithAutoCompletion<?> conceptName;
  private JTextArea steps;
  private JComboBox<String> files;
  private JTextField newFile;
  private JButton OKButton;
  private JButton cancelButton;
  private JLabel errors;

  private final Project project;
  private final List<String> args;

  private boolean cancelled = true;
  private DialogBuilder builder;

  ExtractConceptDialog(Project project, List<String> args) {
    this.project = project;
    this.args = args;
    setContentPane(contentPane);
    setModal(true);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });
    setProperties();
  }

  public void setData(String data, List<String> files, DialogBuilder builder) {
    this.builder = builder;
    this.steps.setColumns(50);
    this.steps.setRows(10);
    this.steps.setEditable(false);
    this.steps.setText(data);

    for (@NlsSafe String file : files) {
      this.files.addItem(file);
    }
  }

  public ExtractConceptInfo getInfo() {
    String fileName = Objects.requireNonNull(this.files.getSelectedItem()).toString();
    if (fileName.equals(ExtractConceptInfoCollector.CREATE_NEW_FILE)) fileName = this.newFile.getText();
    return new ExtractConceptInfo(this.conceptName.getText(), fileName.trim(), cancelled);
  }

  private void setProperties() {
    contentPane.registerKeyboardAction(getCancelAction(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                       JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    this.newFile.setVisible(false);
    this.conceptName.setPlaceholder(GaugeBundle.message("enter.concept.name.example.my.new.concept"));
    this.files.addActionListener(e -> {
      this.newFile.setVisible(false);
      Object selectedItem = Objects.requireNonNull(this.files.getSelectedItem());
      if (selectedItem.toString().equals(ExtractConceptInfoCollector.CREATE_NEW_FILE)) {
        this.newFile.setVisible(true);
      }
    });
    this.cancelButton.addActionListener(getCancelAction());
    this.OKButton.addActionListener(getOKAction());
  }

  @NotNull
  private ActionListener getCancelAction() {
    return e -> onCancel();
  }

  @NotNull
  private ActionListener getOKAction() {
    return e -> {
      if (conceptName.getText().trim().isEmpty()) {
        errors.setText(GaugeBundle.message("please.enter.concept.name"));
      }
      else if (newFile.isVisible() && (FilenameUtils.removeExtension(newFile.getText().trim()).isEmpty() ||
                                       !FilenameUtils.getExtension(newFile.getText().trim()).equals(GaugeConstants.CONCEPT_EXTENSION))) {
        errors.setText(GaugeBundle.message("please.select.filename.cpt"));
      }
      else {
        cancelled = false;
        builder.getWindow().setVisible(false);
      }
    };
  }

  private void onCancel() {
    builder.getWindow().setVisible(false);
    dispose();
  }

  private void createUIComponents() {
    this.conceptName = new TextFieldWithAutoCompletion<>(this.project, getAutoCompleteTextField(this.args), true, "");
  }

  private static TextFieldWithAutoCompletionListProvider<String> getAutoCompleteTextField(final List<String> dirNames) {
    return new TextFieldWithAutoCompletionListProvider<>(dirNames) {
      @Nullable
      @Override
      protected Icon getIcon(@NotNull String item) {
        return super.getIcon(item);
      }

      @NotNull
      @Override
      protected String getLookupString(@NotNull String o) {
        return o;
      }

      @Nullable
      @Override
      protected String getTailText(@NotNull String item) {
        return super.getTailText(item);
      }

      @Nullable
      @Override
      protected String getTypeText(@NotNull String item) {
        return super.getTypeText(item);
      }

      @Override
      public int compare(String o, String t1) {
        return 0;
      }
    };
  }
}