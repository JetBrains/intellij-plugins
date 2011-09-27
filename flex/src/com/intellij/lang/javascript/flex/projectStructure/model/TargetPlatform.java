package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.ide.ui.ListCellRendererWrapper;

import javax.swing.*;

public enum TargetPlatform {
  Web("Web"),
  Desktop("Desktop"),
  Mobile("Mobile");

  private final String myPresentableText;

  public String getPresentableText() {
    return myPresentableText;
  }

  TargetPlatform(final String presentableText) {
    myPresentableText = presentableText;
  }

  public static void initCombo(final JComboBox targetPlatformCombo) {
    targetPlatformCombo.setModel(new DefaultComboBoxModel(values()));
    targetPlatformCombo.setRenderer(new ListCellRendererWrapper<TargetPlatform>(targetPlatformCombo.getRenderer()) {
      public void customize(JList list, TargetPlatform value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
      }
    });
  }
}
