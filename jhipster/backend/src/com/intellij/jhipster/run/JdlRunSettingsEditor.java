// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.intellij.jhipster.run;

import com.intellij.jhipster.JdlBundle;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import static com.intellij.openapi.util.io.FileUtilRt.toSystemDependentName;
import static com.intellij.openapi.vfs.VfsUtilCore.virtualToIoFile;

public final class JdlRunSettingsEditor extends SettingsEditor<JdlRunConfiguration> {

  private final TextFieldWithBrowseButton jdlLocationField = new TextFieldWithBrowseButton(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      var descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
      var file = FileChooser.chooseFile(descriptor, jdlLocationField, project, null);
      if (file != null) {
        File f = virtualToIoFile(file);
        jdlLocationField.setText(toSystemDependentName(f.getPath()));
      }
    }
  });

  private final TextFieldWithBrowseButton outputLocationField = new TextFieldWithBrowseButton(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      var descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
      var file = FileChooser.chooseFile(descriptor, outputLocationField, project, null);
      if (file != null) {
        File f = virtualToIoFile(file);
        outputLocationField.setText(toSystemDependentName(f.getPath()));
      }
    }
  });

  private final TextFieldWithBrowseButton jhipsterLocationField = new TextFieldWithBrowseButton(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      var descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
      var file = FileChooser.chooseFile(descriptor, jhipsterLocationField, project, null);
      if (file != null) {
        File f = virtualToIoFile(file);
        jhipsterLocationField.setText(toSystemDependentName(f.getPath()));
      }
    }
  });

  private final Project project;

  public JdlRunSettingsEditor(Project project) {
    this.project = project;
  }

  @Override
  protected void resetEditorFrom(@NotNull JdlRunConfiguration runConfiguration) {
    JdlRunConfigurationOptions options = runConfiguration.getOptions();

    jdlLocationField.setText(options.getJdlLocation());
    outputLocationField.setText(options.getOutputLocation());
    jhipsterLocationField.setText(options.getJHipsterLocation());
  }

  @Override
  protected void applyEditorTo(@NotNull JdlRunConfiguration runConfiguration) throws ConfigurationException {
    JdlRunConfigurationOptions options = runConfiguration.getOptions();

    String jdlLocation = jdlLocationField.getText();
    File jdlFile = new File(jdlLocation);
    if (!jdlFile.exists() || !jdlFile.isFile()) {
      throw new ConfigurationException(JdlBundle.message("dialog.message.jdl.file.does.not.exist", jdlLocation));
    }

    options.setJdlLocation(jdlLocation);
    options.setJhipsterLocation(jhipsterLocationField.getText());
    options.setOutputLocation(outputLocationField.getText());
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return FormBuilder.createFormBuilder()
      .addLabeledComponent(JdlBundle.message("label.jdl.file"), jdlLocationField)
      .addLabeledComponent(JdlBundle.message("label.output.location"), outputLocationField)
      .addLabeledComponent(JdlBundle.message("label.jhipster.executable"), jhipsterLocationField)
      .getPanel();
  }
}