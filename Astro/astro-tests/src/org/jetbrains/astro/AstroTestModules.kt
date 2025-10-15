// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro

import com.intellij.javascript.testFramework.web.WebFrameworkTestModule
import org.jetbrains.astro.codeInsight.ASTRO_PKG

enum class AstroTestModule(myPackageName: String, myVersion: String) : WebFrameworkTestModule {
  ASTRO_1_9_0(ASTRO_PKG, "1.9.0"),
  ASTRO_5_14_4(ASTRO_PKG, "5.14.4"),
  ASTRO_SVELTE_7_2_2("@astrojs/svelte", "7.2.2"),
  ASTRO_VUE_5_1_1("@astrojs/vue", "5.1.1"),
  ;

  override val packageNames: List<String> = listOf(myPackageName)
  override val folder: String = myPackageName.replace('/', '#') + "/" + myVersion + "/node_modules"
}
