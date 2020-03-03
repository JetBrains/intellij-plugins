package com.intellij.lang.javascript.uml.actions;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.util.PlatformIcons;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public class FlexCreateMethodFromDiagramAction extends JSCreateMethodActionBase {
  public FlexCreateMethodFromDiagramAction() {
    super(JSBundle.messagePointer("new.method.action.text"), JSBundle.messagePointer("new.method.action.description"), PlatformIcons.METHOD_ICON);
  }

  @Override
  protected boolean isForceConstructor() {
    return false;
  }

  @Override
  public String getActionName() {
    return JSBundle.message("new.method.action.description");
  }
}
