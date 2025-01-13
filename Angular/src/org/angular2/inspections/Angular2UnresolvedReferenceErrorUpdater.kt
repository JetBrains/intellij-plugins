// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.psi.JSCallExpression
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.psi.JSThisExpression
import com.intellij.lang.javascript.validation.JSUnresolvedReferenceErrorUpdater
import com.intellij.lang.javascript.validation.JSUnresolvedReferenceErrorUpdater.ErrorInfoImpl
import com.intellij.lang.javascript.validation.fixes.BaseCreateFix
import com.intellij.psi.ResolveResult
import com.intellij.psi.util.parentOfType
import com.intellij.util.SmartList
import org.angular2.entities.source.Angular2SourceUtil
import org.angular2.inspections.quickfixes.*
import org.angular2.lang.expr.Angular2Language
import org.angular2.lang.expr.psi.Angular2Action
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression
import org.angular2.signals.Angular2SignalUtils

class Angular2UnresolvedReferenceErrorUpdater : JSUnresolvedReferenceErrorUpdater {

  override fun updateError(
    info: JSUnresolvedReferenceErrorUpdater.ErrorInfo,
    node: JSReferenceExpression,
    resolveResults: Array<out ResolveResult>,
    isTypeContext: Boolean,
  ) {
    if (node is Angular2PipeReferenceExpression) {
      // TODO add support for create pipe from reference
      (info as ErrorInfoImpl).removeIf { true }
      val myFixes = SmartList<LocalQuickFix>()
      Angular2FixesFactory.addUnresolvedDeclarationFixes(node, myFixes)
      info.addAll(myFixes)
    }
    else if (node.language is Angular2Language) {
      val quickFixes = mutableListOf<LocalQuickFix>()
      if (resolveResults.isEmpty()
          && addCreateFromUsageFixesInAngularExpression(node, quickFixes)
          && !isTypeContext) {
        (info as ErrorInfoImpl).removeIf { it is BaseCreateFix }
      }
      info.addAll(quickFixes)
    }
  }

  private fun addCreateFromUsageFixesInAngularExpression(
    expression: JSReferenceExpression,
    quickFixes: MutableList<in LocalQuickFix>,
  ): Boolean {
    val qualifier = expression.qualifier
    if (qualifier == null || qualifier is JSThisExpression) {
      val componentClass = Angular2SourceUtil.findComponentClass(expression)
      if (componentClass != null && expression.referenceName != null) {
        if (expression.parent is JSCallExpression) {
          quickFixes.add(CreateComponentMethodIntentionAction(expression))
          if (Angular2SignalUtils.supportsSignals(componentClass)) {
            quickFixes.add(CreateComponentSignalIntentionAction(expression))
          }
        }
        else {
          quickFixes.add(CreateComponentFieldIntentionAction(expression))
          if (expression.parentOfType<Angular2EmbeddedExpression>() is Angular2Action) {
            quickFixes.add(CreateDirectiveOutputIntentionAction(expression, expression.referenceName!!))
          }
        }
      }
      return true
    }
    return false
  }
}