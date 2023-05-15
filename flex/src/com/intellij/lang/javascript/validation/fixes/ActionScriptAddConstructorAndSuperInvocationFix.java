// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.ECMAScriptImportOptimizer;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameterListElement;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSChangeUtil;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

public class ActionScriptAddConstructorAndSuperInvocationFix implements IntentionAction {
  private final SmartPsiElementPointer<JSClass> myClass;
  private final SmartPsiElementPointer<JSFunction> mySuperConstructor;

  public ActionScriptAddConstructorAndSuperInvocationFix(@NotNull JSClass clazz, @NotNull JSFunction _superCall) {
    myClass = SmartPointerManager.createPointer(clazz);
    mySuperConstructor = SmartPointerManager.createPointer(_superCall);
  }

  public static String getConstructorText(final JSClass jsClass, final JSFunction superConstructor, Collection<? super String> toImport) {
    final JSAttributeList attributeList = jsClass.getAttributeList();
    StringBuilder fun = new StringBuilder();

    if (attributeList != null && attributeList.getAccessType() == JSAttributeList.AccessType.PUBLIC) {
      fun.append(JSVisibilityUtil.getVisibilityKeyword(JSAttributeList.AccessType.PUBLIC)).append(" ");
    }

    fun.append("function ").append(jsClass.getName()).append("(");
    JSParameterListElement[] parameters = superConstructor.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) {
        fun.append(",");
      }
      if (parameters[i].isRest()) {
        fun.append("...").append(parameters[i].getName());
      }
      else {
        fun.append(parameters[i].getName());
        JSType jsType = parameters[i].getJSType();
        if (jsType != null) {
          String type = jsType.getResolvedTypeText();
          if (ImportUtils.needsImport(jsClass, StringUtil.getPackageName(type))) {
            toImport.add(type);
          }
          fun.append(":").append(type);
        }
        final JSExpression initializer = parameters[i].getInitializer();
        if (initializer != null) {
          fun.append("=").append(initializer.getText());
        }
      }
    }

    fun.append("){\nsuper(");
    for (int i = 0; i < parameters.length; i++) {
      if (i > 0) fun.append(",");
      fun.append(parameters[i].getName());
    }
    fun.append(")").append(JSCodeStyleSettings.getSemicolon(jsClass)).append("\n}");
    return fun.toString();
  }

  @Override
  @NotNull
  public String getText() {
    return JavaScriptBundle.message("javascript.fix.create.constructor.invoke.super");
  }

  @Override
  @NotNull
  public String getFamilyName() {
    return getText();
  }

  @Override
  public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
    JSClass jsClass = myClass.getElement();
    JSFunction superConstructorElement = mySuperConstructor.getElement();
    return jsClass != null && superConstructorElement != null;
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
    final JSClass jsClass = myClass.getElement();
    JSFunction superConstructor = mySuperConstructor.getElement();
    assert superConstructor != null;
    assert jsClass != null;

    Collection<String> toImport = new HashSet<>();
    final String text = getConstructorText(jsClass, superConstructor, toImport);
    jsClass.add(JSChangeUtil.createJSTreeFromText(jsClass.getProject(), text, JavaScriptSupportLoader.ECMA_SCRIPT_L4).getPsi());
    if (!toImport.isEmpty()) {
      ImportUtils.insertImportStatements(jsClass, toImport);
      new ECMAScriptImportOptimizer().processFile(jsClass.getContainingFile()).run();
    }
  }

  @Override
  public boolean startInWriteAction() {
    return true;
  }
}
