// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs

import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator

fun List<PolySymbolsTestConfigurator>.withDisabledConfigUsageCheck(): List<PolySymbolsTestConfigurator> =
  map {
    if (it is VueTsConfigFile) {
      it.copy(checkUsage = false)
    }
    else it
  }
