package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.lang.javascript.flex.FlexUtils;
import com.intellij.lang.javascript.flex.projectStructure.model.AirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.IosPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableAirPackagingOptions;
import com.intellij.lang.javascript.flex.projectStructure.model.ModifiableIosPackagingOptions;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.wm.IdeFocusManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AirDescriptorForm {
  private JPanel myMainPanel; // required for form reuse

  private JRadioButton myGeneratedDescriptorRadioButton;
  private JLabel myApplicationIdLabel;
  private JTextField myApplicationIdTextField;

  private JRadioButton myCustomDescriptorRadioButton;
  private TextFieldWithBrowseButton myCustomDescriptorTextWithBrowse;
  private JButton myCreateDescriptorButton;

  public AirDescriptorForm(final Project project,
                           final Runnable descriptorCreator,
                           final boolean showApplicationIdField) {
    myApplicationIdLabel.setVisible(showApplicationIdField);
    myApplicationIdTextField.setVisible(showApplicationIdField);

    final ActionListener listener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        updateControls();
        if (myCustomDescriptorRadioButton.isSelected()) {
          IdeFocusManager.getInstance(project).requestFocus(myCustomDescriptorTextWithBrowse.getTextField(), true);
        }
      }
    };

    myGeneratedDescriptorRadioButton.addActionListener(listener);
    myCustomDescriptorRadioButton.addActionListener(listener);

    myCustomDescriptorTextWithBrowse.addBrowseFolderListener(null, null, project, FlexUtils.createFileChooserDescriptor("xml"));

    myCreateDescriptorButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        descriptorCreator.run();
      }
    });
  }

  void updateControls() {
    myApplicationIdLabel.setEnabled(myGeneratedDescriptorRadioButton.isSelected());
    myApplicationIdTextField.setEnabled(myGeneratedDescriptorRadioButton.isSelected());

    myCustomDescriptorTextWithBrowse.setEnabled(myCustomDescriptorRadioButton.isSelected());
    myCreateDescriptorButton.setEnabled(myCustomDescriptorRadioButton.isSelected());
  }

  public void resetFrom(final AirPackagingOptions packagingOptions) {
    myGeneratedDescriptorRadioButton.setSelected(packagingOptions.isUseGeneratedDescriptor());
    myCustomDescriptorRadioButton.setSelected(!packagingOptions.isUseGeneratedDescriptor());
    if (packagingOptions instanceof IosPackagingOptions) {
      myApplicationIdTextField.setText(((IosPackagingOptions)packagingOptions).getApplicationId());
    }
    myCustomDescriptorTextWithBrowse.setText(FileUtil.toSystemDependentName(packagingOptions.getCustomDescriptorPath()));
  }

  public boolean isModified(final ModifiableAirPackagingOptions packagingOptions) {
    if (packagingOptions.isUseGeneratedDescriptor() != myGeneratedDescriptorRadioButton.isSelected()) return true;
    if (packagingOptions instanceof ModifiableIosPackagingOptions
        && !((ModifiableIosPackagingOptions)packagingOptions).getApplicationId().equals(myApplicationIdTextField.getText())) {
      return true;
    }
    if (!packagingOptions.getCustomDescriptorPath().equals(
      FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()))) {
      return true;
    }

    return false;
  }

  public void applyTo(final ModifiableAirPackagingOptions model) {
    model.setUseGeneratedDescriptor(myGeneratedDescriptorRadioButton.isSelected());
    if (model instanceof ModifiableIosPackagingOptions) {
      ((ModifiableIosPackagingOptions)model).setApplicationId(myApplicationIdTextField.getText().trim());
    }
    model.setCustomDescriptorPath(FileUtil.toSystemIndependentName(myCustomDescriptorTextWithBrowse.getText().trim()));
  }

  public void setUseCustomDescriptor(final String descriptorPath) {
    myCustomDescriptorRadioButton.setSelected(true);
    myCustomDescriptorTextWithBrowse.setText(FileUtil.toSystemDependentName(descriptorPath));
    updateControls();
  }
}
