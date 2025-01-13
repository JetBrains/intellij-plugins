// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.findUsages

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.findUsages.JSDefaultReadWriteAccessDetector
import com.intellij.lang.javascript.findUsages.JSDialectSpecificReadWriteAccessDetector
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.html.psi.Angular2HtmlBananaBoxBinding
import org.angular2.lang.html.psi.Angular2HtmlBoundAttribute
import org.angular2.signals.Angular2SignalUtils

object Angular2ReadWriteAccessDetector : JSDefaultReadWriteAccessDetector(), JSDialectSpecificReadWriteAccessDetector {
  override fun getExpressionAccess(expression: PsiElement): ReadWriteAccessDetector.Access {
    val result = super.getExpressionAccess(expression)
    if (result == ReadWriteAccessDetector.Access.Read && expression.parent is Angular2Binding) {
      val attr = PsiTreeUtil.findFirstParent(expression) { it is Angular2HtmlBoundAttribute }
      if (attr is Angular2HtmlBananaBoxBinding
          && !withTypeEvaluationLocation(expression) { Angular2SignalUtils.isSignal(expression, expression, writable = true) }) {
        return ReadWriteAccessDetector.Access.ReadWrite
      }
    }
    return result
  }
}
