package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.projectStructure.model.OutputType;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.lang.javascript.flex.projectStructure.options.BuildConfigurationNature;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.NonFocusableCheckBox;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Collection;

public class AddBuildConfigurationDialog extends DialogWrapper {

  private JPanel myMainPanel;
  private JTextField myNameTextField;
  private JComboBox myTargetPlatformCombo;
  private NonFocusableCheckBox myPureActionScriptCheckBox;
  private JComboBox myOutputTypeCombo;
  private final Collection<String> myUsedNames;

  public AddBuildConfigurationDialog(final Project project,
                                     final String dialogTitle,
                                     final Collection<String> usedNames,
                                     BuildConfigurationNature defaultNature) {
    super(project);
    myUsedNames = usedNames;
    setTitle(dialogTitle);
    initCombos();
    myTargetPlatformCombo.setSelectedItem(defaultNature.targetPlatform);
    myPureActionScriptCheckBox.setSelected(defaultNature.pureAS);
    myOutputTypeCombo.setSelectedItem(defaultNature.outputType);
    init();
  }

  private void initCombos() {
    TargetPlatform.initCombo(myTargetPlatformCombo);
    OutputType.initCombo(myOutputTypeCombo);
  }

  public JComponent getPreferredFocusedComponent() {
    return myNameTextField;
  }

  protected JComponent createCenterPanel() {
    return myMainPanel;
  }

  protected ValidationInfo doValidate() {
    final String name = getName();

    if (name.isEmpty()) {
      return new ValidationInfo("Empty name", myNameTextField);
    }

    for (final String usedName : myUsedNames) {
      if (name.equals(usedName)) {
        return new ValidationInfo(MessageFormat.format("Name ''{0}'' is already used", name), myNameTextField);
      }
    }

    return null;
  }

  public String getName() {
    return myNameTextField.getText().trim();
  }

  public BuildConfigurationNature getNature() {
    TargetPlatform targetPlatform = (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
    boolean isPureAs = myPureActionScriptCheckBox.isSelected();
    OutputType outputType = (OutputType)myOutputTypeCombo.getSelectedItem();
    return new BuildConfigurationNature(targetPlatform, isPureAs, outputType);
  }
}

