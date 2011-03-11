package com.intellij.lang.javascript.uml.actions;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.util.Icons;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public class JSCreateMethodAction extends JSCreateMethodActionBase {
  public JSCreateMethodAction() {
    super(JSBundle.message("new.method.action.text"), JSBundle.message("new.method.action.description"), Icons.METHOD_ICON);
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
