// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.prisma

import com.intellij.codeInsight.documentation.DocumentationManager
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.util.Disposer
import com.intellij.testFramework.PlatformTestUtil
import com.intellij.testFramework.common.timeoutRunBlocking
import com.intellij.testFramework.junit5.TestApplication
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@TestApplication
class PrismaDocumentationTest : PrismaTestCase("documentation") {
  @BeforeEach
  fun setUpColorScheme() {
    val colorsManager = EditorColorsManager.getInstance()
    val schemeBefore = colorsManager.globalScheme
    colorsManager.setGlobalScheme(colorsManager.getScheme("Darcula"))
    Disposer.register(web.testRootDisposable) {
      colorsManager.setGlobalScheme(schemeBefore)
    }
  }

  @Test
  fun testModel(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testKeyword(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testKeywordType(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testKeywordTypeInAlias(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testType(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testEnum(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testEnumValue(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testGenerator(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDatasource(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testField(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testFieldTrailingDoc(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testKeyValue(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testPrimitiveType(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testUnsupportedType(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDatasourceField(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testGeneratorField(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testBlockAttribute(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testBlockAttributeParam(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testBlockAttributeFieldArgument(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testFieldAttribute(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testFieldAttributeParam(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDatasourceProviderValue(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDatasourceUrlFunction(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testStringLiteralVariant(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testRelationField(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDefaultValue(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testNativeTypeNamespace(): Unit = timeoutRunBlocking {
    doTest()
  }

  @Test
  fun testDocComment(): Unit = timeoutRunBlocking {
    doTest()
  }

  private suspend fun doTest() {
    val file = myFixture.configureByFile(getTestFileName())
    val generated = readAction {
      val originalElement = file.findElementAt(myFixture.caretOffset)
      val element = DocumentationManager.getInstance(myFixture.project)
        .findTargetElement(myFixture.editor, file, originalElement)
      val provider = DocumentationManager.getProviderFromElement(element, originalElement)
      provider.generateDoc(element, originalElement)
    }
    val doc = generated?.let { reformatDocumentation(project, it) } ?: "<empty>"
    PlatformTestUtil.assertSameLinesWithFile("${testDataPath}/${getTestFileName("html")}", doc)
  }
}