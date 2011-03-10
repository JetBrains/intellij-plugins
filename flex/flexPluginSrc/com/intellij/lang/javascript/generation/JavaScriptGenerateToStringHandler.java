package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Set;

class JavaScriptGenerateToStringHandler extends BaseJSGenerateHandler {

  protected String getTitleKey() {
    return "generate.to.string.chooser.title";
  }

  protected BaseCreateMethodsFix createFix(final JSClass jsClass) {

    return new BaseCreateMethodsFix<JSVariable>(jsClass) {
      @Override
      public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        evalAnchor(editor, file);

        final boolean[] needOverride = new boolean[1];
        JSResolveUtil.processOverrides(jsClass, new JSResolveUtil.OverrideHandler() {
          public boolean process(final ResolveProcessor processor, final PsiElement scope, final String className) {
            needOverride[0] = !"Object".equals(className);
            return false;
          }
        }, "toString", null, myJsClass);

        @NonNls String functionText = "public " +
                                      (needOverride[0] ? "override " : "") +
                                      "function toString():String {\nreturn " +
                                      (needOverride[0] ? "super.toString()" : "\"" + jsClass.getName());

        final String semicolon = JSChangeUtil.getSemicolon(project);

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


  protected void collectCandidates(final JSClass clazz, final Collection<JSNamedElementNode> candidates) {
    collectJSVariables(clazz, candidates, false, false, true);
  }

  protected boolean canHaveEmptySelectedElements() {
    return true;
  }
}