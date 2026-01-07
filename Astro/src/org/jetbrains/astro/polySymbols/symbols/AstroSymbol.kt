// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.polySymbols.framework.FrameworkId
import com.intellij.polySymbols.html.HtmlFrameworkSymbol
import org.jetbrains.astro.AstroFramework

interface AstroSymbol : HtmlFrameworkSymbol {

  override val framework: FrameworkId
    get() = AstroFramework.ID

}