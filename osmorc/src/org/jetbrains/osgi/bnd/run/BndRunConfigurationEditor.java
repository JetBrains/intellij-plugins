// Copyright 2000-2017 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.jetbrains.osgi.bnd.run;

import com.intellij.execution.ui.DefaultJreSelector;
import com.intellij.execution.ui.JrePathEditor;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.osgi.bnd.BndFileType;
import org.osmorc.i18n.OsmorcBundle;

import javax.swing.*;

public class BndRunConfigurationEditor extends SettingsEditor<BndRunConfigurationBase> {
  private JPanel myPanel;
  private TextFieldWithBrowseButton myChooser;
  private JrePathEditor myJrePathEditor;

  public BndRunConfigurationEditor(Project project) {
    myJrePathEditor.setDefaultJreSelector(DefaultJreSelector.projectSdk(project));
    FileChooserDescriptor descriptor = FileChooserDescriptorFactory
      .createSingleFileNoJarsDescriptor()
      .withFileFilter(file -> {
        String ext = file.getExtension();
        return BndFileType.BND_RUN_EXT.equals(ext) || BndFileType.BND_EXT.equals(ext);
      });
    myChooser.addBrowseFolderListener(OsmorcBundle.message("bnd.run.file.chooser.title"), null, project, descriptor);
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
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
