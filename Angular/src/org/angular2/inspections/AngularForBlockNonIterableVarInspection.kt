// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.evaluation.JSTypeEvaluationLocationProvider.withTypeEvaluationLocation
import com.intellij.lang.javascript.psi.JSFunctionType
import com.intellij.lang.javascript.psi.JSType.TypeTextFormat.CODE
import com.intellij.lang.javascript.psi.JSTypeUtils
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory
import com.intellij.lang.javascript.psi.types.JSTypeComparingContextService.LOCATION
import com.intellij.lang.javascript.psi.types.JSTypeContext
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.lang.javascript.validation.JSTooltipWithHtmlHighlighter
import com.intellij.psi.PsiElementVisitor
import com.intellij.util.ProcessingContext
import com.intellij.util.asSafely
import org.angular2.codeInsight.blocks.BLOCK_FOR
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

class AngularForBlockNonIterableVarInspection : LocalInspectionTool() {

  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : Angular2HtmlElementVisitor() {

      override fun visitBlock(block: Angular2HtmlBlock) {
        if (block.getName() == BLOCK_FOR) {
          withTypeEvaluationLocation(block) {
            val expression = block.parameters.getOrNull(0)
              ?.takeIf { it.isPrimaryExpression }
              ?.expression
            val expressionType = expression
                                   ?.let { JSResolveUtil.getExpressionJSType(it) }
                                   ?.substitute()
                                   ?.let { JSTypeUtils.removeNullableComponents(it) }
                                   ?.takeIf { !JSTypeUtils.isAnyType(it) }
                                 ?: return@withTypeEvaluationLocation
            val property = expressionType
              .asRecordType(block)
              .let {
                it.findPropertySignature("[Symbol.iterator]")
                ?: it.findPropertySignature("SymbolConstructor.iterator")
              }
            val type = property
              ?.jsType
              ?.substitute()
              ?.asSafely<JSFunctionType>()
              ?.returnType
            if (property == null
                || (type != null && !JSNamedTypeFactory
                .createType("Iterator", JSTypeSource.EMPTY_TS, JSTypeContext.STATIC)
                .isDirectlyAssignableType(type, ProcessingContext().apply { put(LOCATION, block) }))) {
              holder.registerProblem(expression, Angular2Bundle.htmlMessage(
                "angular.inspection.for-block-non-iterable.message.non-iterable-type",
                JSTooltipWithHtmlHighlighter.highlightTypeOrStmt(block.project, expressionType.getTypeText(CODE)),
                JSTooltipWithHtmlHighlighter.highlightTypeOrStmt(block.project, "[Symbol.iterator]()")),
                                     ProblemHighlightType.GENERIC_ERROR_OR_WARNING)
            }
          }
        }
      }
    }

}