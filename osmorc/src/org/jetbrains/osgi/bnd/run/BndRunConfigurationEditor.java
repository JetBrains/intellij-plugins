// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.BndFileType;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class BndRunConfigurationEditor extends SettingsEditor<BndRunConfigurationBase> {
  private JPanel myPanel;
  private TextFieldWithBrowseButton myChooser;
  private JrePathEditor myJrePathEditor;

  public BndRunConfigurationEditor(Project project) {
    myJrePathEditor.setDefaultJreSelector(DefaultJreSelector.projectSdk(project));
    myChooser.addBrowseFolderListener(project, FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor()
      .withExtensionFilter(OsmorcBundle.message("bnd.run.file.chooser.label"), BndFileType.BND_RUN_EXT, BndFileType.BND_EXT)
      .withTitle(OsmorcBundle.message("bnd.run.file.chooser.title")));
  }

  @Override
  protected @NotNull JComponent createEditor() {
    return myPanel;
  }

  @Override
  protected void resetEditorFrom(@NotNull BndRunConfigurationBase configuration) {
    BndRunConfigurationOptions options = configuration.getOptions();
    myChooser.setText(options.getBndRunFile());
    myJrePathEditor.setPathOrName(options.getAlternativeJrePath(), options.getUseAlternativeJre());
  }

  @Override
  protected void applyEditorTo(@NotNull BndRunConfigurationBase configuration) {
    BndRunConfigurationOptions options = configuration.getOptions();
    options.setBndRunFile(myChooser.getText());
    options.setUseAlternativeJre(myJrePathEditor.isAlternativeJreSelected());
    options.setAlternativeJrePath(myJrePathEditor.getJrePathOrName());
  }
}
