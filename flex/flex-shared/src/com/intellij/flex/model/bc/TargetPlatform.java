package com.intellij.flex.model.bc;

import icons.FlexSharedIcons;

import javax.swing.*;

public enum TargetPlatform {

  Web("Web", FlexSharedIcons.Bc_web),
  Desktop("Desktop", FlexSharedIcons.Bc_desktop),
  Mobile("Mobile", FlexSharedIcons.Bc_mobile);

  private final String myPresentableText;
  private final Icon myIcon;

  TargetPlatform(final String presentableText, final Icon icon) {
    myPresentableText = presentableText;
    myIcon = icon;
  }

  public String getPresentableText() {
    return myPresentableText;
  }

  public Icon getIcon() {
    return myIcon;
  }
}
