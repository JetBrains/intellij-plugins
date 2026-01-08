// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.polySymbols.PolySymbol.Companion.PROP_DOC_HIDE_ICON
import com.intellij.polySymbols.PolySymbolProperty
import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.AstroIcons
import javax.swing.Icon

interface AstroSymbol : HtmlFrameworkSymbol {

  override val framework: FrameworkId
    get() = AstroFramework.ID

  override val icon: Icon?
    get() = AstroIcons.Astro

  override fun <T : Any> get(property: PolySymbolProperty<T>): T? =
    when (property) {
      PROP_DOC_HIDE_ICON -> property.tryCast(true)
      else -> super.get(property)
    }
}