// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.web.symbols

import com.intellij.javascript.nodejs.PackageJsonData
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil
import com.intellij.lang.javascript.modules.NodeModuleUtil
import com.intellij.polySymbols.documentation.PolySymbolDocumentation
import com.intellij.polySymbols.documentation.PolySymbolDocumentationProvider
import com.intellij.psi.PsiElement
import org.jetbrains.vuejs.model.VueSourceElement

internal object VueSymbolDocumentationProvider : PolySymbolDocumentationProvider<VueSymbol> {
  override fun createDocumentation(
    symbol: VueSymbol,
    location: PsiElement?,
  ): PolySymbolDocumentation = PolySymbolDocumentation.builder(symbol, location).apply {
    val item = (symbol as? VueSourceElement)
               ?: throw IllegalArgumentException("Can't create documentation for $symbol")
    item.source
      ?.containingFile
      ?.virtualFile
      ?.let { PackageJsonUtil.findUpPackageJson(it) }
      ?.takeIf { NodeModuleUtil.hasNodeModulesDirInPath(it, null) }
      ?.let { PackageJsonData.getOrCreate(it) }
      ?.takeIf { it.name != null }
      ?.let { data ->
        library = data.name + (data.version?.toString()?.let { "@$it" } ?: "")
      }
  }.build()
}
