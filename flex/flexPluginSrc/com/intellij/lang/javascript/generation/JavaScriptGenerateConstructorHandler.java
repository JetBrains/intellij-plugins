package com.intellij.lang.javascript.generation;

import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.validation.JSAnnotatingVisitor;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class JavaScriptGenerateConstructorHandler extends BaseJSGenerateHandler {

  protected String getTitleKey() {
    return "generate.constructor.fields.chooser.title";
  }

  protected BaseCreateMethodsFix createFix(final JSClass jsClass) {
    return new BaseCreateMethodsFix<JSVariable>(jsClass) {
      @Override
      public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
        final JSCodeStyleSettings codeStyleSettings =
          CodeStyleSettingsManager.getSettings(project).getCustomSettings(JSCodeStyleSettings.class);
        evalAnchor(editor, file);
        @NonNls String functionText = "public function " + jsClass.getName() + "(";
        @NonNls String initialization = "";
        boolean first = true;
        final String semicolon = JSChangeUtil.getSemicolon(project);

        Set<JSVariable> toProcess = getElementsToProcess();
        Iterator<JSVariable> variableIterator = toProcess.iterator();
        boolean hadSuperClassConstructorInitializationBefore = false;

        while (variableIterator.hasNext()) {
          JSVariable var = variableIterator.next();
          if (!first) {
            functionText += ", ";
          }

          first = false;

          final String name = var.getName();
          String parameterName = JSResolveUtil.transformVarNameToAccessorName(name, codeStyleSettings);

          final String typeString = var.getTypeString();
          functionText += parameterName + (typeString != null ? ":" + typeString : "");

          if (JSResolveUtil.findParent(var) == jsClass) {
            if (hadSuperClassConstructorInitializationBefore) {
              initialization += ")" + semicolon + "\n";
              hadSuperClassConstructorInitializationBefore = false;
            }
            initialization += (parameterName.equals(name) ? "this." : "") + name + " = " + parameterName + semicolon + "\n";
          }
          else {
            if (hadSuperClassConstructorInitializationBefore) {
              initialization += ", ";
            }
            else {
              initialization += "super(";
            }
            initialization += parameterName;
            hadSuperClassConstructorInitializationBefore = true;
          }
        }

        if (hadSuperClassConstructorInitializationBefore) initialization += ")" + semicolon + "\n";
        functionText += ") {\n";
        functionText += initialization;
        functionText += "}";
        doAddOneMethod(project, functionText, anchor);
      }

      @Override
      public Set<JSVariable> getElementsToProcess() {
        LinkedHashSet<JSVariable> vars = new LinkedHashSet<JSVariable>();
        JSFunction nontrivialSuperClassConstructor = JSAnnotatingVisitor.getNontrivialSuperClassConstructor(jsClass);

        if (nontrivialSuperClassConstructor != null) {
          ContainerUtil.addAll(vars, nontrivialSuperClassConstructor.getParameterList().getParameters());
        }
        vars.addAll(super.getElementsToProcess());
        return vars;
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
