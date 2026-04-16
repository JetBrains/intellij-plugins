package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.lang.javascript.TrackFailedTestRule
import com.intellij.polySymbols.testFramework.checkDocumentationAtCaret
import com.intellij.polySymbols.testFramework.checkLookupItems
import com.intellij.polySymbols.testFramework.checkNoDocumentationAtCaret
import com.intellij.polySymbols.testFramework.moveToOffsetBySignature
import org.jetbrains.vuejs.VueTestCase
import org.jetbrains.vuejs.VueTestMode
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

class VueDocumentationTest :
  VueDocumentationWithPluginTestBase() {

  class WithLegacyPluginTest :
    VueDocumentationWithPluginTestBase(testMode = VueTestMode.LEGACY_PLUGIN)

  class WithoutServiceTest :
    VueDocumentationTestBase(testMode = VueTestMode.NO_PLUGIN)
}

abstract class VueDocumentationWithPluginTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueDocumentationTestBase(testMode = testMode) {

  // TODO: use separate expected data
  @Rule
  @JvmField
  val rule: TestRule = TrackFailedTestRule(
    "testPropWithDefaults",
    "testFromDefinitions",
    "testPrimeVueMergedProps",
  )
}

@RunWith(JUnit4::class)
abstract class VueDocumentationTestBase(
  testMode: VueTestMode = VueTestMode.DEFAULT,
) : VueTestCase("documentation", testMode = testMode) {

  private val defaultTestFileName: String
    get() = "${getTestName(false)}.vue"

  @Test
  fun testFromDefinitions() {
    doTest(VueTestModule.VUE_2_5_3)
  }

  @Test
  fun testTSLibraryElement() {
    doConfiguredTest(
      configureFileName = defaultTestFileName,
    ) {
      val element = file.findElementAt(caretOffset)
      val elementAtCaret = elementAtCaret
      val documentationProvider = DocumentationManager.getProviderFromElement(elementAtCaret, element)
      documentationProvider as ExternalDocumentationProvider
      val urls = documentationProvider.getUrlFor(elementAtCaret, element)
      assertNotNull(urls)
      assertNull("$urls", documentationProvider.fetchExternalDocumentation(project, elementAtCaret, urls, false))
    }
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
    doTest()
  }

  @Test
  fun testNotRequiredPropertyTS() {
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
    doConfiguredTest(
      dir = true,
      configurators = listOf(
        UseLocalPackageJsonConfigurator(),
      ),
      configureFile = false,
    ) {
      configureFromTempProjectFile("src/MergedWebTypesPropsSource.vue")
      checkLookupItems(
        renderPriority = true,
        renderTypeText = true,
        checkDocumentation = true,
        expectedDataLocation = "",
      ) {
        it.lookupString in setOf("test-prop-two", "test-prop")
      }

      configureFromTempProjectFile("src/MergedWebTypesPropsScriptSource.vue")
      checkLookupItems(
        renderPriority = true,
        renderTypeText = true,
        checkDocumentation = true,
        expectedDataLocation = "",
      ) {
        it.lookupString in setOf("test-prop-two", "test-prop")
      }
    }
  }

  @Test
  fun testPrimeVueMergedProps() {
    doConfiguredTest(
      VueTestModule.PRIMEVUE_3_8_2,
      configureFileName = defaultTestFileName,
    ) {
      checkLookupItems(
        renderPriority = true,
        renderTypeText = true,
        checkDocumentation = true,
        fileName = "PrimeVueMergedPropsElement",
      ) {
        it.lookupString in setOf("Avatar", "BlockUI")
      }

      moveToOffsetBySignature("Avatar <caret>>")
      checkLookupItems(
        renderPriority = true,
        renderTypeText = true,
        checkDocumentation = true,
      ) {
        it.lookupString in setOf("icon", "size")
      }
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
