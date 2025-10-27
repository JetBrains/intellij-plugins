package org.jetbrains.astro.codeInsight

import com.intellij.polySymbols.testFramework.checkGotoDeclaration
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

class AstroGotoDeclarationTest : AstroCodeInsightTestCase("codeInsight/navigation/declaration", useLsp = true) {
  fun testAstroComponentProp() = checkGotoDeclaration("<caret>title: string,", expectedFileName = "component.astro", dir = true)

  fun testDestructuredParams() = doConfiguredTest(dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("la<caret>ng", "<caret>lang: 'en'")
  }

  fun testReactComponentImport() = doConfiguredTest(dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "function <caret>MyComponent", "MyComponent.tsx")
  }

  fun testAstroComponentImport() = doConfiguredTest(dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "<caret><div", "MyComponent.astro")
  }

  fun testSvelteComponentImport() = doConfiguredTest(AstroTestModule.ASTRO_SVELTE_7_2_2, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "<caret><script", "MyComponent.svelte")
  }

  fun testVueComponentImport() = doConfiguredTest(AstroTestModule.ASTRO_VUE_5_1_1, dir = true, configureFileName = "index.astro") {
    checkGotoDeclaration("MyCompo<caret>nent", "<caret><template", "MyComponent.vue")
  }
}