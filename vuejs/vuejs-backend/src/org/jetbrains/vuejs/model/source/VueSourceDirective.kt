// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.model.source

import com.intellij.lang.javascript.psi.JSLiteralExpression
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.openapi.project.DumbService
import com.intellij.polySymbols.PolySymbolKind
import com.intellij.polySymbols.refactoring.PolySymbolRenameTarget
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.createSmartPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.codeInsight.fromAsset
import org.jetbrains.vuejs.model.*
import org.jetbrains.vuejs.model.typed.VueTypedDirectives.getDirectiveModifiers
import org.jetbrains.vuejs.web.VUE_DIRECTIVES

class VueSourceDirective(
  name: String,
  override val source: PsiElement,
  private val typeSource: PsiElement? = null,
  private val mode: VueMode = VueMode.CLASSIC,
  override val vueProximity: VueModelVisitor.Proximity? = null,
) : VueDirective, PsiSourcedPolySymbol {

  override val kind: PolySymbolKind
    get() = VUE_DIRECTIVES

  override val name: String = fromAsset(name)

  override val parents: List<VueEntitiesContainer> = emptyList()

  override val directiveModifiers: List<VueDirectiveModifier>
    get() {
      typeSource ?: return emptyList()

      return CachedValuesManager.getCachedValue(typeSource) {
        CachedValueProvider.Result.create(
          getDirectiveModifiers(typeSource, mode),
          DumbService.getInstance(typeSource.project).modificationTracker,
          PsiModificationTracker.MODIFICATION_COUNT,
        )
      }
    }

  override val renameTarget: PolySymbolRenameTarget?
    get() = if (source is JSLiteralExpression)
      PolySymbolRenameTarget.create(this)
    else null

  override fun withVueProximity(proximity: VueModelVisitor.Proximity): VueDirective =
    VueSourceDirective(name, source, typeSource, mode, proximity)

  override fun createPointer(): Pointer<out VueSourceDirective> {
    val name = name
    val source = this.source.createSmartPointer()
    val typeSource = this.typeSource?.createSmartPointer()
    val mode = this.mode
    val vueProximity = this.vueProximity
    return Pointer {
      val newSource = source.dereference() ?: return@Pointer null
      val newTypeSource = if (typeSource != null) {
        typeSource.dereference() ?: return@Pointer null
      }
      else null

      VueSourceDirective(
        name = name,
        source = newSource,
        typeSource = newTypeSource,
        mode = mode,
        vueProximity = vueProximity,
      )
    }
  }

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
    || symbol is VueSourceDirective
    && symbol.source == source
    && symbol.name == name

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueSourceDirective
     && other.name == name
     && other.source == source
     && other.vueProximity == vueProximity
    )

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + source.hashCode()
    result = 31 * result + (vueProximity?.hashCode() ?: 0)
    return result
  }

  override fun toString(): String {
    return "VueSourceDirective($name)"
  }
}
