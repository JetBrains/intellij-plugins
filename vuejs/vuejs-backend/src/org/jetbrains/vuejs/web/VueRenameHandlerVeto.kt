// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement
import com.intellij.openapi.util.Condition
import com.intellij.psi.PsiElement
import com.intellij.polySymbols.declarations.PolySymbolDeclarationProvider
import org.jetbrains.vuejs.context.isVueContext

class VueRenameHandlerVeto : Condition<PsiElement> {

  override fun value(t: PsiElement): Boolean {
    // AngularJS can break Vue references
    return t is JSImplicitElement &&
           t.parent is JSLiteralExpression &&
           isVueContext(t) &&
           PolySymbolDeclarationProvider.getAllDeclarations(t.parent, -1).isNotEmpty()
  }
}