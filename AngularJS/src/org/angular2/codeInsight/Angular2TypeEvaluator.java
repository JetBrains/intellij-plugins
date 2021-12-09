// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSUnknownType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.types.Angular2TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TypeEvaluator extends TypeScriptTypeEvaluator {

  public Angular2TypeEvaluator(@NotNull JSEvaluateContext context) {
    super(context);
  }

  @Override
  public boolean addTypeFromResolveResult(String referenceName, ResolveResult resolveResult) {
    PsiElement psiElement = resolveResult.getElement();
    if (resolveResult instanceof Angular2ComponentPropertyResolveResult && psiElement != null) {
      startEvaluationWithContext(myContext.withSource(psiElement));
      addType(((Angular2ComponentPropertyResolveResult)resolveResult).getJSType());
      return true;
    }
    super.addTypeFromResolveResult(referenceName, resolveResult);
    return true;
  }

  @Override
  protected void doAddType(@NotNull JSType type) {
    if (type instanceof JSUnknownType) {
      // convert unknown to any to have less strict type validation in Angular
      type = JSAnyType.get(type.getSource());
    }
    super.doAddType(type);
  }

  @Override
  protected void processThisQualifierInExecutionScope(@NotNull JSThisExpression thisQualifier, PsiElement thisScope) {
    if (thisScope instanceof Angular2EmbeddedExpression) {
      TypeScriptClass componentClass = Angular2ComponentLocator.findComponentClass(thisQualifier);
      if (componentClass != null) {
        addType(componentClass.getJSType());
      }
      else {
        addType(JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, true));
      }
      return;
    }
    super.processThisQualifierInExecutionScope(thisQualifier, thisScope);
  }
}
