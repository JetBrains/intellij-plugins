// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.angular2.inspections

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.lang.javascript.psi.JSReferenceExpression
import com.intellij.lang.javascript.validation.JSUnresolvedReferenceErrorUpdater
import com.intellij.lang.javascript.validation.JSUnresolvedReferenceErrorUpdater.ErrorInfoImpl
import com.intellij.psi.ResolveResult
import com.intellij.util.SmartList
import org.angular2.inspections.quickfixes.Angular2FixesFactory
import org.angular2.lang.expr.psi.Angular2PipeReferenceExpression

class Angular2UnresolvedReferenceErrorUpdater: JSUnresolvedReferenceErrorUpdater {

  override fun updateError(info: JSUnresolvedReferenceErrorUpdater.ErrorInfo,
                           node: JSReferenceExpression,
                           resolveResults: Array<out ResolveResult>,
                           isTypeContext: Boolean) {
    if (node is Angular2PipeReferenceExpression) {
      (info as ErrorInfoImpl).removeIf { true }
      val myFixes = SmartList<LocalQuickFix>()
      Angular2FixesFactory.addUnresolvedDeclarationFixes(node, myFixes)
      info.addAll(myFixes)
    }
  }

}