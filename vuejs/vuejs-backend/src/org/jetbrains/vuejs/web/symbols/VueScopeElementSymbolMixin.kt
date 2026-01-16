// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import org.jetbrains.vuejs.model.VueScopeElement

interface VueScopeElementSymbolMixin : VueDocumentedItemSymbolMixin, VueScopeElement, PolySymbolScope {

  override fun getModificationCount(): Long = -1

  abstract override fun createPointer(): Pointer<out VueScopeElementSymbolMixin>

  override val renameTarget: PolySymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      PolySymbolRenameTarget.create(this)
    else null

}