package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.javascript.web.checkDocumentationAtCaret
import com.intellij.javascript.web.checkNoDocumentationAtCaret
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.lang.javascript.TypeScriptTestUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class VueDocumentationTest : BasePlatformTestCase() {

  override fun getBasePath(): String = "/"

  override fun getTestDataPath(): String = getVueTestDataPath() + "/documentation"

  fun testFromDefinitions() {
    myFixture.configureVueDependencies(VueTestModule.VUE_2_5_3)
    defaultTest()
  }

  fun testTSLibraryElement() {
    createPackageJsonWithVueDependency(myFixture, "")
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

  private fun defaultTest() {
    myFixture.configureByFile("${getTestName(false)}.vue")
    myFixture.checkDocumentationAtCaret()
  }

}
