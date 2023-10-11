// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat.CODE
import com.intellij.lang.javascript.validation.JSProblemReporter
import com.intellij.lang.javascript.validation.JSReferenceChecker
import com.intellij.lang.javascript.validation.JSTypeChecker
import com.intellij.lang.javascript.validation.TypeScriptReferenceChecker
import com.intellij.lang.typescript.validation.TypeScriptTypeChecker
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_PIPE
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.inspections.quickfixes.CreateComponentFieldIntentionAction
import org.angular2.inspections.quickfixes.CreateComponentMethodIntentionAction
import org.angular2.inspections.quickfixes.CreateComponentSignalIntentionAction
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.lang.html.psi.Angular2HtmlPropertyBinding
import org.angular2.signals.Angular2SignalUtils

class Angular2AnalysisHandlersFactory : TypeScriptAnalysisHandlersFactory() {

  override fun getInspectionSuppressor(): InspectionSuppressor {
    return Angular2InspectionSuppressor
  }

  override fun <T : Any?> getTypeChecker(problemReporter: JSProblemReporter<T>): JSTypeChecker =
    object : TypeScriptTypeChecker(problemReporter) {

      override fun getFixes(expr: JSExpression?,
                            declaredJSType: JSType,
                            elementToChangeTypeOf: PsiElement?,
                            expressionJSType: JSType,
                            context: ProcessingContext?,
                            holder: DialectOptionHolder?): Collection<LocalQuickFix> {
        val quickFixes = super.getFixes(expr, declaredJSType, elementToChangeTypeOf, expressionJSType, context, holder)
        if (elementToChangeTypeOf is Angular2HtmlPropertyBinding) {
          return Angular2FixesFactory.getCreateInputTransformFixes(elementToChangeTypeOf,
                                                                   expressionJSType.substitute().getTypeText(CODE)) + quickFixes
        }
        return quickFixes
      }
    }

  override fun getReferenceChecker(reporter: JSProblemReporter<*>): JSReferenceChecker =
    object : TypeScriptReferenceChecker(reporter) {
      override fun addCreateFromUsageFixesForCall(methodExpression: JSReferenceExpression,
                                                  isNewExpression: Boolean,
                                                  resolveResults: Array<ResolveResult>,
                                                  quickFixes: MutableList<in LocalQuickFix>) {
        if (methodExpression is Angular2PipeReferenceExpression) {
          // TODO Create pipe from usage
          return
        }
        val qualifier = methodExpression.qualifier
        if (qualifier == null || qualifier is JSThisExpression) {
          val componentClass = Angular2ComponentLocator.findComponentClass(methodExpression)
          if (componentClass != null && methodExpression.referenceName != null) {
            quickFixes.add(CreateComponentMethodIntentionAction(methodExpression))
            if (Angular2SignalUtils.supportsSignals(componentClass)) {
              quickFixes.add(CreateComponentSignalIntentionAction(methodExpression))
            }
          }
          return
        }
        super.addCreateFromUsageFixesForCall(methodExpression, isNewExpression, resolveResults, quickFixes)
      }

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

      override fun reportUnresolvedReference(resolveResults: Array<ResolveResult>,
                                             referenceExpression: JSReferenceExpression,
                                             quickFixes: MutableList<LocalQuickFix>,
                                             @InspectionMessage message: String,
                                             isFunction: Boolean,
                                             inTypeContext: Boolean) {
        if (referenceExpression is Angular2PipeReferenceExpression) {
          Angular2FixesFactory.addUnresolvedDeclarationFixes(referenceExpression, quickFixes)
          // todo reject core TS quickFixes
        }
        super.reportUnresolvedReference(resolveResults, referenceExpression, quickFixes, message, isFunction, inTypeContext)
      }

      override fun addCreateFromUsageFixes(referenceExpression: JSReferenceExpression,
                                           resolveResults: Array<ResolveResult>,
                                           quickFixes: MutableList<in LocalQuickFix>,
                                           inTypeContext: Boolean,
                                           ecma: Boolean): Boolean {
        val qualifier = referenceExpression.qualifier
        if (qualifier == null || qualifier is JSThisExpression) {
          val componentClass = Angular2ComponentLocator.findComponentClass(referenceExpression)
          if (componentClass != null && referenceExpression.referenceName != null) {
            quickFixes.add(CreateComponentFieldIntentionAction(referenceExpression))
          }
          return inTypeContext
        }
        return super.addCreateFromUsageFixes(referenceExpression, resolveResults, quickFixes, inTypeContext, ecma)
      }

    }

}
