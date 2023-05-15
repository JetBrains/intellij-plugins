// Copyright 2000-2018 JetBrains s.r.o.
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
package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class FlexCompilerProjectConfigurable implements SearchableConfigurable, Configurable.NoScroll {

  private JPanel myMainPanel;
  private JRadioButton myBuiltInCompilerRadioButton;
  private JRadioButton myMxmlcCompcRadioButton;
  private JCheckBox myPreferASC20CheckBox;

  private JTextField myHeapSizeTextField;
  private RawCommandLineEditor myVMOptionsEditor;

  private final FlexCompilerProjectConfiguration myConfig;

  public FlexCompilerProjectConfigurable(final Project project) {
    myConfig = FlexCompilerProjectConfiguration.getInstance(project);
  }

  @Override
  public JComponent createComponent() {
    return myMainPanel;
  }

  @Override
  @NotNull
  public String getId() {
    return "flex.compiler";
  }

  @Override
  public String getDisplayName() {
    return FlexBundle.message("configurable.FlexCompilerProjectConfigurable.display.name");
  }

  @Override
  public String getHelpTopic() {
    return "reference.projectsettings.compiler.flex";
  }

  @Override
  public boolean isModified() {
    return myConfig.USE_MXMLC_COMPC != myMxmlcCompcRadioButton.isSelected() ||
           myConfig.USE_BUILT_IN_COMPILER != myBuiltInCompilerRadioButton.isSelected() ||
           myConfig.PREFER_ASC_20 != myPreferASC20CheckBox.isSelected() ||
           !myHeapSizeTextField.getText().trim().equals(String.valueOf(myConfig.HEAP_SIZE_MB)) ||
           !myVMOptionsEditor.getText().trim().equals(myConfig.VM_OPTIONS);
  }

  @Override
  public void apply() throws ConfigurationException {
    myConfig.USE_BUILT_IN_COMPILER = myBuiltInCompilerRadioButton.isSelected();
    myConfig.USE_MXMLC_COMPC = myMxmlcCompcRadioButton.isSelected();
    myConfig.PREFER_ASC_20 = myPreferASC20CheckBox.isSelected();

    try {
      final int heapSizeMb = Integer.parseInt(myHeapSizeTextField.getText().trim());
      if (heapSizeMb > 0) {
        myConfig.HEAP_SIZE_MB = heapSizeMb;
      }
      else {
        throw new ConfigurationException(FlexBundle.message("invalid.flex.compiler.heap.size"));
      }
    }
    catch (NumberFormatException e) {
      throw new ConfigurationException(FlexBundle.message("invalid.flex.compiler.heap.size"));
    }

    myConfig.VM_OPTIONS = myVMOptionsEditor.getText().trim();
  }

  @Override
  public void reset() {
    myBuiltInCompilerRadioButton.setSelected(myConfig.USE_BUILT_IN_COMPILER);
    myMxmlcCompcRadioButton.setSelected(myConfig.USE_MXMLC_COMPC);
    myPreferASC20CheckBox.setSelected(myConfig.PREFER_ASC_20);
    myHeapSizeTextField.setText(String.valueOf(myConfig.HEAP_SIZE_MB));
    myVMOptionsEditor.setText(myConfig.VM_OPTIONS);
  }

  @Override
  public void disposeUIResources() {
    SearchableConfigurable.super.disposeUIResources();
  }
}
