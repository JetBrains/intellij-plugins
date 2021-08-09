package org.jetbrains.vuejs.lang

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.lang.documentation.ExternalDocumentationProvider
import com.intellij.lang.javascript.JSAbstractDocumentationTest
import junit.framework.TestCase

class VueDocumentationTest : JSAbstractDocumentationTest() {

  override fun getBasePath(): String = "/"
  override fun getExtension(): String = "vue"

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
    val testName = getTestName(false)
    doTest(testName, extension, testName, false, Check.Null)
  }

  fun testInnerLevelTemplateCustomAttr() {
    defaultTest()
  }

  fun testCustomComponentProperty() {
    defaultTest()
  }

  fun testUnknownParentTag() {
    defaultTest()
  }

  fun testRequiredPropertyTS() {
    defaultTest()
  }

  fun testNotRequiredPropertyTS() {
    defaultTest()
  }

  fun testNotRequiredPropertyJS() {
    defaultTest()
  }

}
