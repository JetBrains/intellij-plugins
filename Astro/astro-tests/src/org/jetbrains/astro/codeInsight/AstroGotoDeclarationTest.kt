package org.jetbrains.astro.codeInsight

import com.intellij.testFramework.junit5.TestApplication
import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule
import org.junit.jupiter.api.Test

@TestApplication
class AstroGotoDeclarationTest : AstroCodeInsightTestCase("codeInsight/navigation/declaration", useLsp = true) {
  @Test
  fun testAstroComponentProp() =
    astro.doGotoDeclarationTest(declarationSignature = "<caret>title: string,", expectedFileName = "component.astro",
                          dir = true)

  @Test
  fun testDestructuredParams() =
    astro.doGotoDeclarationTest(fromSignature = "la<caret>ng", declarationSignature = "<caret>lang: 'en'",
                          dir = true, configureFileName = "index.astro")

  @Test
  fun testReactComponentImport() =
    astro.doGotoDeclarationTest(fromSignature = "MyCompo<caret>nent", declarationSignature = "function <caret>MyComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testAstroComponentImport() =
    astro.doGotoDeclarationTest(fromSignature = "MyCompo<caret>nent", declarationSignature = "<caret><div",
                          expectedFileName = "MyComponent.astro", dir = true, configureFileName = "index.astro")

  @Test
  fun testSvelteComponentImport() =
    astro.doGotoDeclarationTest("<caret><script", AstroTestModule.ASTRO_SVELTE_7_2_2,
                          fromSignature = "MyCompo<caret>nent",
                          expectedFileName = "MyComponent.svelte", dir = true, configureFileName = "index.astro")

  @Test
  fun testVueComponentImport() =
    astro.doGotoDeclarationTest("<caret><template", AstroTestModule.ASTRO_VUE_5_1_1, dir = true,
                          fromSignature = "MyCompo<caret>nent",
                          expectedFileName = "MyComponent.vue", configureFileName = "index.astro")

  @Test
  fun testReactNamespacedAsFieldComponent() =
    astro.doGotoDeclarationTest(fromSignature = "Nes<caret>ted", declarationSignature = "function <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testReactNamespacedAsObjectComponent() =
    astro.doGotoDeclarationTest(fromSignature = "Nested<caret>Component", declarationSignature = "mponents = {\n  <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testReactMultipleNamespacedComponent() =
    astro.doGotoDeclarationTest(fromSignature = "NestedSecond<caret>LevelComponent", declarationSignature = "<caret>NestedSecondLevelComponent",
                          expectedFileName = "NestedComponent.tsx", dir = true, configureFileName = "index.astro")

  @Test
  fun testReactMultipleNamespacedMiddleComponent() =
    astro.doGotoDeclarationTest(fromSignature = "Nested<caret>Component", declarationSignature = "mponents = {\n  <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

}