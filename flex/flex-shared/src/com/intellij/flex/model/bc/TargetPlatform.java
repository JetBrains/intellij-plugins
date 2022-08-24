// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.flex.model.bc;

import com.intellij.ui.IconManager;
import com.intellij.ui.PlatformIcons;
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
        return IconManager.getInstance().getPlatformIcon(PlatformIcons.PpWeb);
      case Desktop:
        return FlexSharedIcons.BcDesktop;
      case Mobile:
        return FlexSharedIcons.BcMobile;
      default:
        assert false : this;
        return null;
    }
  }
}
