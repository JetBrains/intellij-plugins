package org.intellij.prisma

import com.intellij.testFramework.ParsingTestCase
import org.intellij.prisma.lang.parser.PrismaParserDefinition

class PrismaParserTest : ParsingTestCase(
  "parser",
  "prisma",
  true,
  PrismaParserDefinition()
) {
  override fun getTestDataPath(): String = getPrismaTestDataPath()

  fun testPrismaFile() = doTest(true, true)

  fun testUnsupportedType() = doTest(true, false)

  fun testUnsupportedTypeInAlias() = doTest(true, true)

  fun testPathExpression() = doTest(true, true)
}