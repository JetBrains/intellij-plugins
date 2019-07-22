// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.codeInsight

import com.intellij.lang.javascript.psi.JSFieldVariable
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluationHelper
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.vuejs.lang.expr.VueVForExpression
import org.jetbrains.vuejs.lang.expr.VueVForVariable

class VueJSTypeEvaluator(context: JSEvaluateContext, processor: JSTypeProcessor, helper: JSTypeEvaluationHelper)
  : JSTypeEvaluator(context, processor, helper) {
  override fun addTypeFromVariableResolveResult(jsVariable: JSFieldVariable) {
    if (jsVariable is VueVForVariable) {
      val vForExpression = PsiTreeUtil.getParentOfType(jsVariable, VueVForExpression::class.java)
      if (vForExpression != null) {
        pushDestructuringContext(jsVariable)
        val expression = myContext.processedExpression
        val types = getComponentTypeFromArrayExpression(expression, vForExpression.getCollectionExpression())
        for (type in types) {
          addType(type, expression)
        }
        restoreEvaluationContextApplingElementsSize(myContext.jsElementsToApply.size)
        myContext.finishEvaluationWithStrictness(myContext.isStrict)
        return
      }
    }
    super.addTypeFromVariableResolveResult(jsVariable)
  }
}
