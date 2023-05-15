// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight

import com.intellij.lang.javascript.ecmascript6.TypeScriptTypeEvaluator
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.psi.types.JSUnknownType
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression

class Angular2TypeEvaluator(context: JSEvaluateContext) : TypeScriptTypeEvaluator(context) {

  override fun addTypeFromResolveResult(referenceName: String, resolveResult: ResolveResult): Boolean {
    val psiElement = resolveResult.element
    if (resolveResult is Angular2ComponentPropertyResolveResult && psiElement != null) {
      startEvaluationWithContext(myContext.withSource(psiElement))
      addType(resolveResult.jsType)
      return true
    }
    super.addTypeFromResolveResult(referenceName, resolveResult)
    return true
  }

  override fun doAddType(type: JSType) {
    super.doAddType(
      if (type is JSUnknownType) {
        // convert unknown to any to have less strict type validation in Angular
        JSAnyType.get(type.getSource())
      }
      else type
    )
  }

  override fun processThisQualifierInExecutionScope(thisQualifier: JSThisExpression, thisScope: PsiElement) {
    if (thisScope is Angular2EmbeddedExpression) {
      addType(Angular2ComponentLocator.findComponentClass(thisQualifier)?.jsType
              ?: JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS, true))
      return
    }
    super.processThisQualifierInExecutionScope(thisQualifier, thisScope)
  }
}
