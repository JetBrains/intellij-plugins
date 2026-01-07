// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.polySymbols.symbols

import com.intellij.polySymbols.PolySymbolOrigin
import com.intellij.polySymbols.framework.FrameworkId
import org.jetbrains.astro.AstroFramework
import org.jetbrains.astro.AstroIcons
import javax.swing.Icon

object AstroProjectSymbolOrigin : PolySymbolOrigin {

  override val defaultIcon: Icon
    get() = AstroIcons.Astro

  override val framework: FrameworkId
    get() = AstroFramework.ID
}