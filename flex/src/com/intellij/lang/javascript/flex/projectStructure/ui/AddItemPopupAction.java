// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.ui.popup.PopupStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

abstract class AddItemPopupAction implements Runnable {
  private final String myTitle;
  private final Icon myIcon;
  private final int myIndex;

  AddItemPopupAction(int index, String title, Icon icon) {
    myTitle = title;
    myIcon = icon;
    myIndex = index;
  }

  public boolean hasSubStep() {
    return false;
  }

  public @Nullable PopupStep createSubStep() {
    return null;
  }

  public String getTitle() {
    return myTitle;
  }

  public Icon getIcon() {
    return myIcon;
  }

  public int getIndex() {
    return myIndex;
  }
}
