// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.model.Pointer
import com.intellij.model.Symbol
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.presentation.TargetPresentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationTarget
import com.intellij.polySymbols.query.PolySymbolScope
import com.intellij.polySymbols.search.PsiSourcedPolySymbol
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiModificationTracker
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.codeInsight.documentation.VueDocumentedItem
import org.jetbrains.vuejs.codeInsight.documentation.VueItemDocumentation

abstract class VueDocumentedItemSymbol<T : VueDocumentedItem>(
  override val name: String,
  protected val item: T,
) : PolySymbolScope, PsiSourcedPolySymbol, VueSymbol {

  override fun getModificationCount(): Long =
    source?.project?.let { PsiModificationTracker.getInstance(it).modificationCount } ?: 0

  override val source: PsiElement?
    get() = item.source

  val rawSource: PsiElement?
    get() = item.rawSource

  override fun getDocumentationTarget(location: PsiElement?): DocumentationTarget? =
    PolySymbolDocumentationTarget.create(this, location) { symbol, _ ->
      description = symbol.item.description

      symbol.item.source
        ?.containingFile
        ?.virtualFile
        ?.let { PackageJsonUtil.findUpPackageJson(it) }
        ?.takeIf { NodeModuleUtil.hasNodeModulesDirInPath(it, null) }
        ?.let { PackageJsonData.getOrCreate(it) }
        ?.takeIf { it.name != null }
        ?.let { data ->
          library = data.name + (data.version?.toString()?.let { "@$it" } ?: "")
        }
    }

  override val presentation: TargetPresentation
    get() = TargetPresentation.builder(VueBundle.message("vue.symbol.presentation", VueItemDocumentation.typeOf(item), name))
      .icon(icon)
      .presentation()

  abstract override fun createPointer(): Pointer<out VueDocumentedItemSymbol<T>>

  override fun equals(other: Any?): Boolean =
    other === this ||
    (other is VueDocumentedItemSymbol<*>
     && other.javaClass == this.javaClass
     && name == other.name
     && item == other.item)

  override fun hashCode(): Int =
    31 * name.hashCode() + item.hashCode()

  override fun isEquivalentTo(symbol: Symbol): Boolean =
    if (symbol is VueDocumentedItemSymbol<*>)
      symbol === this || (symbol.javaClass == this.javaClass
                          && symbol.name == name)
    //&& VueDelegatedContainer.unwrap(item) == VueDelegatedContainer.unwrap(symbol.item))
    else
      super<PsiSourcedPolySymbol>.isEquivalentTo(symbol)
}