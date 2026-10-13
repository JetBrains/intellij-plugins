package org.intellij.prisma

import com.intellij.platform.testFramework.junit5.codeInsight.fixture.parsingFixture
import com.intellij.testFramework.junit5.TestApplication
import com.intellij.testFramework.junit5.fixture.projectFixture
import org.intellij.prisma.lang.parser.PrismaParserDefinition
import org.junit.jupiter.api.Test

@TestApplication
class PrismaParserTest {
  companion object {
    private val projectFixture = projectFixture()
  }

  private val parsing by parsingFixture(projectFixture, getPrismaTestDataPath() + "/parser", "prisma", true, PrismaParserDefinition())

  @Test
  fun testPrismaFile() = parsing.doTest(true, true)

  @Test
  fun testUnsupportedType() = parsing.doTest(true, false)

  @Test
  fun testUnsupportedOptionalType() = parsing.doTest(true, true)

  @Test
  fun testUnsupportedOptionalListType() = parsing.doTest(true, true)

  @Test
  fun testUnsupportedListType() = parsing.doTest(true, true)

  @Test
  fun testUnsupportedTypeInAlias() = parsing.doTest(true, true)

  @Test
  fun testPathExpression() = parsing.doTest(true, true)

  @Test
  fun testViewDeclaration() = parsing.doTest(true, true)

  @Test
  fun testComments() = parsing.doTest(true, true)
}