// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.Disposer

class PrismaDocumentationTest : PrismaTestCase("documentation") {
  override fun setUp() {
    super.setUp()

    val colorsManager = EditorColorsManager.getInstance()
    val schemeBefore = colorsManager.globalScheme
    colorsManager.setGlobalScheme(colorsManager.getScheme("Darcula"))
    Disposer.register(testRootDisposable) {
      colorsManager.setGlobalScheme(schemeBefore)
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

  fun testNativeTypeNamespace() {
    doTest()
  }

  fun testDocComment() {
    doTest()
  }

  private fun doTest() {
    val file = myFixture.configureByFile(getTestFileName())
    val originalElement = file.findElementAt(myFixture.caretOffset)
    val element = DocumentationManager.getInstance(myFixture.project)
      .findTargetElement(myFixture.editor, file, originalElement)
    val provider = DocumentationManager.getProviderFromElement(element, originalElement)
    val doc = provider.generateDoc(element, originalElement)?.let { reformatDocumentation(project, it) } ?: "<empty>"
    assertSameLinesWithFile("${testDataPath}/${getTestFileName("html")}", doc)
  }
}