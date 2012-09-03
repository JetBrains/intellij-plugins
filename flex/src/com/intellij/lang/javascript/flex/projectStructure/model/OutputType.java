package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.flex.FlexCommonBundle;
import com.intellij.ui.ListCellRendererWrapper;

import javax.swing.*;

public enum OutputType {
  Application(FlexCommonBundle.message("bc.app.long.text"), FlexCommonBundle.message("bc.app.short.text")),
  Library(FlexCommonBundle.message("bc.lib.long.text"), FlexCommonBundle.message("bc.lib.short.text")),
  RuntimeLoadedModule(FlexCommonBundle.message("bc.rlm.long.text"), FlexCommonBundle.message("bc.rlm.short.text"));

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
    outputTypeCombo.setRenderer(new ListCellRendererWrapper<OutputType>() {
      public void customize(JList list, OutputType value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
      }
    });
  }
}
