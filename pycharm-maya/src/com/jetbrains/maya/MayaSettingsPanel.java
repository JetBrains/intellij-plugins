package com.jetbrains.maya;

import com.intellij.openapi.util.text.StringUtil;

import javax.swing.*;

/**
 * @author traff
 */
public class MayaSettingsPanel {
  private JTextField myPythonCommandPort;
  private JPanel myPanel;
  private final MayaSettingsProvider mySettingsProvider;

  public MayaSettingsPanel(MayaSettingsProvider provider) {
    mySettingsProvider = provider;
    reset();
  }

  public JComponent createPanel() {
    return myPanel;
  }

  public boolean isModified() {
    return getPythonCommandPort() != mySettingsProvider.getPort();
  }

  public int getPythonCommandPort() {
    return StringUtil.parseInt(myPythonCommandPort.getText(), -1);
  }

  public void apply() {
    mySettingsProvider.setPort(getPythonCommandPort());
  }

  public void reset() {
    setPythonCommandPort(mySettingsProvider.getPort());
  }

  public void setPythonCommandPort(int pythonCommandPort) {
    myPythonCommandPort.setText(Integer.toString(pythonCommandPort));
  }
}
