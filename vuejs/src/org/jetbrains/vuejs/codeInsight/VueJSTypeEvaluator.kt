// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSFieldVariable
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.resolve.*
import com.intellij.lang.javascript.psi.types.primitives.JSNumberType
import com.intellij.lang.javascript.psi.types.primitives.JSStringType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
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
    if (vForExpression.getVarStatement()?.variables?.indexOf(jsVariable) != 0) return false

    pushDestructuringContext(jsVariable)
    val expression = myContext.processedExpression
    when (val collectionType = JSResolveUtil.getElementJSType(vForExpression.getCollectionExpression())) {
      is JSStringType -> addType(collectionType, expression)
      is JSNumberType -> addType(collectionType, expression)
      else -> getComponentTypeFromArrayExpression(expression, vForExpression.getCollectionExpression())
        .forEach { addType(it, expression) }
    }
    restoreEvaluationContextApplingElementsSize(myContext.jsElementsToApply.size)
    myContext.finishEvaluationWithStrictness(myContext.isStrict)
    return true
  }

  companion object {
    fun resolveEventType(@Suppress("UNUSED_PARAMETER") attribute: XmlAttribute): JSType? {
      // TODO resolve event type
      return null
    }
  }
}
