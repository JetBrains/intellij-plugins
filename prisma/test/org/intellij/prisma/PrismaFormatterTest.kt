package org.intellij.prisma

import com.intellij.psi.formatter.FormatterTestCase

class PrismaFormatterTest : FormatterTestCase() {
  override fun getBasePath(): String = "/formatter"

  override fun getTestDataPath(): String = getPrismaTestDataPath()

  override fun getFileExtension(): String = "prisma"

  fun testIndents() {
    doTest()
  }

  fun testLineSpacing() {
    doTest()
  }

  fun testTrailingNewLine() {
    doTest()
  }

  fun testEmptyFile() {
    doTest()
  }

  fun testModelSpacing() {
    doTest()
  }

  fun testEnumSpacing() {
    doTest()
  }

  fun testTypeSpacing() {
    doTest()
  }

  fun testGeneratorSpacing() {
    doTest()
  }

  fun testDatasourceSpacing() {
    doTest()
  }

  fun testKeyValueAlignment() {
    doTest()
  }

  fun testFieldsAlignment() {
    doTest()
  }

  fun testTypeAliasAlignment() {
    doTest()
  }

  fun testAlignmentWithDocComments() {
    doTest()
  }
}