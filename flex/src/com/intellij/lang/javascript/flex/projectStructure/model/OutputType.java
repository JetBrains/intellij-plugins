package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.lang.javascript.flex.FlexBundle;

import javax.swing.*;

public enum OutputType {
  Application(FlexBundle.message("bc.app.long.text"), FlexBundle.message("bc.app.short.text")),
  Library(FlexBundle.message("bc.lib.long.text"), FlexBundle.message("bc.lib.short.text")),
  RuntimeLoadedModule(FlexBundle.message("bc.rlm.long.text"), FlexBundle.message("bc.rlm.short.text"));

  private final String myPresentableText;
  private final String myShortText;

  public String getPresentableText() {
    return myPresentableText;
  }

  public String getShortText() {
    return myShortText;
  }

  OutputType(final String presentableText, final String shortText) {
    myPresentableText = presentableText;
    myShortText = shortText;
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
