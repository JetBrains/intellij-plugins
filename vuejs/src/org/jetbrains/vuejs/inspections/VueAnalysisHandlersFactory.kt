// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.codeInspection.util.InspectionMessage
import com.intellij.lang.javascript.JSAnalysisHandlersFactory
import com.intellij.lang.javascript.highlighting.JSFixFactory
import com.intellij.lang.javascript.psi.*
import com.intellij.lang.javascript.validation.*
import com.intellij.openapi.util.Trinity
import com.intellij.psi.ResolveResult
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterExpression
import org.jetbrains.vuejs.lang.expr.psi.VueJSFilterReferenceExpression

class VueAnalysisHandlersFactory : JSAnalysisHandlersFactory() {

  override fun getFunctionSignatureChecker(holder: ProblemsHolder): JSFunctionSignatureChecker {
    val typeChecker = getTypeChecker(holder)
    return object : JavaScriptFunctionSignatureChecker(typeChecker) {

      override fun checkParameterLength(node: JSCallExpression,
                                        function: JSFunctionItem?,
                                        expressions: Array<JSExpression>,
                                        fixes: MutableList<LocalQuickFix>,
                                        minMaxParameters: Trinity<Int, Int, Boolean>,
                                        actualParameterLength: Int): Boolean {
        if (node is VueJSFilterExpression) {
          val min = minMaxParameters.first
          val max = minMaxParameters.second
          val argumentList = node.argumentList
          if (expressions.size in min..max) return true
          if (function is JSFunction) {
            fixes.add(JSFixFactory.getInstance().changeSignatureFix(function as JSFunction?, argumentList))
          }

          //foo(...[1, 2]) WEB-12007 do not check max length
          if (argumentList != null && argumentList.hasSpreadElement() && expressions.size < min) {
            return false //todo possible should be "return true" for ts
          }

          if (min == 0 && max == 0) {
            registerProblem(node, VueBundle.message("vue.inspection.message.filter.function.with.no.args"),
                            *fixes.toTypedArray())
          }
          else {
            val s = "${if (min == 0) 0 else min - 1}" +
                    if (minMaxParameters.third) " or more" else if (min != max) "..${max - 1}" else ""
            registerProblem(node, VueBundle.message("vue.inspection.message.filter.invalid.number.of.arguments", s), *fixes.toTypedArray())
          }
          return false
        }
        return super.checkParameterLength(node, function, expressions, fixes, minMaxParameters, actualParameterLength)
      }

      override fun registerProblem(callExpression: JSCallExpression, message: @InspectionMessage String, vararg fixes: LocalQuickFix) {
        val place = (callExpression as? VueJSFilterExpression)
                      ?.filterArgumentsList
                    ?: ValidateTypesUtil.getPlaceForSignatureProblem(callExpression, null)
        holder.registerProblem(place, message, *fixes)
      }
    }
  }

  override fun getReferenceChecker(reporter: JSProblemReporter<*>): JSReferenceChecker {
    return object : JSReferenceChecker((reporter)) {
      override fun addCreateFromUsageFixes(node: JSReferenceExpression?,
                                           resolveResults: Array<out ResolveResult>?,
                                           fixes: MutableList<LocalQuickFix>?,
                                           inTypeContext: Boolean,
                                           ecma: Boolean): Boolean {
        return inTypeContext
      }

      override fun addCreateFromUsageFixesForCall(referenceExpression: JSReferenceExpression,
                                                  isNewExpression: Boolean,
                                                  resolveResults: Array<out ResolveResult>,
                                                  quickFixes: MutableList<LocalQuickFix>) {
        if (referenceExpression is VueJSFilterReferenceExpression) {
          // TODO Create filter from usage
          return
        }
        quickFixes.add(JSFixFactory.getInstance().renameReferenceFix())
      }

      override fun createUnresolvedCallReferenceMessage(methodExpression: JSReferenceExpression,
                                                        isNewExpression: Boolean): @InspectionMessage String {
        return if (methodExpression is VueJSFilterReferenceExpression) {
          VueBundle.message("vue.inspection.message.unresolved.filter", methodExpression.referenceName!!)
        }
        else super.createUnresolvedCallReferenceMessage(methodExpression, isNewExpression)
      }
    }
  }
}
