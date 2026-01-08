// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PolySymbolSearchTarget
import org.jetbrains.vuejs.model.VueScopeElement

abstract class VueScopeElementSymbol<T : VueScopeElement>(name: String, item: T) :
  VueDocumentedItemSymbol<T>(name, item) {

  abstract override fun createPointer(): Pointer<out VueScopeElementSymbol<T>>

  override val searchTarget: PolySymbolSearchTarget?
    get() = PolySymbolSearchTarget.create(this)

  override val renameTarget: PolySymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      PolySymbolRenameTarget.create(this)
    else null
}