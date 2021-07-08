package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSVarStatement;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public class FlexCreateFieldFromDiagramAction extends NewJSMemberActionBase {
  public FlexCreateFieldFromDiagramAction() {
    super(JavaScriptBundle.messagePointer("new.field.action.text"), JavaScriptBundle.messagePointer("new.field.action.description"), PlatformIcons.FIELD_ICON);
  }

  @Override
  public boolean isEnabledOn(Object o) {
    return super.isEnabledOn(o) && !((JSClass)o).isInterface();
  }

  @Override
  public Runnable prepare(final Object element, DiagramBuilder builder) {
    final JSClass clazz = ((JSClass)element);
    if (!JSRefactoringUtil.checkReadOnlyStatus(clazz, null, getTemplatePresentation().getText())) return null;

    final JSCreateFieldDialog d = new JSCreateFieldDialog(clazz);
    if (!d.showAndGet()) {
      return null;
    }

    return () -> {
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
      var.append(JSCodeStyleSettings.getSemicolon(clazz));

      JSVarStatement varStatement = (JSVarStatement)JSChangeUtil.createStatementFromText(clazz.getProject(), var.toString(),
                                                                                         JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi();
      JSRefactoringUtil.addMemberToTargetClass(clazz, varStatement);
      new ECMAScriptImportOptimizer().processFile(clazz.getContainingFile()).run();
    };
  }

  @Override
  public @NotNull String getActionName() {
    return JavaScriptBundle.message("new.field.action.description");
  }
}
