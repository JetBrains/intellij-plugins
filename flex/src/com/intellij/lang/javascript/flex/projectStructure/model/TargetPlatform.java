package com.intellij.lang.javascript.flex.projectStructure.model;

import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public enum TargetPlatform {

  Web("Web", IconLoader.getIcon("/images/bc-web.png")),
  Desktop("Desktop", IconLoader.getIcon("/images/bc-desktop.png")),
  Mobile("Mobile", IconLoader.getIcon("/images/bc-mobile.png"));

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
    targetPlatformCombo.setRenderer(new ListCellRendererWrapper<TargetPlatform>(targetPlatformCombo.getRenderer()) {
      public void customize(JList list, TargetPlatform value, int index, boolean selected, boolean hasFocus) {
        setText(value.getPresentableText());
        setIcon(value.getIcon());
      }
    });
  }
}
