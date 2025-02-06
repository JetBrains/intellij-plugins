package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.webSymbols.testFramework.checkDocumentationAtCaret
import com.intellij.webSymbols.testFramework.checkLookupItems
import com.intellij.webSymbols.testFramework.checkNoDocumentationAtCaret
import com.intellij.webSymbols.testFramework.moveToOffsetBySignature
import junit.framework.TestCase

class VueDocumentationTest : BasePlatformTestCase() {

  override fun getBasePath(): String = "/"

  override fun getTestDataPath(): String = getVueTestDataPath() + "/documentation"

  fun testFromDefinitions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    defaultTest()
  }

  fun testTSLibraryElement() {
    myFixture.configureVueDependencies()
    myFixture.configureByFile("TSLibraryElement.vue")
    val element = myFixture.file.findElementAt(myFixture.caretOffset)
    val elementAtCaret = myFixture.elementAtCaret
    val documentationProvider = DocumentationManager.getProviderFromElement(elementAtCaret, element)
    documentationProvider as ExternalDocumentationProvider
    val urls = documentationProvider.getUrlFor(elementAtCaret, element)
    TestCase.assertNotNull(urls)
    TestCase.assertNull("$urls", documentationProvider.fetchExternalDocumentation(project, elementAtCaret, urls, false))
  }

  fun testTopLevelTemplate() {
    defaultTest()
  }

  fun testInnerLevelTemplate() {
    defaultTest()
  }

  fun testInnerLevelTemplateStdAttr() {
    defaultTest()
  }

  fun testInnerLevelTemplateStdAttrNoDoc() {
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkNoDocumentationAtCaret()
  }

  fun testInnerLevelTemplateCustomAttr() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    defaultTest()
  }

  fun testDynamicAttributes() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_6_10)
    defaultTest()
  }

  fun testScriptSetupDestructing() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    defaultTest()
  }

  fun testCustomComponentProperty() {
    defaultTest()
  }

  fun testUnknownParentTag() {
    defaultTest()
  }

  fun testRequiredPropertyTS() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    defaultTest()
  }

  fun testNotRequiredPropertyTS() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    defaultTest()
  }

  fun testNotRequiredPropertyJS() {
    defaultTest()
  }

  fun testMergedWebTypesComponents() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NAIVE_UI_2_33_2_PATCHED)
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkLookupItems(renderPriority = true, checkDocumentation = true) {
      it.lookupString in setOf("n-affix", "n-bar", "n-a", "n-button", "n-alert")
    }
  }

  fun testMergedWebTypesPropsGlobal() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NAIVE_UI_2_33_2_PATCHED)
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true) {
      it.lookupString in setOf("bottom", "offset-top", "position", "trigger-bottom")
    }
  }

  fun testMergedWebTypesPropsLocal() {
    TypeScriptTestUtil.setStrictNullChecks(project, testRootDisposable)
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NAIVE_UI_2_33_2_PATCHED)
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true) {
      it.lookupString in setOf("bottom", "offset-top", "position", "trigger-bottom")
    }
  }

  fun testMergedWebTypesSlots() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.NAIVE_UI_2_33_2_PATCHED)
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkLookupItems(renderPriority = true, checkDocumentation = true)
  }

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

  fun testPrimeVueMergedProps() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2, VueTestModule.PRIMEVUE_3_8_2)
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true,
                               fileName = "PrimeVueMergedPropsElement") {
      it.lookupString in setOf("Avatar", "BlockUI")
    }
    myFixture.moveToOffsetBySignature("Avatar <caret>>")
    myFixture.checkLookupItems(renderPriority = true, renderTypeText = true, checkDocumentation = true) {
      it.lookupString in setOf("icon", "size")
    }
  }

  fun testPropertyTypeDoc() {
    defaultTest()
  }

  fun testEmitEvents() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    defaultTest()
  }

  fun testGenericComponentProp() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    defaultTest()
  }

  fun testPropJsDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    defaultTest()
  }

  fun testPropRefJsDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    defaultTest()
  }

  fun testDataPropJsDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    defaultTest()
  }

  fun testDataPropRefJsDoc() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_2_2)
    defaultTest()
  }

  fun testPropWithDefaults() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    defaultTest()
  }

  fun testNoComponentDocInCodeCompletion() {
    myFixture.configureVueDependencies(VueTestModule.VUE_3_3_4)
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkLookupItems(checkDocumentation = true) {
      it.lookupString == "NoComponentDocInCodeCompletion" || it.lookupString == "Component"
    }
  }

  private fun defaultTest() {
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkDocumentationAtCaret()
  }

}
