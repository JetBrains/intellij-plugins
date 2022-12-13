package org.intellij.prisma

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.Disposer

class PrismaDocumentationTest : PrismaTestCase() {
  override fun getBasePath(): String = "/documentation"

  override fun setUp() {
    super.setUp()

    val colorsManager = EditorColorsManager.getInstance()
    val schemeBefore = colorsManager.globalScheme
    colorsManager.globalScheme = colorsManager.getScheme("Darcula")
    Disposer.register(testRootDisposable) {
      colorsManager.globalScheme = schemeBefore
    }
  }

  fun testModel() {
    doTest()
  }

  fun testKeyword() {
    doTest()
  }

  fun testKeywordType() {
    doTest()
  }

  fun testKeywordTypeInAlias() {
    doTest()
  }

  fun testType() {
    doTest()
  }

  fun testEnum() {
    doTest()
  }

  fun testEnumValue() {
    doTest()
  }

  fun testGenerator() {
    doTest()
  }

  fun testDatasource() {
    doTest()
  }

  fun testField() {
    doTest()
  }

  fun testFieldTrailingDoc() {
    doTest()
  }

  fun testKeyValue() {
    doTest()
  }

  fun testPrimitiveType() {
    doTest()
  }

  fun testUnsupportedType() {
    doTest()
  }

  fun testDatasourceField() {
    doTest()
  }

  fun testGeneratorField() {
    doTest()
  }

  fun testBlockAttribute() {
    doTest()
  }

  fun testBlockAttributeParam() {
    doTest()
  }

  fun testBlockAttributeFieldArgument() {
    doTest()
  }

  fun testFieldAttribute() {
    doTest()
  }

  fun testFieldAttributeParam() {
    doTest()
  }

  fun testDatasourceProviderValue() {
    doTest()
  }

  fun testDatasourceUrlFunction() {
    doTest()
  }

  fun testStringLiteralVariant() {
    doTest()
  }

  fun testRelationField() {
    doTest()
  }

  fun testDefaultValue() {
    doTest()
  }

  private fun doTest() {
    val file = myFixture.configureByFile(getTestName())
    val originalElement = file.findElementAt(myFixture.caretOffset)
    val element = DocumentationManager.getInstance(myFixture.project)
      .findTargetElement(myFixture.editor, file, originalElement)
    val provider = DocumentationManager.getProviderFromElement(element, originalElement)
    val doc = provider.generateDoc(element, originalElement) ?: "<empty>"
    assertSameLinesWithFile("${testDataPath}/${getTestName("html")}", doc)
  }
}