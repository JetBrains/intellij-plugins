// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.model.typed

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.lang.javascript.psi.JSType
import com.intellij.lang.javascript.psi.ecma6.TypeScriptPropertySignature
import com.intellij.lang.javascript.psi.types.JSAnyType
import com.intellij.lang.javascript.psi.types.JSTypeSource
import com.intellij.model.Pointer
import com.intellij.polySymbols.PolySymbol
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.query.PolySymbolListSymbolsQueryParams
import com.intellij.polySymbols.query.PolySymbolQueryStack
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.model.VueDirective
import org.jetbrains.vuejs.model.VueDirectiveModifier
import org.jetbrains.vuejs.model.VueMode
import org.jetbrains.vuejs.model.typed.VueTypedDirectives.getDirectiveModifiers
import org.jetbrains.vuejs.web.VUE_GLOBAL_DIRECTIVES

data class VueTypedDirective(
  override val source: TypeScriptPropertySignature,
  override val name: String,
) : VueTypedContainer(source), VueDirective, PsiSourcedPolySymbol {

  override val kind: PolySymbolKind
    get() = VUE_GLOBAL_DIRECTIVES

  override val type: JSType?
    get() = source.jsType

  override val thisType: JSType
    get() = JSAnyType.getWithLanguage(JSTypeSource.SourceLanguage.TS)

  override val directiveModifiers: List<VueDirectiveModifier>
    get() = CachedValuesManager.getCachedValue(source) {
      CachedValueProvider.Result.create(
        getDirectiveModifiers(source, VueMode.CLASSIC),
        PsiModificationTracker.MODIFICATION_COUNT,
      )
    }

  override val renameTarget: PolySymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      PolySymbolRenameTarget.create(this)
    else null

  override fun getSymbols(kind: PolySymbolKind, params: PolySymbolListSymbolsQueryParams, stack: PolySymbolQueryStack): List<PolySymbol> =
    super<VueDirective>.getSymbols(kind, params, stack)

  override fun getModificationCount(): Long = -1

  override fun createPointer(): Pointer<VueTypedDirective> {
    val sourcePtr = source.createSmartPointer()
    val defaultName = this.name
    return Pointer {
      val source = sourcePtr.dereference() ?: return@Pointer null
      VueTypedDirective(source, defaultName)
    }
  }

}
