// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSThisExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.types.JSAnyType;
import com.intellij.lang.javascript.psi.types.JSTypeSource;
import com.intellij.lang.javascript.psi.types.JSTypeSourceFactory;
import com.intellij.lang.javascript.psi.types.JSUnknownType;
import com.intellij.lang.javascript.psi.types.evaluable.JSApplyCallType;
import com.intellij.lang.javascript.psi.types.evaluable.JSQualifiedReferenceType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2ComponentLocator;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression;
import org.angular2.lang.expr.psi.Angular2PipeExpression;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.angular2.lang.types.Angular2TypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Angular2TypeEvaluator extends TypeScriptTypeEvaluator {

  public Angular2TypeEvaluator(@NotNull JSEvaluateContext context) {
    super(context);
  }

  @Override
  protected void evaluateCallExpressionTypes(@NotNull JSCallExpression callExpression) {
    if (callExpression instanceof Angular2PipeExpression) {
      evaluatePipeExpressionTypes((Angular2PipeExpression)callExpression);
    }
    else {
      super.evaluateCallExpressionTypes(callExpression);
    }
  }

  private void evaluatePipeExpressionTypes(@NotNull Angular2PipeExpression pipeExpression) {
    JSExpression methodExpression = pipeExpression.getMethodExpression();
    String name = pipeExpression.getName();
    if (methodExpression == null || name == null) return;

    Angular2DeclarationsScope scope = new Angular2DeclarationsScope(methodExpression);
    Angular2Pipe pipe = ContainerUtil.find(
      Angular2EntitiesProvider.findPipes(pipeExpression.getProject(), pipeExpression.getName()),
      scope::contains
    );
    if (pipe == null) return;

    var jsClass = pipe.getTypeScriptClass();
    if (jsClass == null) return;

    var typeSource = JSTypeSourceFactory.createTypeSource(pipeExpression, true);
    var instanceMethod = new JSQualifiedReferenceType(Angular2EntitiesProvider.TRANSFORM_METHOD, jsClass.getJSType(), typeSource);
    var type = new JSApplyCallType(instanceMethod, typeSource);
    addType(type);
  }

  @Override
  protected void addTypeFromElementResolveResult(@Nullable PsiElement resolveResult) {
    if (resolveResult instanceof Angular2TemplateBindings) {
      JSType type = Angular2TypeUtils.getTemplateBindingsContextType((Angular2TemplateBindings)resolveResult);
      if (type != null) {
        addType(type);
      }
    }
    else {
      super.addTypeFromElementResolveResult(resolveResult);
    }
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
