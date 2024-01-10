// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.childrenOfType
import org.angular2.codeInsight.Angular2HighlightingUtils.TextAttributesKind.NG_DEFER_TRIGGER
import org.angular2.codeInsight.Angular2HighlightingUtils.withColor
import org.angular2.codeInsight.blocks.BLOCK_DEFER
import org.angular2.codeInsight.blocks.PARAMETER_ON
import org.angular2.codeInsight.blocks.getDeferOnTriggerDefinition
import org.angular2.lang.Angular2Bundle
import org.angular2.lang.expr.psi.Angular2DeferredTimeLiteralExpression
import org.angular2.lang.html.psi.Angular2HtmlBlock
import org.angular2.lang.html.psi.Angular2HtmlElementVisitor

class AngularDeferBlockOnTriggerInspection : LocalInspectionTool() {
  override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor =
    object : Angular2HtmlElementVisitor() {

      override fun visitBlock(block: Angular2HtmlBlock) {
        if (block.name == BLOCK_DEFER) {
          block.parameters
            .filter { it.name == PARAMETER_ON }
            .forEach { parameter ->
              val trigger = parameter.childrenOfType<JSReferenceExpression>().firstOrNull()
              if (trigger == null) return@forEach
              val triggerDefinition = getDeferOnTriggerDefinition(parameter)
                                      ?: return@forEach
              val argument = parameter.children
                .lastOrNull { it is Angular2DeferredTimeLiteralExpression || it is JSReferenceExpression }
                ?.takeIf { it is Angular2DeferredTimeLiteralExpression || it != trigger }
              if (triggerDefinition.properties["parameter-required"] == true) {
                if (argument == null) {
                  holder.registerProblem(
                    trigger,
                    Angular2Bundle.htmlMessage("angular.inspection.defer-block-on-trigger.message.argument-required",
                                               trigger.referenceName!!.withColor(NG_DEFER_TRIGGER, block)))
                }
              }
              when (triggerDefinition.properties["parameter"]) {
                "template-reference-variable" ->
                  if (argument != null && argument !is JSReferenceExpression) {
                    holder.registerProblem(
                      argument,
                      Angular2Bundle.htmlMessage("angular.inspection.defer-block-on-trigger.message.template-var-ref-required",
                                                 trigger.referenceName!!.withColor(NG_DEFER_TRIGGER, block)))
                  }
                "time-duration" ->
                  if (argument != null && argument !is Angular2DeferredTimeLiteralExpression) {
                    holder.registerProblem(
                      argument,
                      Angular2Bundle.htmlMessage("angular.inspection.defer-block-on-trigger.message.time-duration-required",
                                                 trigger.referenceName!!.withColor(NG_DEFER_TRIGGER, block)))
                  }
                else ->
                  if (argument != null) {
                    holder.registerProblem(
                      argument,
                      Angular2Bundle.htmlMessage("angular.inspection.defer-block-on-trigger.message.argument-not-supported",
                                                 trigger.referenceName!!.withColor(NG_DEFER_TRIGGER, block)))
                  }
              }
            }
        }
      }
    }
}