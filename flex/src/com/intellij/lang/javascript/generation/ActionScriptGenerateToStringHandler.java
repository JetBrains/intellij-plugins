package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSPsiElementBase;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSOverrideHandler;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMembersFix;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Set;

class ActionScriptGenerateToStringHandler extends BaseJSGenerateHandler {

  @Override
  protected String getTitleKey() {
    return "generate.to.string.chooser.title";
  }

  @Override
  protected BaseCreateMembersFix createFix(final PsiElement jsClass) {

    return new BaseCreateMembersFix<JSVariable>(jsClass) {
      @Override
      public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        evalAnchor(editor, file);

        final boolean[] needOverride = new boolean[1];
        JSInheritanceUtil.processOverrides((JSClass)jsClass, new JSOverrideHandler() {
          @Override
          public boolean process(@NotNull List<? extends JSPsiElementBase> elements, final PsiElement scope, final @Nullable String className) {
            needOverride[0] = !"Object".equals(className);
            return false;
          }
        }, "toString", null, myJsClass, true);

        @NonNls String functionText = "public " +
                                      (needOverride[0] ? "override " : "") +
                                      "function toString():String {\nreturn " +
                                      (needOverride[0] ? "super.toString()" : "\"" + ((JSClass)jsClass).getName());

        final String semicolon = JSCodeStyleSettings.getSemicolon(jsClass);

        boolean first = true;

        final Set<JSVariable> jsVariables = getElementsToProcess();
        if (!jsVariables.isEmpty()) {
          functionText += needOverride[0] ? " + \"{" : "{";
          for (JSVariable var : jsVariables) {
            if (!first) {
              functionText += " + \",";
            }
            first = false;

            functionText += var.getName() + "=\" + String(" + var.getName() + ")";
          }
          functionText += "+\"}\"";
        }
        else {
          if (!needOverride[0]) functionText += "\"";
        }

        functionText += semicolon + "\n}";
        doAddOneMethod(project, functionText, anchor);
      }
    };
  }


  @Override
  protected void collectCandidates(final PsiElement clazz, final Collection<JSChooserElementNode> candidates) {
    collectJSVariables(clazz, candidates, false, false, true, true,
                       true, false, false);
  }

  @Override
  protected boolean canHaveEmptySelectedElements() {
    return true;
  }

  @Override
  protected boolean isValidForTarget(PsiElement jsClass) {
    return jsClass instanceof JSClass && !((JSClass)jsClass).isInterface();
  }
}