package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.psi.ecmal4.impl.JSClassImpl;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.psi.PsiDirectory;

public class NewActionScriptClassAction extends NewJSClassActionBase {

  public NewActionScriptClassAction() {
    super(JSBundle.message("new.actionscript.class.action.title"), JSBundle.message("new.actionscript.class.action.description"),
          JSClassImpl.CLASS_ICON, CreateClassOrInterfaceAction.ACTIONSCRIPT_TEMPLATES_EXTENSIONS);
  }

  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return JSBundle.message("new.actionscript.class.command.name");
  }

  @Override
  protected String getDialogTitle() {
    return JSBundle.message("new.actionscript.class.dialog.title");
  }

}
