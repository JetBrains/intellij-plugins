package com.intellij.jps.flex.model.bc;

import com.intellij.openapi.util.IconLoader;

import javax.swing.*;

public enum JpsTargetPlatform {

  Web("Web", IconLoader.getIcon("/images/bc-web.png")),
  Desktop("Desktop", IconLoader.getIcon("/images/bc-desktop.png")),
  Mobile("Mobile", IconLoader.getIcon("/images/bc-mobile.png"));

  private final String myPresentableText;
  private final Icon myIcon;

  JpsTargetPlatform(final String presentableText, final Icon icon) {
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
