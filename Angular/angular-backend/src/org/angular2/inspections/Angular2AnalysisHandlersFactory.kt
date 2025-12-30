// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat.CODE
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.lang.javascript.validation.TypeScriptReferenceChecker
import com.intellij.lang.typescript.validation.TypeScriptTypeChecker
import com.intellij.psi.PsiElement
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_PIPE
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2Binding
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression

class Angular2AnalysisHandlersFactory : TypeScriptAnalysisHandlersFactory() {

  override fun <T : Any?> getTypeChecker(problemReporter: JSProblemReporter<T>): JSTypeChecker =
    object : TypeScriptTypeChecker(problemReporter) {

      override fun getFixes(
        expr: JSExpression?,
        declaredJSType: JSType,
        elementToChangeTypeOf: PsiElement?,
        expressionJSType: JSType,
        context: ProcessingContext?,
        holder: DialectOptionHolder?,
      ): Collection<LocalQuickFix> {
        val quickFixes = super.getFixes(expr, declaredJSType, elementToChangeTypeOf, expressionJSType, context, holder)
        expr?.parent?.asSafely<Angular2Binding>()?.enclosingAttribute?.let {
          return Angular2FixesFactory.getCreateInputTransformFixes(it, expressionJSType.substitute(expr).getTypeText(CODE)) + quickFixes
        }
        return quickFixes
      }
    }

  override fun getReferenceChecker(reporter: JSProblemReporter<*>): JSReferenceChecker =
    object : TypeScriptReferenceChecker(reporter) {

      @InspectionMessage
      override fun createUnresolvedCallReferenceMessage(methodExpression: JSReferenceExpression, isNewExpression: Boolean): String {
        return if (methodExpression is Angular2PipeReferenceExpression) {
          Angular2Bundle.htmlMessage(
            "angular.inspection.unresolved-pipe.message",
            methodExpression.getReferenceName()!!.withColor(NG_PIPE, methodExpression)
          )
        }
        else super.createUnresolvedCallReferenceMessage(methodExpression, isNewExpression)
      }
    }
}