// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.astro.service

import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import org.jetbrains.astro.AstroLspTestCase
import org.jetbrains.astro.AstroTestModule

class AstroTypeScriptServiceGotoDeclarationTest : AstroLspTestCase("codeInsight/navigation/gotoDeclaration") {
  override val defaultDependencies: Map<String, String> =
    mapOf(
      "astro" to "5.14.4"
    )

  fun testDestructuredParams() = doConfiguredTest(AstroTestModule.ASTRO_5_14_4, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("la<caret>ng", "<caret>lang: 'en'")
  }

  fun testReactComponentImport() = doConfiguredTest(AstroTestModule.ASTRO_5_14_4, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "function <caret>MyComponent", "MyComponent.tsx")
  }

  fun testAstroComponentImport() = doConfiguredTest(AstroTestModule.ASTRO_5_14_4, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "<caret><div", "MyComponent.astro")
  }

  fun testSvelteComponentImport() = doConfiguredTest(AstroTestModule.ASTRO_5_14_4, AstroTestModule.ASTRO_SVELTE_7_2_2, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "<caret><script", "MyComponent.svelte")
  }

  fun testVueComponentImport() = doConfiguredTest(AstroTestModule.ASTRO_5_14_4, AstroTestModule.ASTRO_VUE_5_1_1, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "<caret><template", "MyComponent.vue")
  }
}