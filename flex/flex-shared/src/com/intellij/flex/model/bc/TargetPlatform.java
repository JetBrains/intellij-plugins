package com.intellij.flex.model.bc;

import icons.FlexSharedIcons;

import javax.swing.*;

public enum TargetPlatform {

  Web("Web"),
  Desktop("Desktop"),
  Mobile("Mobile");

  private final String myPresentableText;

  TargetPlatform(final String presentableText) {
    myPresentableText = presentableText;
  }

  public String getPresentableText() {
    return myPresentableText;
  }

  public Icon getIcon() {
    // do not keep icon in a field that is initialized at instantiation, because it will lead to runtime error in external compiler process on Mac (IDEA-90997)
    switch (this) {
      case Web:
        return FlexSharedIcons.Bc_web;
      case Desktop:
        return FlexSharedIcons.Bc_desktop;
      case Mobile:
        return FlexSharedIcons.Bc_mobile;
      default:
        assert false : this;
        return null;
    }
  }
}
