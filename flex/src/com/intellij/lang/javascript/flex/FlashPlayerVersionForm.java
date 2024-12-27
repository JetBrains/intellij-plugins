// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex;

import com.intellij.util.ArrayUtilRt;

import javax.swing.*;
import javax.swing.event.DocumentListener;

public class FlashPlayerVersionForm {
  private JPanel myMainPanel;
  private JTextField myPlayerVersionMajorTextField;
  private JTextField myPlayerVersionMinorTextField;
  private JTextField myPlayerVersionRevisionTextField;

  public String getPlayerVersion() {
    final String major = myPlayerVersionMajorTextField.getText().trim();
    final String minor = myPlayerVersionMinorTextField.getText().trim();
    final String revision = myPlayerVersionRevisionTextField.getText().trim();
    return major +
           ((!minor.isEmpty() || !revision.isEmpty()) ? "." + (!minor.isEmpty() ? minor : "0") : "") +
           (!revision.isEmpty() ? "." + revision : "");
  }

  public String getPlayerVersionMajor(){
    return myPlayerVersionMajorTextField.getText().trim();
  }

  public String getPlayerVersionMinor(){
    return myPlayerVersionMinorTextField.getText().trim();
  }

  public String getPlayerVersionRevision(){
    return myPlayerVersionRevisionTextField.getText().trim();
  }


  public void setPlayerVersion(String playerVersion) {
    final String[] strings = playerVersion == null ? ArrayUtilRt.EMPTY_STRING_ARRAY : playerVersion.split("[.]");
    myPlayerVersionMajorTextField.setText(strings.length > 0 ? strings[0] : "");
    myPlayerVersionMinorTextField.setText(strings.length > 1 ? strings[1] : "");
    myPlayerVersionRevisionTextField.setText(strings.length > 2 ? strings[2] : "");
  }

  public JComponent getMainPanel() {
    return myMainPanel;
  }

  public void setEnabled(final boolean enabled) {
    myMainPanel.setEnabled(enabled);
  }

  public boolean isEnabled() {
    return myMainPanel.isEnabled();
  }

  public void addDocumentListener(final DocumentListener listener) {
    myPlayerVersionMajorTextField.getDocument().addDocumentListener(listener);
    myPlayerVersionMinorTextField.getDocument().addDocumentListener(listener);
    myPlayerVersionRevisionTextField.getDocument().addDocumentListener(listener);
  }

  public void removeDocumentListener(final DocumentListener listener) {
    myPlayerVersionMajorTextField.getDocument().removeDocumentListener(listener);
    myPlayerVersionMinorTextField.getDocument().removeDocumentListener(listener);
    myPlayerVersionRevisionTextField.getDocument().removeDocumentListener(listener);
  }
}
