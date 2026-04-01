package org.jetbrains.astro.codeInsight

import org.jetbrains.astro.AstroCodeInsightTestCase
import org.jetbrains.astro.AstroTestModule

class AstroGotoDeclarationTest : AstroCodeInsightTestCase("codeInsight/navigation/declaration", useLsp = true) {
  fun testAstroComponentProp() =
    doGotoDeclarationTest(declarationSignature = "<caret>title: string,", expectedFileName = "component.astro",
                          dir = true)

  fun testDestructuredParams() =
    doGotoDeclarationTest(fromSignature = "la<caret>ng", declarationSignature = "<caret>lang: 'en'",
                          dir = true, configureFileName = "index.astro")

  fun testReactComponentImport() =
    doGotoDeclarationTest(fromSignature = "MyCompo<caret>nent", declarationSignature = "function <caret>MyComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  fun testAstroComponentImport() =
    doGotoDeclarationTest(fromSignature = "MyCompo<caret>nent", declarationSignature = "<caret><div",
                          expectedFileName = "MyComponent.astro", dir = true, configureFileName = "index.astro")

  fun testSvelteComponentImport() =
    doGotoDeclarationTest("<caret><script", AstroTestModule.ASTRO_SVELTE_7_2_2,
                          fromSignature = "MyCompo<caret>nent",
                          expectedFileName = "MyComponent.svelte", dir = true, configureFileName = "index.astro")

  fun testVueComponentImport() =
    doGotoDeclarationTest("<caret><template", AstroTestModule.ASTRO_VUE_5_1_1, dir = true,
                          fromSignature = "MyCompo<caret>nent",
                          expectedFileName = "MyComponent.vue", configureFileName = "index.astro")

  fun testReactNamespacedAsFieldComponent() =
    doGotoDeclarationTest(fromSignature = "Nes<caret>ted", declarationSignature = "function <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  fun testReactNamespacedAsObjectComponent() =
    doGotoDeclarationTest(fromSignature = "Nested<caret>Component", declarationSignature = "mponents = {\n  <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

  fun testReactMultipleNamespacedComponent() =
    doGotoDeclarationTest(fromSignature = "NestedSecond<caret>LevelComponent", declarationSignature = "<caret>NestedSecondLevelComponent",
                          expectedFileName = "NestedComponent.tsx", dir = true, configureFileName = "index.astro")

  fun testReactMultipleNamespacedMiddleComponent() =
    doGotoDeclarationTest(fromSignature = "Nested<caret>Component", declarationSignature = "mponents = {\n  <caret>NestedComponent",
                          expectedFileName = "MyComponent.tsx", dir = true, configureFileName = "index.astro")

}