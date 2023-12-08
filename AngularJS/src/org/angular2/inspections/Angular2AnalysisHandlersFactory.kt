// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections

import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder
import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.DialectOptionHolder
import com.intellij.lang.javascript.JSTokenTypes
import com.intellij.lang.javascript.ecmascript6.TypeScriptAnalysisHandlersFactory
import com.intellij.lang.javascript.highlighting.TypeScriptHighlighter
import com.intellij.lang.javascript.psi.JSExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat.CODE
import com.intellij.lang.javascript.validation.*
import com.intellij.lang.typescript.validation.TypeScriptTypeChecker
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.childLeafs
import com.intellij.psi.util.elementType
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_PIPE
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.blocks.BLOCK_FOR
import org.angular2.entities.Angular2ComponentLocator
import org.angular2.inspections.quickfixes.*
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.*
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
            if (referenceExpression.parentOfType<Angular2EmbeddedExpression>() is Angular2Action) {
              quickFixes.add(CreateDirectiveOutputIntentionAction(referenceExpression, referenceExpression.referenceName!!))
            }
          }
          return inTypeContext
        }
        return super.addCreateFromUsageFixes(referenceExpression, resolveResults, quickFixes, inTypeContext, ecma)
      }

    }

  override fun createKeywordHighlighterVisitor(holder: HighlightInfoHolder,
                                               dialectOptionHolder: DialectOptionHolder): JSKeywordHighlighterVisitor =
    object : TypeScriptKeywordHighlighterVisitor(holder) {
      override fun visitElement(element: PsiElement) {
        when (element) {
          is Angular2BlockParameter -> if (element.block?.getName() == BLOCK_FOR && element.isPrimaryExpression)
            element.node.findChildByType(JSTokenTypes.IDENTIFIER)
              ?.let { highlightKeyword(it, TypeScriptHighlighter.TS_KEYWORD) }
              ?.let { myHolder.add(it) }
          is Angular2DeferredTimeLiteralExpression -> element.childLeafs
            .find { it.elementType == JSTokenTypes.IDENTIFIER }
            ?.takeIf { it.text == "s" || it.text == "ms" }
            ?.let { highlightKeyword(it.node, TypeScriptHighlighter.TS_NUMBER) }
            ?.let { myHolder.add(it) }
          else -> super.visitElement(element)
        }
      }
    }

}
