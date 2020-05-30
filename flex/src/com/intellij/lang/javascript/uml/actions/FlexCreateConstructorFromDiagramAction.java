package com.intellij.lang.javascript.uml.actions;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSFunctionImpl;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public class FlexCreateConstructorFromDiagramAction extends JSCreateMethodActionBase {
  public FlexCreateConstructorFromDiagramAction() {
    super(JavaScriptBundle.messagePointer("new.constructor.action.text"), JavaScriptBundle
            .messagePointer("new.constructor.action.description"),
          JSFunctionImpl.CONSTRUCTOR_ICON);
  }

  @Override
  protected boolean isForceConstructor() {
    return true;
  }

  @Override
  protected String createFakeMethodText(JSClass clazz) {
    final String visibility = JSVisibilityUtil.getVisibilityKeyword(PUBLIC);
    return visibility + " function " + clazz.getName() + "()";
  }

  @Override
  public boolean isEnabledOn(Object o) {
    if (!super.isEnabledOn(o)) return false;
    JSClass clazz = (JSClass)o;
    return !(clazz instanceof XmlBackedJSClassImpl) && !clazz.isInterface() && clazz.getConstructor() == null;
  }

  @Override
  public String getActionName() {
    return JavaScriptBundle.message("new.constructor.action.description");
  }
}
