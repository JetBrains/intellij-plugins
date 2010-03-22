package com.intellij.tapestry.intellij.facet.ui;

import com.intellij.tapestry.intellij.facet.TapestryFacetConfiguration;

import javax.swing.*;

public class NewFacetDialog extends JDialog {

  private JPanel _mainPanel;
  private JTextField _filterName;
  private JTextField _applicationPackage;
  private JCheckBox _generateStartupApplication;
  private JCheckBox _generatePom;

  public NewFacetDialog(TapestryFacetConfiguration configuration) {
    setContentPane(_mainPanel);
    _filterName.setText(configuration.getFilterName());
    _applicationPackage.setText(configuration.getApplicationPackage());
    _generateStartupApplication.setVisible(false); // TODO: till we have sample application generated
    setModal(true);
  }

  public String getFilterName() {
    return _filterName.getText();
  }

  public String getApplicationPackage() {
    return _applicationPackage.getText();
  }

  public boolean shouldGenerateStartupApplication() {
    return _generateStartupApplication.isSelected();
  }

  public boolean shouldGeneratePom() {
    return _generatePom.isSelected();
  }

  public void setGenerateStartupApplication(boolean value) {
    _generateStartupApplication.setSelected(value);
  }

  public void setGeneratePom(boolean value) {
    _generatePom.setSelected(value);
  }

  public JPanel getMainPanel() {
    return _mainPanel;
  }
}
