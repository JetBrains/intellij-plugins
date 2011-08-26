package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.openapi.ui.popup.PopupStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

abstract class AddItemPopupAction implements Runnable {
  private final String myTitle;
  private final Icon myIcon;
  private final int myIndex;

  public AddItemPopupAction(int index, String title, Icon icon) {
    myTitle = title;
    myIcon = icon;
    myIndex = index;
  }

  public boolean hasSubStep() {
    return false;
  }

  @Nullable
  public PopupStep createSubStep() {
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
