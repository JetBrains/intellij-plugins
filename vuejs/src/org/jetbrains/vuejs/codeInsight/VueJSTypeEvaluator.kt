// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSFieldVariable
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluationHelper
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSVForVariable

class VueJSTypeEvaluator(context: JSEvaluateContext, processor: JSTypeProcessor, helper: JSTypeEvaluationHelper)
  : JSTypeEvaluator(context, processor, helper) {
  override fun addTypeFromVariableResolveResult(jsVariable: JSFieldVariable) {
    if (evaluateTypeFromVForVariable(jsVariable)) return
    super.addTypeFromVariableResolveResult(jsVariable)
  }

  private fun evaluateTypeFromVForVariable(jsVariable: JSFieldVariable): Boolean {
    if (jsVariable !is VueJSVForVariable) return false
    val vForExpression = PsiTreeUtil.getParentOfType(jsVariable, VueJSVForExpression::class.java) ?: return false
    pushDestructuringContext(jsVariable)
    val expression = myContext.processedExpression
    getComponentTypeFromArrayExpression(expression, vForExpression.getCollectionExpression()).forEach { addType(it, expression) }
    restoreEvaluationContextApplingElementsSize(myContext.jsElementsToApply.size)
    return true
  }
}
