// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.polySymbols.PolySymbol.DocHideIconProperty
import com.intellij.polySymbols.PolySymbol
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

  @PolySymbol.Property(DocHideIconProperty::class)
  val docHideIcon: Boolean
    get() = true
}