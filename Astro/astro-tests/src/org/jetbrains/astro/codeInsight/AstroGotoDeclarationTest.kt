package org.jetbrains.astro.codeInsight

import com.intellij.lang.javascript.TrackFailedTestRule
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestMode
import org.jetbrains.astro.AstroTestModule
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class AstroGotoDeclarationTest : AstroGotoDeclarationTestBase() {

  @RunWith(JUnit4::class)
  class WithTsGoProxyTest : AstroGotoDeclarationTestBase(testMode = AstroTestMode.TS_GO_PROXY) {

    @Rule
    @JvmField
    val rule: TestRule = TrackFailedTestRule(
      // With TypeScript 7, the navigation from a namespaced React component shows the usages, not the declaration
      "testReactNamespacedAsFieldComponent",
      "testReactMultipleNamespacedComponent",
    )
  }
}

abstract class AstroGotoDeclarationTestBase(
  testMode: AstroTestMode = AstroTestMode.ASTRO_LSP,
) : AstroCodeInsightTestCase("codeInsight/navigation/declaration", testMode) {

  @Test
  fun testAstroComponentProp() =
    doGotoDeclarationTest(declarationSignature = "<caret>title: string,", expectedFileName = "component.astro",
                          dir = true)

  @Test
  fun testDestructuredParams() =
    doGotoDeclarationTest(fromSignature = "la<caret>ng", declarationSignature = "<caret>lang: 'en'",
                          dir = true, configureFileName = "index.astro")

  @Test
  fun testReactComponentImport() =
    doGotoDeclarationTest(fromSignature = "MyCompo<caret>nent", declarationSignature = "function <caret>MyComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testAstroComponentImport() =
    doGotoDeclarationTest(fromSignature = "MyCompo<caret>nent", declarationSignature = "<caret><div",
                          expectedFileName = "MyComponent.astro", dir = true, configureFileName = "index.astro")

  @Test
  fun testSvelteComponentImport() =
    doGotoDeclarationTest("<caret><script", AstroTestModule.ASTRO_SVELTE_7_2_2,
                          fromSignature = "MyCompo<caret>nent",
                          expectedFileName = "MyComponent.svelte", dir = true, configureFileName = "index.astro")

  @Test
  fun testVueComponentImport() =
    doGotoDeclarationTest("<caret><template", AstroTestModule.ASTRO_VUE_5_1_1, dir = true,
                          fromSignature = "MyCompo<caret>nent",
                          expectedFileName = "MyComponent.vue", configureFileName = "index.astro")

  @Test
  fun testReactNamespacedAsFieldComponent() =
    doGotoDeclarationTest(fromSignature = "Nes<caret>ted", declarationSignature = "function <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testReactNamespacedAsObjectComponent() =
    doGotoDeclarationTest(fromSignature = "Nested<caret>Component", declarationSignature = "mponents = {\n  <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testReactMultipleNamespacedComponent() =
    doGotoDeclarationTest(fromSignature = "NestedSecond<caret>LevelComponent", declarationSignature = "<caret>NestedSecondLevelComponent",
                          expectedFileName = "NestedComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testReactMultipleNamespacedMiddleComponent() =
    doGotoDeclarationTest(fromSignature = "Nested<caret>Component", declarationSignature = "mponents = {\n  <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")
}