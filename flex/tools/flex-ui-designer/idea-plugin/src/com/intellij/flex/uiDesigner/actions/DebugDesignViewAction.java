package com.intellij.flex.uiDesigner.actions;

import com.intellij.flex.uiDesigner.DebugPathManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;

public class DebugDesignViewAction extends RunDesignViewAction {
  @Override
  protected boolean isDebug() {
    return true;
  }

  @Override
  public void update(AnActionEvent event) {
    if (ApplicationManager.getApplication().isInternal() || DebugPathManager.IS_DEV) {
      super.update(event);
    }
    else {
      event.getPresentation().setVisible(false);
    }
  }
}
