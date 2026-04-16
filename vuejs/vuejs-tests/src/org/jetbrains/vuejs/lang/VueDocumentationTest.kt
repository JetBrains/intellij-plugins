package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.polySymbols.testFramework.PolySymbolsTestConfigurator
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.polySymbols.testFramework.checkLookupItems
import com.intellij.polySymbols.testFramework.checkNoDocumentationAtCaret
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.jetbrains.vuejs.VueTsConfigFile
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class VueDocumentationTest :
  VueTestCase("documentation", testMode = VueTestMode.NO_PLUGIN) {

  // for migration
  override fun adjustConfigurators(
    configurators: List<PolySymbolsTestConfigurator>,
  ): List<PolySymbolsTestConfigurator> =
    super.adjustConfigurators(configurators)
      .filter { it !is VueTsConfigFile }

  private val defaultTestFileName: String
    get() = "${getTestName(false)}.vue"

  @Test
  fun testFromDefinitions() {
    doTest(VueTestModule.VUE_2_5_3)
  }

  @Test
  fun testTSLibraryElement() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("TSLibraryElement.vue")
    val element = myFixture.file.findElementAt(myFixture.caretOffset)
    val elementAtCaret = myFixture.elementAtCaret
    val documentationProvider = DocumentationManager.getProviderFromElement(elementAtCaret, element)
    documentationProvider as ExternalDocumentationProvider
    val urls = documentationProvider.getUrlFor(elementAtCaret, element)
    assertNotNull(urls)
    assertNull("$urls", documentationProvider.fetchExternalDocumentation(project, elementAtCaret, urls, false))
  }

  @Test
  fun testTopLevelTemplate() {
    doTest()
  }

  @Test
  fun testInnerLevelTemplate() {
    doTest()
  }

  @Test
  fun testInnerLevelTemplateStdAttr() {
    doTest()
  }

  @Test
  fun testInnerLevelTemplateStdAttrNoDoc() {
    doConfiguredTest(
      configureFileName = defaultTestFileName,
    ) {
      checkNoDocumentationAtCaret()
    }
  }

  @Test
  fun testInnerLevelTemplateCustomAttr() {
    doTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testDynamicAttributes() {
    doTest(VueTestModule.VUE_2_6_10)
  }

  @Test
  fun testScriptSetupDestructing() {
    doTest()
  }

  @Test
  fun testCustomComponentProperty() {
    doTest()
  }

  @Test
  fun testUnknownParentTag() {
    doTest()
  }

  @Test
  fun testRequiredPropertyTS() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doTest()
  }

  @Test
  fun testNotRequiredPropertyTS() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    doTest()
  }

  @Test
  fun testNotRequiredPropertyJS() {
    doTest()
  }

  @Test
  fun testMergedWebTypesComponents() {
    doLookupTest(
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
      configureFileName = defaultTestFileName,
      renderTypeText = false,
      checkDocumentation = true,
    ) {
      it.lookupString in setOf("n-affix", "n-bar", "n-a", "n-button", "n-alert")
    }
  }

  @Test
  fun testMergedWebTypesPropsGlobal() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)

    doLookupTest(
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
      configureFileName = defaultTestFileName,
      checkDocumentation = true,
    ) {
      it.lookupString in setOf("bottom", "offset-top", "position", "trigger-bottom")
    }
  }

  @Test
  fun testMergedWebTypesPropsLocal() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)

    doLookupTest(
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
      configureFileName = defaultTestFileName,
      checkDocumentation = true,
    ) {
      it.lookupString in setOf("bottom", "offset-top", "position", "trigger-bottom")
    }
  }

  @Test
  fun testMergedWebTypesSlots() {
    doLookupTest(
      VueTestModule.NAIVE_UI_2_33_2_PATCHED,
      configureFileName = defaultTestFileName,
      renderTypeText = false,
      checkDocumentation = true,
    )
  }

  @Test
  fun testMergedWebTypesPropsSource() {
    myFixture.copyDirectoryToProject(getTestName(true), ".")
    myFixture.configureFromTempProjectFile("src/MergedWebTypesPropsSource.vue")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true, expectedDataLocation = "") {
      it.lookupString in setOf("test-prop-two", "test-prop")
    }
    myFixture.configureFromTempProjectFile("src/MergedWebTypesPropsScriptSource.vue")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true, expectedDataLocation = "") {
      it.lookupString in setOf("test-prop-two", "test-prop")
    }
  }

  @Test
  fun testPrimeVueMergedProps() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_5_0, VueTestModule.PRIMEVUE_3_8_2)
    myFixture.configureByFile(defaultTestFileName)
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true,
                               fileName = "PrimeVueMergedPropsElement") {
      it.lookupString in setOf("Avatar", "BlockUI")
    }
    myFixture.moveToOffsetBySignature("Avatar <caret>>")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true) {
      it.lookupString in setOf("icon", "size")
    }
  }

  @Test
  fun testPropertyTypeDoc() {
    doTest()
  }

  @Test
  fun testEmitEvents() {
    doTest()
  }

  @Test
  fun testGenericComponentProp() {
    doTest()
  }

  @Test
  fun testPropJsDoc() {
    doTest()
  }

  @Test
  fun testPropRefJsDoc() {
    doTest()
  }

  @Test
  fun testDataPropJsDoc() {
    doTest()
  }

  @Test
  fun testDataPropRefJsDoc() {
    doTest()
  }

  @Test
  fun testPropWithDefaults() {
    doTest()
  }

  @Test
  fun testNoComponentDocInCodeCompletion() {
    doLookupTest(
      configureFileName = defaultTestFileName,
      renderPriority = false,
      renderTypeText = false,
      checkDocumentation = true,
    ) {
      it.lookupString == "NoComponentDocInCodeCompletion"
      || it.lookupString == "Component"
    }
  }

  private fun doTest(
    vararg modules: VueTestModule,
  ) {
    doConfiguredTest(
      modules = modules,
      configureFileName = defaultTestFileName,
    ) {
      checkDocumentationAtCaret()
    }
  }

}
