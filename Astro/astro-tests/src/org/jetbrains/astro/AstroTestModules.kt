// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.javascript.web.WebFrameworkTestModule
import org.jetbrains.astro.codeInsight.ASTRO_PKG

enum class AstroTestModule(override val folder: String, vararg packageNames: String) : WebFrameworkTestModule {
  ASTRO_1_9_0("astro/1.9.0", ASTRO_PKG),
  ;

  override val packageNames: List<String> = if (packageNames.isEmpty()) listOf(folder) else packageNames.toList()
}
