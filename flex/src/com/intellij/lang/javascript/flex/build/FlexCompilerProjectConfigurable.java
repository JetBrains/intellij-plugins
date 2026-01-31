// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.build;

import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.RawCommandLineEditor;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public final class FlexCompilerProjectConfigurable implements SearchableConfigurable, Configurable.NoScroll {

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
  public @NotNull String getId() {
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
}
