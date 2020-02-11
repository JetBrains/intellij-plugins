package com.intellij.lang.javascript.uml.actions;

import com.intellij.diagram.DiagramBuilder;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.refactoring.FormatFixer;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.refactoring.changeSignature.JSParameterInfo;
import com.intellij.lang.javascript.refactoring.util.JSRefactoringUtil;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Konstantin Bulenkov
 * @author Kirill Safonov
 */
public abstract class JSCreateMethodActionBase extends NewJSMemberActionBase {
  protected static final JSAttributeList.AccessType PUBLIC = JSAttributeList.AccessType.PUBLIC;

  protected JSCreateMethodActionBase(@NotNull Supplier<String> name, @NotNull Supplier<String> description, Icon icon) {
    super(name, description, icon);
  }

  protected abstract boolean isForceConstructor();

  @Override
  public Runnable prepare(Object element, DiagramBuilder builder) {
    final JSClass clazz = (JSClass)element;
    if (!JSRefactoringUtil.checkReadOnlyStatus(clazz, null, getTemplatePresentation().getText())) return null;
    final JSFunction fakeMethod = JSCreateMethodDialog.createFakeMethod(clazz, createFakeMethodText(clazz), false);
    final JSCreateMethodDialog dialog = new JSCreateMethodDialog(clazz, fakeMethod, isForceConstructor());
    if (!dialog.showAndGet()) {
      return null;
    }

    return () -> {
      final JSFunction method = dialog.createMethod();
      importType(clazz, dialog.getReturnTypeText());
      for (JSParameterInfo param : dialog.getParameters()) {
        importType(clazz, param.getTypeText());
      }

      final PsiElement added = JSRefactoringUtil.addMemberToTargetClass(clazz, method);
      final List<FormatFixer> formatters = new ArrayList<>();
      formatters.add(FormatFixer.create(added, FormatFixer.Mode.Reformat));
      formatters.addAll(ECMAScriptImportOptimizer.executeNoFormat(clazz.getContainingFile()));
      FormatFixer.fixAll(FormatFixer.merge(formatters));
    };
  }

  protected String createFakeMethodText(JSClass clazz) {
    final String visibility = clazz.isInterface() ? "" : JSVisibilityUtil.getVisibilityKeyword(PUBLIC);
    return visibility + " function ()";
  }

  private static void importType(JSClass clazz, String typeText) {
    if (typeText.contains(".")) {
      ImportUtils.doImport(clazz, typeText, false);
    }
  }
}
