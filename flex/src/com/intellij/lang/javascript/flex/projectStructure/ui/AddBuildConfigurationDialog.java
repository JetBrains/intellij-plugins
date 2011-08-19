package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.ide.ui.ListCellRendererWrapper;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.NonFocusableCheckBox;

import javax.swing.*;
import java.text.MessageFormat;
import java.util.Collection;

import static com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration.OutputType;
import static com.intellij.lang.javascript.flex.projectStructure.options.FlexIdeBuildConfiguration.TargetPlatform;

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
                                     final TargetPlatform targetPlatform,
                                     final boolean pureActionScript,
                                     final OutputType outputType) {
    super(project);
    myUsedNames = usedNames;
    setTitle(dialogTitle);
    initCombos();
    myTargetPlatformCombo.setSelectedItem(targetPlatform);
    myPureActionScriptCheckBox.setSelected(pureActionScript);
    myOutputTypeCombo.setSelectedItem(outputType);
    init();
  }

  private void initCombos() {
    myTargetPlatformCombo.setModel(new DefaultComboBoxModel(TargetPlatform.values()));
    myTargetPlatformCombo.setRenderer(new ListCellRendererWrapper<TargetPlatform>(myTargetPlatformCombo.getRenderer()) {
      public void customize(JList list, TargetPlatform value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });

    myOutputTypeCombo.setModel(new DefaultComboBoxModel(OutputType.values()));
    myOutputTypeCombo.setRenderer(new ListCellRendererWrapper<OutputType>(myOutputTypeCombo.getRenderer()) {
      public void customize(JList list, OutputType value, int index, boolean selected, boolean hasFocus) {
        setText(value.PRESENTABLE_TEXT);
      }
    });
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

  public TargetPlatform getTargetPlatform() {
    return (TargetPlatform)myTargetPlatformCombo.getSelectedItem();
  }

  public boolean isPureActionScript() {
    return myPureActionScriptCheckBox.isSelected();
  }

  public OutputType getOutputType() {
    return (OutputType)myOutputTypeCombo.getSelectedItem();
  }
}

