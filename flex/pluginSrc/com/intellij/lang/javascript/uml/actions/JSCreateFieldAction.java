package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.UIBundle;
import com.intellij.util.Icons;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public class JSCreateFieldAction extends NewJSMemberActionBase {
  public JSCreateFieldAction() {
    super(JSBundle.message("new.field.action.text"), JSBundle.message("new.field.action.description"), Icons.FIELD_ICON);
  }

  @Override
  public boolean isEnabledOn(Object o) {
    return super.isEnabledOn(o) && !((JSClass)o).isInterface();
  }

  public Runnable prepare(final Object element, DiagramBuilder builder) {
    final JSClass clazz = ((JSClass)element);
    if (!JSRefactoringUtil.checkReadOnlyStatus(clazz, null, getTemplatePresentation().getText())) return null;

    final JSCreateFieldDialog d = new JSCreateFieldDialog(clazz);
    d.show();

    if (!d.isOK()) {
      return null;
    }

    return new Runnable() {
      @Override
      public void run() {
        if (d.getFieldType().contains(".")) {
          ImportUtils.doImport(clazz, d.getFieldType(), false);
        }
        StringBuilder var = new StringBuilder(JSVisibilityUtil.getVisibilityKeyword(JSAttributeList.AccessType.valueOf(d.getVisibility())));
        var.append(" ");
        if (d.isStatic()) {
          var.append("static ");
        }
        var.append(d.isConstant() ? "const " : "var ");
        var.append(d.getFieldName()).append(":").append(d.getFieldType());

        if (StringUtil.isNotEmpty(d.getInitializer())) {
          var.append("=").append(d.getInitializer());
        }
        var.append(JSChangeUtil.getSemicolon(clazz.getProject()));

        JSVarStatement varStatement = (JSVarStatement)JSChangeUtil.createStatementFromText(clazz.getProject(), var.toString(),
                                                                                           JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
        JSRefactoringUtil.addMemberToTargetClass(clazz, varStatement);
        new ECMAScriptImportOptimizer().processFile(clazz.getContainingFile()).run();
      }
    };
  }

  @Override
  public String getActionName() {
    return JSBundle.message("new.field.action.description");
  }
}
