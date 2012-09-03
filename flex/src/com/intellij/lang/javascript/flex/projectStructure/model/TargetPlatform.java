package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.ui.ListCellRendererWrapper;
import icons.FlexIcons;

import javax.swing.*;

public enum TargetPlatform {

  Web("Web", FlexIcons.Bc_web),
  Desktop("Desktop", FlexIcons.Bc_desktop),
  Mobile("Mobile", FlexIcons.Bc_mobile);

  private final String myPresentableText;
  private final Icon myIcon;

  public String getPresentableText() {
    return myPresentableText;
  }

  TargetPlatform(final String presentableText, final Icon icon) {
    myPresentableText = presentableText;
    myIcon = icon;
  }

  public Icon getIcon() {
    return myIcon;
  }

  public static void initCombo(final JComboBox targetPlatformCombo) {
    targetPlatformCombo.setModel(new DefaultComboBoxModel(values()));
    targetPlatformCombo.setRenderer(new ListCellRendererWrapper<TargetPlatform>() {
      public void customize(JList list, TargetPlatform value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
        setIcon(value.getIcon());
      }
    });
  }
}
