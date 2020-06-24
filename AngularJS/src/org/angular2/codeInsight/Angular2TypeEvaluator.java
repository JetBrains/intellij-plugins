// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
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

  public Angular2TypeEvaluator(@NotNull JSEvaluateContext context,
                               @NotNull JSTypeProcessor processor) {
    super(context, processor, Angular2TypeEvaluationHelper.INSTANCE);
  }

  @Override
  protected boolean addTypeFromDialectSpecificElements(PsiElement resolveResult) {
    if (resolveResult instanceof Angular2TemplateBindings) {
      JSType type = Angular2TypeUtils.getTemplateBindingsContextType((Angular2TemplateBindings)resolveResult);
      if (type != null) {
        addType(type, resolveResult);
      }
      return true;
    }
    return super.addTypeFromDialectSpecificElements(resolveResult);
  }

  @Override
  protected boolean addTypeFromResolveResult(String referenceName, ResolveResult resolveResult) {
    PsiElement psiElement = resolveResult.getElement();
    if (resolveResult instanceof Angular2ComponentPropertyResolveResult && psiElement != null) {
      myContext.setSource(psiElement);
      addType(((Angular2ComponentPropertyResolveResult)resolveResult).getJSType(), resolveResult.getElement(), false);
      return true;
    }
    super.addTypeFromResolveResult(referenceName, resolveResult);
    return true;
  }

  @Override
  protected boolean processFunction(@NotNull JSFunction function) {
    if (!Angular2LibrariesHacks.hackSlicePipeType(this, this.myContext, function)) {
      super.processFunction(function);
    }
    return true;
  }

  @Override
  protected void doAddType(@Nullable JSType type, @Nullable PsiElement source, boolean skipGuard) {
    if (type instanceof JSUnknownType) {
      // convert unknown to any to have less strict type validation in Angular
      type = JSAnyType.get(type.getSource());
    }
    super.doAddType(type, source, skipGuard);
  }

  @Override
  protected void processThisQualifierInExecutionScope(@NotNull JSThisExpression thisQualifier, PsiElement thisScope) {
    if (thisScope instanceof Angular2EmbeddedExpression) {
      TypeScriptClass componentClass = Angular2ComponentLocator.findComponentClass(thisQualifier);
      if (componentClass != null) {
        addType(componentClass.getJSType(), thisQualifier);
      }
      else {
        addType(JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, true), thisQualifier);
      }
      return;
    }
    super.processThisQualifierInExecutionScope(thisQualifier, thisScope);
  }
}
