package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.ide.ui.ListCellRendererWrapper;

import javax.swing.*;

public enum OutputType {
  Application("Application"),
  RuntimeLoadedModule("Runtime-loaded module"),
  Library("Library");

  private final String myPresentableText;

  public String getPresentableText() {
    return myPresentableText;
  }

  OutputType(final String presentableText) {
    myPresentableText = presentableText;
  }

  public static void initCombo(final JComboBox outputTypeCombo) {
    outputTypeCombo.setModel(new DefaultComboBoxModel(values()));
    outputTypeCombo.setRenderer(new ListCellRendererWrapper<OutputType>(outputTypeCombo.getRenderer()) {
      public void customize(JList list, OutputType value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
      }
    });
  }
}
